package actoj.gui;

import actoj.core.Actogram;
import actoj.io.ActogramReader;
import actoj.gui.ActogramProcessor;
import actoj.gui.ATreeSelectionListener;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

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
	final Zoom zoom;
	final StatusBar status;

	private int zoomf;
	private float uLimit = 1f;
	private int ppl = 2;

	public CustomWindow() {
		super("ActoJ");
		setPreferredSize(new Dimension(800, 600));

		canvas = new ImageCanvas();
		tree = new TreeView();
		zoom = new Zoom(this);
		status = new StatusBar(this);

		zoomf = zoom.LEVELS[zoom.getZoomIndex()];

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

	public Zoom getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoomf = zoom;
		updateCanvas();
	}

	public int getPeriodsPerLine() {
		return ppl;
	}

	public void setPeriodsPerLine(int ppl) {
		this.ppl = ppl;
		updateCanvas();
	}

	public float getUpperLimit() {
		return uLimit;
	}

	public void setUpperLimit(float uLimit) {
		this.uLimit = uLimit;
		updateCanvas();
	}

	public int getNumColumns() {
		return canvas.getMaxColumns();
	}

	public void setNumColumns(int n) {
		canvas.setMaxColumns(n);
		updateCanvas();
	}

	public void updateCanvas() {
		canvas.clear();

		List<Actogram> selected = tree.getSelected();
		if(selected.size() == 0)
			return;

		List<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(Actogram a : selected)
			ac.add(new ActogramCanvas(
				a, zoomf, uLimit, ppl, this));

		canvas.addAll(ac);
		invalidate();
		validateTree();
		doLayout();
	}

	public void selectionChanged() {
		List<Actogram> selected = tree.getSelected();
		if(selected.size() == 0)
			return;

		HashMap<Actogram, ActogramCanvas> displayed
			= new HashMap<Actogram, ActogramCanvas>();
		for(ActogramCanvas a : canvas.getActograms())
			displayed.put(a.processor.original, a);

		canvas.clear();
		List<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(Actogram a : selected) {
			if(displayed.containsKey(a))
				ac.add(displayed.get(a));
			else
				ac.add(new ActogramCanvas(
					a, zoomf, uLimit, ppl, this));
		}
		canvas.addAll(ac);
		invalidate();
		validateTree();
		doLayout();
	}

	public void positionChanged(String pos) {
		status.setPositionString(pos);
	}

	public void periodChanged(String per) {
		status.setFreerunningPeriod(per);
	}
}

