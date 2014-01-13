package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class FittingAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public FittingAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Periodogram");
		putValue(LONG_DESCRIPTION, "Periodogram");
		putValue(NAME, "Periodogram");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/Periodogram.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.calculatePeriodogram();
	}
}

