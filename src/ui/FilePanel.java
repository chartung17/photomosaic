package ui;

import java.awt.FontMetrics;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * This class is used to select the image file and folder to use.
 */
public class FilePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField label = new JTextField();
	private JButton button;

	/**
	 * Construct the FilePanel.
	 * 
	 * @param buttonText the text to be displayed on the button
	 * @param listener the ActionListener which will listen to the button
	 */
	public FilePanel(String buttonText, ActionListener listener) {
		label.setEditable(false);
		label.setColumns(42);
		label.setHorizontalAlignment(JTextField.CENTER);
		button = new JButton(buttonText);
		button.setActionCommand(buttonText);
		button.addActionListener(listener);
		add(label);
		add(button);
	}

	/**
	 * Display the name of the selected file or folder.
	 * 
	 * @param text the text to display
	 */
	public void setLabel(String text) {
		if (text == null)
			text = "";
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int width = label.getWidth() - 10;
		if (fm.stringWidth(text) <= width) {
			label.setText(text);
			return;
		}
		StringBuilder sb = new StringBuilder(text);
		while (fm.stringWidth("..." + sb.toString()) > width)
			sb.deleteCharAt(0);
		label.setText("..." + sb.toString());
	}
}
