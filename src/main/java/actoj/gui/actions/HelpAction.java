package actoj.gui.actions;

import ij.IJ;
import ij.plugin.BrowserLauncher;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;

@SuppressWarnings("serial")
public class HelpAction extends AbstractAction {

	private final String url = "http://actogramj.neurofly.de";

	public HelpAction() {
		putValue(SHORT_DESCRIPTION, "Help");
		putValue(LONG_DESCRIPTION, "Help");
		putValue(NAME, "Help");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/help.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			BrowserLauncher.openURL(url);
		} catch(Exception ex) {
			IJ.error("Can't open browser. Please open the following URL manually: " + url);
			ex.printStackTrace();
		}
	}
}

