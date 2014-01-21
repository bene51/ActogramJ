package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class OnOffAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public OnOffAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Activity on- and offset");
		putValue(LONG_DESCRIPTION, "Activity on- and offset");
		putValue(NAME, "Activity on- and offset");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/OnOffset.png"))); // TODO create and fill in icon
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.calculateOnAndOffsets();
	}
}
