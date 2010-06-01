package actoj;

import javax.swing.SwingUtilities;

import actoj.gui.CustomWindow;
import ij.plugin.PlugIn;

public class ActogramJ_ implements PlugIn {

	private CustomWindow window;

	public void run(String arg) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					window = new CustomWindow();
				}
			});
	}
}

