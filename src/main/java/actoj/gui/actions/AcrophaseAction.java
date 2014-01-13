package actoj.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import actoj.ActogramJ_;
import actoj.gui.ImageCanvas;

@SuppressWarnings("serial")
public class AcrophaseAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON

	private final ImageCanvas canvas;

	public AcrophaseAction(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Acrophase");
		putValue(LONG_DESCRIPTION, "Acrophase");
		putValue(NAME, "Acrophase");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/Acrophase.png"))); // TODO create and fill in icon
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas.calculateAcrophase();
	}
}
