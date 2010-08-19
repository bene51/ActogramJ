package actoj.gui.actions;

import actoj.ActogramJ_;
import actoj.core.ActogramGroup;
import actoj.io.ActogramReader;
import actoj.gui.ImageCanvas;
import actoj.gui.ActogramCanvas;

import ij.IJ;
import ij.io.OpenDialog;

import javax.swing.*;

import java.awt.event.ActionEvent;

import java.io.File;

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
		putValue(SHORT_DESCRIPTION, "Calibration");
		putValue(LONG_DESCRIPTION, "Calibration");
		putValue(NAME, "");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/calib.png")));
	}

	public void actionPerformed(ActionEvent e) {
		canvas.setCanvasMode(ActogramCanvas.Mode.FREERUNNING_PERIOD);
	}
}

