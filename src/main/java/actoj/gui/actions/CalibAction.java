package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ActogramCanvas;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class CalibAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public CalibAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Eye fit tool");
		putValue(LONG_DESCRIPTION, "Eye fit tool");
		putValue(NAME, "Eye fit tool");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/FreerunningPeriod.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.setCanvasMode(ActogramCanvas.Mode.FREERUNNING_PERIOD);
	}
}

