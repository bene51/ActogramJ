package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class AverageActivityAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public AverageActivityAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Average Activity");
		putValue(LONG_DESCRIPTION, "Average Activity");
		putValue(NAME, "Average Activity");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/AverageActivity.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.calculateAverageActivity();
	}
}

