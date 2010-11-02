package actoj.gui;

import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.ButtonGroup;

import actoj.gui.actions.*;
import actoj.gui.TreeView;

public class ActoToolBar extends JToolBar implements ModeChangeListener {

	public JToggleButton pointer, selection, calibration;

	public ActoToolBar(CustomWindow win) {
		add(new OpenAction(win.tree));
		add(new ExportPDF(win.canvas));
		add(new PropertiesAction(win));
		add(new CalculateAction(win));

		addSeparator();

		ButtonGroup bg = new ButtonGroup();

		pointer = new JToggleButton(new PointerAction(win.canvas));
		pointer.setLabel("");
		add(pointer);
		bg.add(pointer);
		pointer.setSelected(true);
		selection = new JToggleButton(new SelectingAction(win.canvas));
		selection.setLabel("");
		add(selection);
		bg.add(selection);
		calibration = new JToggleButton(new CalibAction(win.canvas));
		calibration.setLabel("");
		add(calibration);
		bg.add(calibration);

		addSeparator();

		add(new FittingAction(win.canvas));
		add(new NormalizeAction(win));
	}

	public void modeChanged(ActogramCanvas.Mode mode) {
		JToggleButton b = null;
		switch(mode) {
			case POINTING: b = pointer; break;
			case FREERUNNING_PERIOD: b = calibration; break;
			case SELECTING: b = selection; break;
		}
		b.setSelected(true);
	}
}

