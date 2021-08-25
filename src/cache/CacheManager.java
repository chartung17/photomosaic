package cache;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import image.ImageTile;

/**
 * Singleton class used to read from and write to the cache.
 */
public class CacheManager {
	private static final CacheManager instance = new CacheManager();
	public static final String CACHE = "." + File.separator + "PhotomosaicCache";
	private boolean cacheEnabled = false;
	private String directory;
	private TreeSet<String> filenames;

	/**
	 * Initialize the CacheManager.
	 */
	private CacheManager() {
	}

	/**
	 * Get the singleton CacheManager instance.
	 * 
	 * @return the CacheManager instance
	 */
	public static CacheManager getInstance() {
		return instance;
	}

	/**
	 * Clear the filenames set to save heap space.
	 */
	public synchronized void clearFilenames() {
		filenames = null;
	}

	/**
	 * This methods attempts to enable or disable the cache. Enabling the cache will fail if the
	 * cache directory does not exist and cannot be created. Disabling the cache will always
	 * succeed.
	 * 
	 * @param cacheEnabled true to attempt to enable the cache or false to disable the cache
	 */
	public synchronized void setCacheEnabled(boolean cacheEnabled) {
		if (!cacheEnabled) {
			this.cacheEnabled = false;
			return;
		}
		File cacheDirectory = new File(CACHE);
		if (!cacheDirectory.exists()) {
			this.cacheEnabled = cacheDirectory.mkdir();
		} else if (!cacheDirectory.isDirectory()) {
			this.cacheEnabled = false;
		} else {
			this.cacheEnabled = true;
		}
	}

	/**
	 * Set the directory to use.
	 * 
	 * @param directory the directory to use
	 */
	public synchronized void setDirectory(String directory) {
		if (!cacheEnabled)
			return;
		directory = directory.substring(directory.lastIndexOf(File.separator) + 1);
		File cacheDirectory = new File(CACHE + File.separator + directory);
		if (!cacheDirectory.exists()) {
			this.directory = cacheDirectory.mkdir() ? directory : null;
		} else if (!cacheDirectory.isDirectory()) {
			this.directory = null;
		} else {
			this.directory = directory;
		}
		filenames = null;
	}

	/**
	 * Get the beginning of the filenames of cached files related to the given file.
	 * 
	 * @param filename the original filename
	 * @param width    the image tile width
	 * @param height   the image tile height
	 * @return the String to use as the beginning of the filename for all cached files related to
	 *         the given file
	 */
	private String getFileRoot(String filename, int width, int height) {
		String cacheDirectory = CACHE;
		if (directory != null)
			cacheDirectory += (File.separator + directory);
		return cacheDirectory + File.separator + filename + "_" + width + "_" + height;
	}

	/**
	 * Get the File used to cache the given image.
	 * 
	 * @param filename the original filename
	 * @param width    the image tile width
	 * @param height   the image tile height
	 * @return the File used to cache the given image
	 */
	private File getFile(String filename, int width, int height) {
		return new File(getFileRoot(filename, width, height) + ".jpg");
	}

	/**
	 * Get the FIle used to cache color information for the given image.
	 * 
	 * @param filename the original filename
	 * @param width    the image tile width
	 * @param height   the image tile height
	 * @param red      the red value of the image tile's average color
	 * @param green    the green value of the image tile's average color
	 * @param blue     the blue value of the image tile's average color
	 * @return the File used to cache the given image
	 */
	private File getLogFile(String filename, int width, int height, int red, int green, int blue) {
		return new File(
				getFileRoot(filename, width, height) + "." + red + "_" + green + "_" + blue);
	}

	/**
	 * Assemble a TreeSet of all filenames in the specified directory within the cache that do not
	 * end in .jpg.
	 */
	private synchronized void assembleFilenames() {
		filenames = new TreeSet<>();
		String cacheDirectory = CACHE;
		if (directory != null)
			cacheDirectory += (File.separator + directory);
		File root = new File(cacheDirectory);
		for (String file : root.list()) {
			if (!file.endsWith("jpg")) {
				filenames.add(file);
			}
		}
	}

	/**
	 * If cached data exists for the specified file, this method returns a 3-element int array
	 * representing the red, green, and blue values of the file's average color. If the data cannot
	 * be found, this method returns null
	 * 
	 * @param filename the filename to check
	 * @param width    the width to check
	 * @param height   the height to check
	 * @return a 3-element int[] with the elements representing red, green, and blue in that order,
	 *         or null if the cached data does not exist
	 */
	public synchronized int[] getColor(String filename, int width, int height) {
		if (!cacheEnabled)
			return null;
		if (filenames == null) {
			assembleFilenames();
		}
		String file = filenames.ceiling(filename + "_" + width + "_" + height);
		if ((file == null) || (!file.startsWith(filename + "_" + width + "_" + height))) {
			return null;
		}
		file = file.substring(file.lastIndexOf('.') + 1);
		int[] color = new int[3];
		try {
			color[0] = Integer.parseInt(file.substring(0, file.indexOf('_')));
			color[1] = Integer
					.parseInt(file.substring(file.indexOf('_') + 1, file.lastIndexOf('_')));
			color[2] = Integer.parseInt(file.substring(file.lastIndexOf('_') + 1));
			return color;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method adds the file to the cache with the specified parameters.
	 * 
	 * @param filename the original name of the image file
	 * @param tile     the image tile to cache
	 * @param width    the width of the image
	 * @param height   the height of the image
	 */
	public synchronized void cache(String filename, ImageTile tile, int width, int height) {
		if (!cacheEnabled)
			return;
		File output = getFile(filename, width, height);
		File log = getLogFile(filename, width, height, tile.getRed(), tile.getGreen(),
				tile.getBlue());
		try {
			ImageIO.write(tile.getImage(), "jpg", output);
			log.createNewFile();
		} catch (IOException e) {
			// do nothing
		}
	}

	/**
	 * This method returns the cached image matching the specified parameters if it exists. If there
	 * is no matching cached image, or if caching is disabled, null is returned.
	 * 
	 * @param filename the file to look for
	 * @param width    the width to look for
	 * @param height   the height to look for
	 * @return the matching cached image, or null if there is no match or if caching is disabled
	 */
	public synchronized BufferedImage getCached(String filename, int width, int height) {
		if (!cacheEnabled)
			return null;
		try {
			return ImageIO.read(getFile(filename, width, height));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method deletes all cached files and returns true if successful.
	 * 
	 * @return true if successful and false otherwise
	 */
	public synchronized boolean clearCache() {
		boolean result = true;
		File root = new File("." + File.separator + CACHE);
		for (String dir : root.list()) {
			result &= deleteDir(dir);
		}
		return result;
	}

	/**
	 * This method deletes all cached files in the specified directory and returns true if
	 * successful.
	 * 
	 * @param directory the directory to delete
	 * @return true if successful and false otherwise
	 */
	public synchronized boolean clearCache(String directory) {
		return deleteDir(directory);
	}

	/**
	 * This method deletes the specified directory in the cache and all of its contents. If the
	 * argument is a file rather than a directory, this method deletes the file.
	 * 
	 * @param directory the directory to delete
	 * @return true if successful and false otherwise
	 */
	private boolean deleteDir(String directory) {
		directory = directory.substring(directory.lastIndexOf(File.separator) + 1);
		File dir = new File(CACHE + File.separator + directory);
		if (!dir.exists())
			return true;
		for (File file : dir.listFiles()) {
			if (!file.delete()) {
				return false;
			}
		}
		return dir.delete();
	}
}
