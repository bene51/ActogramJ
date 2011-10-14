package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.CustomWindow;
import actoj.gui.PropertiesDialog;

@SuppressWarnings("serial")
public class PropertiesAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final CustomWindow win;

	public PropertiesAction(CustomWindow win) {
		this.win = win;
		putValue(SHORT_DESCRIPTION, "Edit properties");
		putValue(LONG_DESCRIPTION, "Edit properties");
		putValue(NAME, "Properties");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/properties.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PropertiesDialog.changeProperties(win);
	}
}

