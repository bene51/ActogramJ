package actoj.gui;

import actoj.core.Actogram;
import actoj.io.ActogramReader;
import actoj.gui.ActogramProcessor;
import actoj.gui.ATreeSelectionListener;

import ij.process.ImageProcessor;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;


public class CustomWindow extends JFrame
		implements ATreeSelectionListener, ActogramCanvas.Feedback {

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
		getContentPane().add(new ActoToolBar(this), BorderLayout.NORTH);
		getContentPane().add(status, BorderLayout.SOUTH);
		pack();
		setVisible(true);
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

