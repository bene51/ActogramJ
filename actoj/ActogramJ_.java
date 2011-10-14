package actoj;

import ij.plugin.PlugIn;

import javax.swing.SwingUtilities;

import actoj.gui.CustomWindow;

public class ActogramJ_ implements PlugIn {

	@Override
	public void run(String arg) {
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					new CustomWindow();
				}
			});
	}
}

