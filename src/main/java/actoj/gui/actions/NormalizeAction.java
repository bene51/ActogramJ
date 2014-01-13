package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.core.Actogram;
import actoj.gui.CustomWindow;

@SuppressWarnings("serial")
public class NormalizeAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final CustomWindow win;

	public NormalizeAction(CustomWindow win) {
		this.win = win;
		putValue(SHORT_DESCRIPTION, "Normalize");
		putValue(LONG_DESCRIPTION, "Normalize");
		putValue(NAME, "Normalize");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/Normalize.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Actogram[] normalized = win.getCanvas().normalizeActograms();
		if(normalized == null)
			return;
		for(Actogram a : normalized)
			win.getTreeView().addCalculated(a);

		win.getTreeView().setSelection(normalized);
	}
}

