package actoj.gui;

import javax.swing.JToolBar;

import actoj.gui.actions.*;
import actoj.gui.TreeView;

public class ActoToolBar extends JToolBar {

	public ActoToolBar(CustomWindow win) {
		add(new OpenAction(win.tree));
		add(new ExportPDF(win.canvas));
		add(new PropertiesAction(win));
	}
}

