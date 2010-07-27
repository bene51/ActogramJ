package actoj.gui;

import ij.gui.GenericDialog;

public class PropertiesDialog {

	public static final void changeProperties(CustomWindow win) {
		int ppl  = win.getPeriodsPerLine();
		float ul = win.getUpperLimit();
		int cols = win.getNumColumns();
		int subd = win.getCalibrationSubdivisions();

		GenericDialog gd = new GenericDialog("Edit Properties");
		gd.addNumericField("Number of plots", ppl, 0);
		gd.addNumericField("Upper limit", ul, 1);
		gd.addNumericField("Max. number of columns on sheet", cols, 0);
		gd.addNumericField("Number of calibration subdivisions", subd, 0);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		ppl  = (int)gd.getNextNumber();
		ul   = (float)gd.getNextNumber();
		cols = (int)gd.getNextNumber(); 
		subd = (int)gd.getNextNumber();

		win.setPeriodsPerLine(ppl);
		win.setUpperLimit(ul);
		win.setNumColumns(cols);
		win.setCalibrationSubdivisions(subd);
	}
}

