package actoj.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class CustomWindow extends JFrame
		implements TreeViewListener, ActogramCanvas.Feedback {

	final ImageCanvas canvas;
	final TreeView tree;
	final StatusBar status;
	final ActoMenuBar menubar;

	public CustomWindow() {
		super("ActoJ");
		setPreferredSize(new Dimension(800, 600));

		canvas = new ImageCanvas(this);
		tree = new TreeView();
		status = new StatusBar(this);

		tree.addTreeSelectionListener(this);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setContinuousLayout(true);
		split.setLeftComponent(new JScrollPane(tree));
		split.setRightComponent(new JScrollPane(canvas));
		split.setDividerLocation(200);

		getContentPane().add(split);
		ActoToolBar toolbar = new ActoToolBar(this);
		menubar = new ActoMenuBar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(status, BorderLayout.SOUTH);
		setJMenuBar(menubar);

		canvas.addModeChangeListener(toolbar);
		canvas.addModeChangeListener(menubar);
		pack();
		setVisible(true);
	}

	public ImageCanvas getCanvas() {
		return canvas;
	}

	public TreeView getTreeView() {
		return tree;
	}

	@Override
	public void externalVariablesChanged() {
		canvas.update();
	}

	@Override
	public void markersChanged() {
		canvas.repaint();
	}

	@Override
	public void selectionChanged() {
		canvas.display(tree.getSelected());
		canvas.setCanvasMode(menubar.getMode());
	}

	@Override
	public void positionChanged(String pos) {
		status.setPositionString(pos);
	}

	@Override
	public void periodChanged(String per) {
		status.setFreerunningPeriod(per);
	}
}

