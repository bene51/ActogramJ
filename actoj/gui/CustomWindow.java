package actoj.gui;

import actoj.core.Actogram;
import actoj.io.ActogramReader;
import actoj.gui.ActogramProcessor;
import actoj.gui.TreeViewListener;

import ij.process.ImageProcessor;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;


public class CustomWindow extends JFrame
		implements TreeViewListener, ActogramCanvas.Feedback {

	final ImageCanvas canvas;
	final TreeView tree;
	final StatusBar status;

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
		ActoMenuBar menubar = new ActoMenuBar(this);
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

	public void externalVariablesChanged() {
		canvas.update();
	}

	public void selectionChanged() {
		canvas.display(tree.getSelected());
	}

	public void positionChanged(String pos) {
		status.setPositionString(pos);
	}

	public void periodChanged(String per) {
		status.setFreerunningPeriod(per);
	}
}

