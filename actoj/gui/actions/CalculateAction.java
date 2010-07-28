package actoj.gui.actions;

import actoj.ActogramJ_;
import actoj.gui.CalculateDialog;
import actoj.gui.CustomWindow;

import javax.swing.*;

import java.awt.event.ActionEvent;

public class CalculateAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final CustomWindow win;

	public CalculateAction(CustomWindow win) {
		this.win = win;
		putValue(SHORT_DESCRIPTION, "Calculate");
		putValue(LONG_DESCRIPTION, "Calculate");
		putValue(NAME, "Calculate");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/properties.png")));
	}

	public void actionPerformed(ActionEvent e) {
		CalculateDialog.run(win);
	}
}

