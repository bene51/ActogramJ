package actoj.gui;

import actoj.gui.actions.*;

import javax.swing.*;

public class ActoMenuBar extends JMenuBar implements ModeChangeListener {

	private JCheckBoxMenuItem pointer, calibration, selection;

	public ActoMenuBar(CustomWindow win) {
		super();

		JMenu menu = new JMenu("File");
		menu.add(new OpenAction(win.tree));
		menu.add(new ExportPDF(win.canvas));
		add(menu);


		ButtonGroup bg = new ButtonGroup();
		menu = new JMenu("Edit");
		pointer = new JCheckBoxMenuItem(new PointerAction(win.canvas));
		menu.add(pointer);
		bg.add(pointer);
		pointer.setSelected(true);
		selection = new JCheckBoxMenuItem(new SelectingAction(win.canvas));
		menu.add(selection);
		bg.add(selection);
		calibration = new JCheckBoxMenuItem(new CalibAction(win.canvas));
		menu.add(calibration);
		bg.add(calibration);
		menu.addSeparator();
		menu.add(new PropertiesAction(win));
		add(menu);

		menu = new JMenu("Analyze");
		menu.add(new CalculateAction(win));
		menu.add(new NormalizeAction(win));
		menu.addSeparator();
		menu.add(new FittingAction(win.canvas));
		add(menu);
	}

	public void modeChanged(ActogramCanvas.Mode mode) {
		JCheckBoxMenuItem b = null;
		switch(mode) {
			case POINTING: b = pointer; break;
			case FREERUNNING_PERIOD: b = calibration; break;
			case SELECTING: b = selection; break;
		}
		b.setSelected(true);
	}
}

