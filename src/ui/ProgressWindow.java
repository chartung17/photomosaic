package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A window used to display a progress bar.
 */
public class ProgressWindow {
	private JFrame frame = new JFrame("Progress");
	private JPanel panel = new JPanel();
	private static final ProgressWindow instance = new ProgressWindow();
	JProgressBar progressBar = new JProgressBar(0, 100);

	/**
	 * Constructs the ProgressWindow.
	 */
	private ProgressWindow() {
		frame.setSize(300, 100);
		progressBar.setStringPainted(true);
		progressBar.setString("0%");
		frame.getContentPane().add(panel);
		panel.add(progressBar);
	}

	/**
	 * Get the ProgressWindow instance.
	 * 
	 * @return the ProgressWindow instance
	 */
	public static ProgressWindow getInstance() {
		return instance;
	}

	/**
	 * This method updates the value of the progress bar.
	 * 
	 * @param value the value to set
	 */
	public synchronized void update(int value) {
		progressBar.setValue(value);
		progressBar.setString(value + "%");
	}

	/**
	 * This method allows the ProgressWindow to be displayed above the center of the specified
	 * JFrame.
	 * 
	 * @param parent the JFrame over which the ProgressWindow should be displayed
	 */
	public synchronized void setParent(JFrame parent) {
		frame.setLocationRelativeTo(parent);
	}

	/**
	 * Sets the visibility of the ProgressWindow.
	 * 
	 * @param visible true to display the ProgressWindow or false to hide it
	 */
	public synchronized void setVisible(boolean visible) {
		frame.setVisible(visible);
	}
}
