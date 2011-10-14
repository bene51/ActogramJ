package actoj.gui;

import ij.gui.GenericDialog;
import actoj.core.TimeInterval.Units;

public class PropertiesDialog {

	public static final void changeProperties(CustomWindow win) {
		ImageCanvas ic = win.canvas;
		int ppl  = ic.getPeriodsPerLine();
		float ul = ic.getUpperLimit();
		float ll = ic.getLowerLimit();
		int cols = ic.getMaxColumns();
		int subd = ic.getCalibrationSubdivisions();
		float whRatio = ic.getWHRatio();

		GenericDialog gd = new GenericDialog("Edit Properties");
		gd.addNumericField("Number of plots", ppl, 0);
		gd.addNumericField("Upper limit", ul, 1);
		gd.addNumericField("Lower limit", ll, 1);
		gd.addNumericField("Max. number of columns on sheet", cols, 0);
		gd.addNumericField("Number of ticks in the calibration bar", subd, 0);
		gd.addNumericField("Ratio w:h", whRatio, 2);
		String[] units = new String[Units.values().length];
		for(int i = 0; i < units.length; i++)
			units[i] = Units.values()[i].toString();
		String u = Units.HOURS.toString();
		gd.addChoice("Unit of freerunning period", units, u);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		ppl  = (int)gd.getNextNumber();
		ul   = (float)gd.getNextNumber();
		ll   = (float)gd.getNextNumber();
		cols = (int)gd.getNextNumber();
		subd = (int)gd.getNextNumber();
		whRatio = (float)gd.getNextNumber();
		Units fpUnit = Units.values()[
			gd.getNextChoiceIndex()];

		ic.set(ppl, ul, ll, cols, subd, whRatio, fpUnit);
	}
}

