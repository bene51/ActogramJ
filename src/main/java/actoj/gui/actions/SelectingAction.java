package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ActogramCanvas;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class SelectingAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public SelectingAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Selecting");
		putValue(LONG_DESCRIPTION, "Selecting");
		putValue(NAME, "Selection tool");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/Select.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.setCanvasMode(ActogramCanvas.Mode.SELECTING);
	}
}

