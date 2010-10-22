package actoj.gui;

import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.ButtonGroup;

import actoj.gui.actions.*;
import actoj.gui.TreeView;

public class ActoToolBar extends JToolBar {

	public ActoToolBar(CustomWindow win) {
		add(new OpenAction(win.tree));
		add(new ExportPDF(win.canvas));
		add(new PropertiesAction(win));
		add(new CalculateAction(win));

		addSeparator();

		ButtonGroup bg = new ButtonGroup();
		JToggleButton button;

		button = new JToggleButton(new PointerAction(win.canvas));
		add(button);
		bg.add(button);
		button.setSelected(true);
		button = new JToggleButton(new SelectingAction(win.canvas));
		add(button);
		bg.add(button);
		button = new JToggleButton(new CalibAction(win.canvas));
		add(button);
		bg.add(button);

		addSeparator();

		button = new JToggleButton(new FittingAction(win.canvas));
		add(button);
		bg.add(button);
	}
}

