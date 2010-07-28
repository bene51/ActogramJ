package actoj.gui;

import ij.gui.GenericDialog;

import actoj.core.Actogram;

import java.util.ArrayList;
import java.util.List;

public class CalculateDialog {

	public static final void run(CustomWindow win) {

		GenericDialog gd = new GenericDialog("Calculate");
		String[] ops = new String[] {"Average", "Sum"};
		gd.addChoice("Operation", ops, ops[0]);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		int idx = gd.getNextChoiceIndex();
		ArrayList<ActogramCanvas> acs;
		List<Actogram> actograms;
		Actogram result;
		switch(idx) {
			case 0:
				acs = win.canvas.getActograms();
				actograms = new ArrayList<Actogram>();
				for(ActogramCanvas ac : acs)
					actograms.add(ac.processor.original);
				result = Actogram.average(actograms);
				win.tree.addCalculated(result);
				break;
			case 1:
				acs = win.canvas.getActograms();
				actograms = new ArrayList<Actogram>();
				for(ActogramCanvas ac : acs)
					actograms.add(ac.processor.original);
				result = Actogram.sum(actograms);
				win.tree.addCalculated(result);
				break;
		}
	}
}

