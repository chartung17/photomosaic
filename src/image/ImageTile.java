package image;

import java.awt.*;
import java.awt.image.*;

import cache.CacheManager;

/**
 * This class represents a single tile that may be used in the photomosaic. Instances of this class
 * are immutable.
 */
public final class ImageTile {
	private final BufferedImage image;
	private final Color avgColor;

	/**
	 * Constructs an ImageTile.
	 * 
	 * @param img      the image to use
	 * @param width    the width of the tile
	 * @param height   the height of the tile
	 * @param filename the original filename of the image
	 * @param isCached true if the image was loaded from the cache
	 */
	public ImageTile(BufferedImage img, int width, int height, String filename, boolean isCached) {
		if (isCached) {
			image = img;
		} else {
			// crop image to be proportional to specified dimensions
			int w = img.getWidth();
			int h = img.getHeight();
			double ratio = width / (double) height;
			int x, y, scaledW, scaledH;
			if ((h * ratio) > w) {
				scaledW = w;
				scaledH = (int) (w / ratio);
				x = 0;
				y = (h - scaledH) / 2;
			} else {
				scaledW = (int) (h * ratio);
				scaledH = h;
				x = (w - scaledW) / 2;
				y = 0;
			}
			img = img.getSubimage(x, y, scaledW, scaledH);

			// scale image to specified size
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			g.drawImage(img, 0, 0, width, height, 0, 0, scaledW, scaledH, null);
			g.dispose();
		}

		// determine average color
		int[] cacheColor;
		if (isCached && ((cacheColor = CacheManager.getInstance().getColor(filename, width,
				height)) != null)) {
			avgColor = new Color(cacheColor[0], cacheColor[1], cacheColor[2]);
		} else {
			int[] rgbColors = image.getRGB(0, 0, width, height, null, 0, width);
			Color[] colors = new Color[width * height];
			int rTotal = 0;
			int gTotal = 0;
			int bTotal = 0;
			for (int i = 0; i < rgbColors.length; i++) {
				colors[i] = new Color(rgbColors[i]);
				rTotal += colors[i].getRed();
				gTotal += colors[i].getGreen();
				bTotal += colors[i].getBlue();
			}
			avgColor = new Color(rTotal / colors.length, gTotal / colors.length,
					bTotal / colors.length);
		}

		// cache image if not already cached
		if (!isCached)
			CacheManager.getInstance().cache(filename, this, width, height);
	}

	/**
	 * Get a copy of this tile's image.
	 * 
	 * @return a copy of the image
	 */
	public BufferedImage getImage() {
		BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(),
				image.getType());
		Graphics g = copy.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return copy;
	}

	/**
	 * Get the red value of this tile's average color.
	 * 
	 * @return the red value of this tile's average color
	 */
	public int getRed() {
		return avgColor.getRed();
	}

	/**
	 * Get the blue value of this tile's average color.
	 * 
	 * @return the blue value of this tile's average color
	 */
	public int getBlue() {
		return avgColor.getBlue();
	}

	/**
	 * Get the green value of this tile's average color.
	 * 
	 * @return the green value of this tile's average color
	 */
	public int getGreen() {
		return avgColor.getGreen();
	}
}
