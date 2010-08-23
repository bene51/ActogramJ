package actoj.gui;

import ij.gui.GenericDialog;

public class PropertiesDialog {

	public static final void changeProperties(CustomWindow win) {
		ImageCanvas ic = win.canvas;
		int ppl  = ic.getPeriodsPerLine();
		float ul = ic.getUpperLimit();
		int cols = ic.getMaxColumns();
		int subd = ic.getCalibrationSubdivisions();
		float whRatio = ic.getWHRatio();

		GenericDialog gd = new GenericDialog("Edit Properties");
		gd.addNumericField("Number of plots", ppl, 0);
		gd.addNumericField("Upper limit", ul, 1);
		gd.addNumericField("Max. number of columns on sheet", cols, 0);
		gd.addNumericField("Number of calibration subdivisions", subd, 0);
		gd.addNumericField("Ratio w:h", whRatio, 2);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		ppl  = (int)gd.getNextNumber();
		ul   = (float)gd.getNextNumber();
		cols = (int)gd.getNextNumber(); 
		subd = (int)gd.getNextNumber();
		whRatio = (float)gd.getNextNumber();

		ic.set(ppl, ul, cols, subd, whRatio);
	}
}

