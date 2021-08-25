package main;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import ui.UserInterface;

public class Main {
	public static void main(String[] args) {
		// if heap size is less than 1GB, restart in a new process with a larger heap and wait for
		// that process to finish
		if (Runtime.getRuntime().maxMemory() < 1_000_000_000) {
			String path;
			try {
				path = Main.class.getResource(Main.class.getSimpleName() + ".class").getFile();
				path = ClassLoader.getSystemResource(path).getFile();
				path = path.substring(0, path.lastIndexOf('!'));
				path = Paths.get(new URI(path)).toString();
			} catch (NullPointerException | URISyntaxException e) {
				path = "Photomosaic.jar";
			}
			try {
				Process proc = new ProcessBuilder().command("Java", "-Xmx1g", "-jar", path)
						.inheritIO().start();
				proc.waitFor();
			} catch (Exception e) {
				// show error message if unable to start new process
				System.out.println(
						"Error: unable to run. Please try again with a heap size of at least 1GB.");
			} finally {
				System.exit(0);
			}
		}
		
		// start the user interface
		UserInterface.getInstance();
	}
}
