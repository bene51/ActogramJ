package actoj.gui.actions;

import actoj.ActogramJ_;
import actoj.gui.ImageCanvas;
import actoj.io.PDFExporter;

import ij.IJ;
import ij.io.SaveDialog;

import javax.swing.*;

import java.awt.event.ActionEvent;

import java.io.File;

public class ExportPDF extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON 

	private final ImageCanvas canvas;

	public ExportPDF(ImageCanvas canvas) {
		this.canvas = canvas;
		putValue(SHORT_DESCRIPTION, "Export PDF");
		putValue(LONG_DESCRIPTION, "Export PDF");
		putValue(NAME, "Export PDF");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/exportpdf.png")));
	}

	public void actionPerformed(ActionEvent e) {
		SaveDialog od = new SaveDialog("Export as pdf...", "", ".pdf");
		String dir = od.getDirectory();
		String file = od.getFileName();
		if(dir == null || file == null)
			return;
		PDFExporter pe = new PDFExporter(canvas,
				new File(dir, file).getAbsolutePath());
		try {
			pe.export();
		} catch(Exception ex) {
			IJ.error(ex.getMessage());
			return;
		}
	}
}

