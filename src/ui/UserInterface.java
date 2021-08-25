package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;

import cache.CacheManager;
import image.ImageProcessor;

/**
 * Singleton class for the user interface.
 */
public class UserInterface implements ActionListener, ChangeListener {
	private static final String SELECT_IMAGE = "Select main image";
	private static final String SELECT_FOLDER = "Select image folder";
	private static final String START = "Start";
	private static final String CLEAR_CACHE = "Clear Cache";

	public static final String TILE_WIDTH = "Tile Width";
	public static final String TILE_HEIGHT = "Tile Height";
	public static final String TRANSPARENCY = "Transparency Percent";
	public static final String ENABLE_CACHE = "Enable Cache";
	public static final int WIDTH = 500, HEIGHT = 600;

	private JFrame frame = new JFrame("Photomosaic Maker");
	private Container pane = frame.getContentPane();
	private JPanel messagePanel = new JPanel();
	private JTextField message = new JTextField("Photomosaic Maker");
	private JFileChooser imageChooser = new JFileChooser();
	private JFileChooser folderChooser = new JFileChooser();
	private OptionPanel optionPanel = new OptionPanel(this);
	private JPanel buttonPanel = new JPanel();
	private JButton startButton = new JButton(START);
	private JButton clearButton = new JButton(CLEAR_CACHE);
	private FilePanel imagePanel, folderPanel;
	private File image, folder;
	private ImageProcessor processor = new ImageProcessor();
	private int tileWidth = 50, tileHeight = 50, transparencyPercent = 100;
	private boolean cacheEnabled = true;
	private ProgressWindow progress = ProgressWindow.getInstance();

	private static UserInterface instance;

	/**
	 * This method initializes the UserInterface.
	 */
	private UserInterface() {
		// set the folder chooser to only allow the user to choose folders
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// set the image file chooser to only accept common image file extensions
		// the user can choose to look at all files instead
		imageChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				String path = f.getAbsolutePath();
				String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
				switch (extension) {
				case "jpeg":
				case "jpg":
				case "tif":
				case "tiff":
				case "png":
				case "gif":
				case "bmp":
					return true;
				default:
					return false;
				}
			}

			@Override
			public String getDescription() {
				return "Image Files";
			}
		});

		// initialize UI components and add to the frame
		messagePanel.add(message);
		message.setEditable(false);
		message.setHorizontalAlignment(JTextField.CENTER);
		message.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
		message.setBorder(BorderFactory.createEmptyBorder());
		messagePanel.setMaximumSize(new Dimension(WIDTH, 40));
		startButton.addActionListener(this);
		startButton.setActionCommand(START);
		startButton.setToolTipText(
				"Start creating a photomosaic with the specified options. Depending on the size of"
						+ " the selected main image and image folder, processing may take a few minutes.");
		clearButton.addActionListener(this);
		clearButton.setActionCommand(CLEAR_CACHE);
		clearButton.setToolTipText("Delete all cached files.");
		buttonPanel.add(startButton);
		buttonPanel.add(clearButton);
		buttonPanel.setMaximumSize(new Dimension(WIDTH, 50));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		frame.setSize(WIDTH, HEIGHT);
		imagePanel = new FilePanel(SELECT_IMAGE, this);
		folderPanel = new FilePanel(SELECT_FOLDER, this);
		pane.add(messagePanel);
		pane.add(imagePanel);
		pane.add(folderPanel);
		pane.add(optionPanel);
		pane.add(buttonPanel);
		frame.setVisible(true);
		progress.setParent(frame);
	}

	/**
	 * This method returns the singleton UserInterface instance.
	 * 
	 * @return the UserInterface instance
	 */
	public static synchronized UserInterface getInstance() {
		if (instance == null)
			instance = new UserInterface();
		return instance;
	}

	/**
	 * This method starts creating a photomosaic with the specified parameters.
	 */
	private void start() {
		// show error message and enable the buttons if user did not select an image and a folder
		if ((image == null) || (folder == null)) {
			startButton.setEnabled(true);
			clearButton.setEnabled(true);
			JOptionPane.showMessageDialog(frame, "Please select a main image and an image folder.");
			return;
		}

		// show the progress bar and create the photomosaic
		progress.update(0);
		progress.setVisible(true);
		boolean result = processor.createPhotomosaic(Math.max(tileWidth, 1),
				Math.max(tileHeight, 1), transparencyPercent, image.getAbsolutePath(),
				folder.getAbsolutePath(), cacheEnabled);

		// after completion, hide the progess bar and enable the buttons
		progress.setVisible(false);
		startButton.setEnabled(true);
		clearButton.setEnabled(true);

		// show an error message if unsuccessful
		if (!result)
			JOptionPane.showMessageDialog(frame,
					"Error: unable to create photomosaic. \n"
							+ "Please ensure you have selected a valid image file \n"
							+ "and a valid folder containing at least one image.");
	}

	/**
	 * This method clears the cache.
	 */
	private void clearCache() {
		CacheManager.getInstance().clearCache();
		startButton.setEnabled(true);
		clearButton.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case SELECT_IMAGE:
			int result = imageChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				image = imageChooser.getSelectedFile();
				imagePanel.setLabel(image.getAbsolutePath());
			}
			break;
		case SELECT_FOLDER:
			int folderResult = folderChooser.showOpenDialog(frame);
			if (folderResult == JFileChooser.APPROVE_OPTION) {
				folder = folderChooser.getSelectedFile();
				folderPanel.setLabel(folder.getAbsolutePath());
			}
			break;
		case START:
			startButton.setEnabled(false);
			clearButton.setEnabled(false);
			new Thread(() -> start()).start();
			break;
		case CLEAR_CACHE:
			startButton.setEnabled(false);
			clearButton.setEnabled(false);
			new Thread(() -> clearCache()).start();
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Component source = (Component) e.getSource();
		String name = source.getName();
		int value = 0;
		if (source instanceof JSlider)
			value = ((JSlider) source).getValue();
		switch (name) {
		case TILE_WIDTH:
			tileWidth = value;
			break;
		case TILE_HEIGHT:
			tileHeight = value;
			break;
		case TRANSPARENCY:
			transparencyPercent = value;
			break;
		case ENABLE_CACHE:
			cacheEnabled = ((JCheckBox) source).isSelected();
			break;
		}
	}
}
