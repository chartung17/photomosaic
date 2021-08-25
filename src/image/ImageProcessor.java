package image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import cache.CacheManager;
import octree.Octree;
import ui.ProgressWindow;

/**
 * Class used to process images and create the photomosaic.
 */
public class ImageProcessor {
	private int fileCount = 0, fileTotal = 1;
	private static final int COUNT_TICK = 50;
	private static ProgressWindow progress = ProgressWindow.getInstance();

	/**
	 * This method increments fileCount by 1 and then checks whether the value of fileCount is a
	 * multiple of COUNT_TICK
	 * 
	 * @return true if fileCount is a multiple of COUNT_TICK after incrementing and false otherwise
	 */
	private synchronized boolean incrementCount() {
		return ++fileCount % COUNT_TICK == 0;
	}

	/**
	 * This method creates a photomosaic with the specified parameters and returns true if
	 * successful.
	 * 
	 * @param tileWidth           the width of the image tiles
	 * @param tileHeight          the height of the image tiles
	 * @param transparencyPercent the transparency of the main image as a percentage between 0
	 *                            (opaque; only the main image is visible in the output) and 100
	 *                            (completely transparent; only the image tiles are visible in the
	 *                            output)
	 * @param imagePath           the path to the main image
	 * @param directory           the path to the directory containing the images to use for the
	 *                            image tiles
	 * @param cacheEnabled        true if the cache should be used and false otherwise
	 * @return true if successful and false if unsuccessful
	 */
	public boolean createPhotomosaic(int tileWidth, int tileHeight, int transparencyPercent,
			String imagePath, String directory, boolean cacheEnabled) {
		fileCount = 0;
		Octree<ImageTile> tree = new Octree<>(0, 255);
		CacheManager manager = CacheManager.getInstance();
		manager.setCacheEnabled(cacheEnabled);
		ExecutorService service = null;
		try {
			// construct image tiles
			File imageFolder = new File(directory);
			manager.setDirectory(directory);

			// read image
			service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			Future<BufferedImage> imageFuture = service
					.submit(() -> ImageIO.read(new File(imagePath)));

			// iterate over all images in the source folder
			List<Callable<ImageTile>> tasks = new ArrayList<>();
			String[] fileList = imageFolder.list();
			if (fileList == null)
				fileList = new String[0];
			fileTotal = fileList.length + 1;
			for (String file : fileList) {
				tasks.add(() -> {
					try {
						// check for a matching cached image
						BufferedImage tileImage = manager.getCached(file, tileWidth, tileHeight);
						boolean isCached = (tileImage != null);

						// if there is no matching cached image, read the image file from the source
						// folder
						if (!isCached) {
							tileImage = ImageIO.read(new File(directory + File.separator + file));
						}

						// if an image has been read from either the cache or the source folder,
						// create
						// an image tile using that image, otherwise set the tile to null
						ImageTile tile = null;
						if (tileImage != null) {
							tile = new ImageTile(tileImage, tileWidth, tileHeight, file, isCached);
						}

						// update progress
						if (incrementCount()) {
							SwingUtilities.invokeLater(
									() -> progress.update((100 * fileCount) / fileTotal));
						}

						// return the tile
						return tile;
					} catch (Exception e) {
						return null;
					}
				});
			}
			List<Future<ImageTile>> results = new ArrayList<>();
			for (Callable<ImageTile> task : tasks) {
				results.add(service.submit(task));
			}
			for (Future<ImageTile> future : results) {
				try {
					ImageTile tile = future.get(10L, TimeUnit.SECONDS);
					if (tile != null)
						tree.put(tile.getRed(), tile.getGreen(), tile.getBlue(), tile);
				} catch (ExecutionException | InterruptedException | TimeoutException e) {
					// skip this tile;
				}
			}
			if (tree.size() == 0) {
				return false;
			}
			// unpack the Future with the main image
			BufferedImage image;
			try {
				image = imageFuture.get(10L, TimeUnit.SECONDS);
			} catch (ExecutionException | InterruptedException | TimeoutException e) {
				// if an error occurred with the main image, read it again
				image = ImageIO.read(new File(imagePath));
			}
			if (image == null) {
				return false;
			}

			// clear CacheManager's filenames tree to save heap space
			manager.clearFilenames();

			fileCount = fileTotal;

			// crop image so width and height are multiples of tileWidth and tileHeight
			int width = image.getWidth();
			width -= width % tileWidth;
			int height = image.getHeight();
			height -= height % tileHeight;
			image = image.getSubimage(0, 0, width, height);

			// for each tile of the image, update the tile to the image tile closest to its
			// average pixel color
			Graphics2D g = image.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					transparencyPercent / 100.0f));
			int[] rgbColors = new int[tileWidth * tileHeight];
			Color[] colors = new Color[tileWidth * tileHeight];
			for (int x = 0; x < width; x += tileWidth) {
				for (int y = 0; y < height; y += tileHeight) {
					image.getRGB(x, y, tileWidth, tileHeight, rgbColors, 0, tileWidth);
					int rTotal = 0;
					int gTotal = 0;
					int bTotal = 0;
					for (int i = 0; i < rgbColors.length; i++) {
						colors[i] = new Color(rgbColors[i]);
						rTotal += colors[i].getRed();
						gTotal += colors[i].getGreen();
						bTotal += colors[i].getBlue();
					}
					Color avgColor = new Color(rTotal / colors.length, gTotal / colors.length,
							bTotal / colors.length);
					ImageTile nearby = tree.getNearestEntry(avgColor.getRed(), avgColor.getGreen(),
							avgColor.getBlue()).getValue();
					g.drawImage(nearby.getImage(), x, y, null);
				}
			}

			File output = new File("." + File.separator + "temp.jpg");
			ImageIO.write(image, "jpg", output);
			Desktop.getDesktop().open(output);
		} catch (IOException | NullPointerException e) {
			return false;
		} finally {
			if (service != null) {
				service.shutdownNow();
			}
		}
		return true;
	}
}
