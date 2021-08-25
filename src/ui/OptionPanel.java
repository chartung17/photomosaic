package ui;

import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * This class is used for selecting an option's value using a JSlider.
 */
public class OptionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel widthLabel = new JLabel(UserInterface.TILE_WIDTH);
	private JLabel heightLabel = new JLabel(UserInterface.TILE_HEIGHT);
	private JLabel transparencyLabel = new JLabel(UserInterface.TRANSPARENCY);
	private JSlider widthSlider, heightSlider, transparencySlider;
	private JCheckBox cacheCheckbox = new JCheckBox(UserInterface.ENABLE_CACHE, true);

	/**
	 * Constructs the OptionPanel.
	 * 
	 * @param listener the ChangeListener to listen to the slider
	 */
	public OptionPanel(ChangeListener listener) {
		this.setLayout(new GridLayout(4, 1));
		widthSlider = createSlider(UserInterface.TILE_WIDTH, listener, 0, 250, 50);
		widthSlider.setToolTipText("Select the tile width in pixels.");
		heightSlider = createSlider(UserInterface.TILE_HEIGHT, listener, 0, 250, 50);
		heightSlider.setToolTipText("Select the tile height in pixels.");
		transparencySlider = createSlider(UserInterface.TRANSPARENCY, listener, 0, 100, 100);
		transparencySlider.setToolTipText(
				"Select the transparency percentage for the main image. If transparency is 100%, only the tiles"
						+ " will be visible; if transparency is 0%, only the main image will be visible.");
		widthLabel.setLabelFor(widthSlider);
		heightLabel.setLabelFor(heightSlider);
		transparencyLabel.setLabelFor(transparencySlider);
		cacheCheckbox.setName(UserInterface.ENABLE_CACHE);
		cacheCheckbox.addChangeListener(listener);
		cacheCheckbox.setToolTipText(
				"If the cache is enabled, image tile data will be cached to disk. This will increase the time"
						+ " required for the first run with a given image folder and tile size, but it will"
						+ " significantly improve performace for later runs with the same settings.");
		JPanel widthPanel = new JPanel();
		widthPanel.add(widthLabel);
		widthPanel.add(widthSlider);
		add(widthPanel);
		JPanel heightPanel = new JPanel();
		heightPanel.add(heightLabel);
		heightPanel.add(heightSlider);
		add(heightPanel);
		JPanel transparencyPanel = new JPanel();
		transparencyPanel.add(transparencyLabel);
		transparencyPanel.add(transparencySlider);
		add(transparencyPanel);
		JPanel cachePanel = new JPanel();
		cachePanel.add(cacheCheckbox);
		add(cachePanel);
	}

	/**
	 * Creates a JSlider with the specified characteristics.
	 * 
	 * @param name     the name of the slider
	 * @param listener the ChangeListener to listen to the slider
	 * @param min      the minimum value of the slider
	 * @param max      the maximum value of the slider
	 * @param value    the initial value of the slider
	 * @return a JSlider with the specified characteristics
	 */
	private JSlider createSlider(String name, ChangeListener listener, int min, int max,
			int value) {
		JSlider slider = new JSlider(min, max, value);
		slider.addChangeListener(listener);
		slider.setName(name);
		slider.setMajorTickSpacing((max - min) / 5);
		slider.setMinorTickSpacing((max - min) / 25);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		return slider;
	}
}
