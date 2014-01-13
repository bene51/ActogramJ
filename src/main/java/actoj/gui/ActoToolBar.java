package actoj.gui;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import actoj.gui.actions.AverageActivityAction;
import actoj.gui.actions.CalculateAction;
import actoj.gui.actions.CalibAction;
import actoj.gui.actions.ExportPDF;
import actoj.gui.actions.FittingAction;
import actoj.gui.actions.HelpAction;
import actoj.gui.actions.NormalizeAction;
import actoj.gui.actions.OpenAction;
import actoj.gui.actions.PointerAction;
import actoj.gui.actions.PropertiesAction;
import actoj.gui.actions.SelectingAction;

@SuppressWarnings("serial")
public class ActoToolBar extends JToolBar implements ModeChangeListener {

	public JToggleButton pointer, selection, calibration;

	public ActoToolBar(CustomWindow win) {
		add(makeButton(new OpenAction(win.tree)));
		add(makeButton(new ExportPDF(win.canvas)));
		add(makeButton(new PropertiesAction(win)));
		add(makeButton(new CalculateAction(win)));

		addSeparator();

		ButtonGroup bg = new ButtonGroup();

		pointer = new JToggleButton(new PointerAction(win.canvas));
		pointer.setText("");
		add(pointer);
		bg.add(pointer);
		pointer.setSelected(true);
		selection = new JToggleButton(new SelectingAction(win.canvas));
		selection.setText("");
		add(selection);
		bg.add(selection);
		calibration = new JToggleButton(new CalibAction(win.canvas));
		calibration.setText("");
		add(calibration);
		bg.add(calibration);

		addSeparator();

		add(makeButton(new FittingAction(win.canvas)));
		add(makeButton(new AverageActivityAction(win.canvas)));
		add(makeButton(new NormalizeAction(win)));

		addSeparator();
		add(makeButton(new HelpAction()));

	}

	private JButton makeButton(Action a) {
		JButton button = new JButton(a);
		button.setHideActionText(true);
		button.setBorderPainted(false);
		return button;
	}

	@Override
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

