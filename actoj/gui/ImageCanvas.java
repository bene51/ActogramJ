package actoj.gui;

import actoj.core.Actogram;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import java.awt.*;

import ij.IJ;
import ij.gui.GenericDialog;

public class ImageCanvas extends JPanel {

	public static final int PADDING = 20;

	private ArrayList<ActogramCanvas> actograms =
			new ArrayList<ActogramCanvas>();

	private int maxColumns = 4;

	private int nRows = 0;
	private int nCols = 0;

	private int nSubdivisions = 8;
	final Zoom zoom;
	private int zoomf;
	private float uLimit = 1f;
	private int ppl = 2;
	private float whRatio = 2f / 3f;

	private ActogramCanvas.Feedback feedback;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();

	public ImageCanvas(ActogramCanvas.Feedback feedback) {
		this.feedback = feedback;
		zoom = new Zoom(this);
		zoomf = zoom.LEVELS[zoom.getZoomIndex()];

		setLayout(gridbag);
		c.gridx = maxColumns - 1; c.gridy = -1;
		c.insets = new Insets(PADDING, PADDING, PADDING, PADDING);
	}

	public ArrayList<ActogramCanvas> getActograms() {
		return actograms;
	}

	public int getRows() {
		return nRows;
	}

	public int getCols() {
		return nCols;
	}

	public int getMaxColumns() {
		return maxColumns;
	}

	public void calculatePeriodogram() {
		ActogramCanvas first = null;
		for(ActogramCanvas ac : actograms) {
			if(ac.hasSelection()) {
				first = ac;
				break;
			}
		}
		if(first == null) {
			IJ.error("Selection required");
			return;
		}
		Actogram a = first.processor.original;

		int fromPeriod = a.SAMPLES_PER_PERIOD / 2;
		int toPeriod = a.SAMPLES_PER_PERIOD * 2;
		int nPeaks = 3;
		int methodIdx = 0;

		GenericDialog gd = new GenericDialog("Create Periodogram");
		String[] methods = new String[] {
			"Fourier", "Enright", "Lomb-Scargle" };
		gd.addChoice("Method", methods, methods[methodIdx]);
		gd.addNumericField("from_period", fromPeriod, 0, 6, "samples");
		gd.addNumericField("to_period", toPeriod, 0, 6, "samples");
		gd.addNumericField("Number of peaks", nPeaks, 0);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		final int m  = gd.getNextChoiceIndex();
		final int fp = (int)gd.getNextNumber();
		final int tp = (int)gd.getNextNumber();
		final int np = (int)gd.getNextNumber();
		new Thread() {
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection())
						ac.calculatePeriodogram(
							fp, tp, m, np);
				}
			}
		}.start();
	}

	public void setCanvasMode(ActogramCanvas.Mode mode) {
		for(ActogramCanvas ac : actograms)
			ac.setMode(mode);
	}

	public void setMaxColumns(int n) {
		if(this.maxColumns != n) {
			this.maxColumns = n;
			update();
		}
	}

	public void setCalibrationSubdivisions(int n) {
		this.nSubdivisions = n;
		for(ActogramCanvas c : actograms)
			c.setNSubdivisions(n);
	}

	public int getCalibrationSubdivisions() {
		return nSubdivisions;
	}

	public void setZoom(int zoom) {
		if(this.zoomf != zoom) {
			this.zoomf = zoom;
			update();
		}
	}

	public Zoom getZoom() {
		return zoom;
	}

	public int getPeriodsPerLine() {
		return ppl;
	}

	public void setPeriodsPerLine(int ppl) {
		if(this.ppl != ppl) {
			this.ppl = ppl;
			update();
		}
	}

	public float getUpperLimit() {
		return uLimit;
	}

	public void setUpperLimit(float uLimit) {
		if(this.uLimit != uLimit) {
			this.uLimit = uLimit;
			update();
		}
	}

	public float getWHRatio() {
		return whRatio;
	}

	public void setWHRatio(float whRatio) {
		if(this.whRatio != whRatio) {
			this.whRatio = whRatio;
			update();
		}
	}

	public void set(int ppl, float uLimit, int maxColumns, int nSubdivisions, float whRatio) {
		this.ppl = ppl;
		this.uLimit = uLimit;
		this.maxColumns = maxColumns;
		this.nSubdivisions = nSubdivisions;
		this.whRatio = whRatio;
		update();
	}

	public void addActogram(ActogramCanvas a) {
		actograms.add(a);
		c.gridx++;
		if(nCols < maxColumns)
			nCols++;

		if(c.gridx == maxColumns) {
			c.gridx = 0;
			c.gridy++;
			nRows++;
		}
		gridbag.setConstraints(a, c);
		add(a);
	}

	public void clear() {
		actograms.clear();
		removeAll();
		c.gridx = maxColumns - 1;
		c.gridy = -1;
		nRows = nCols = 0;
	}

	public void addAll(Collection<ActogramCanvas> a) {
		for(ActogramCanvas ac : a)
			addActogram(ac);
	}
	
	public void display(java.util.List<Actogram> selected) {
		HashMap<Actogram, ActogramCanvas> displayed
			= new HashMap<Actogram, ActogramCanvas>();
		for(ActogramCanvas a : actograms)
			displayed.put(a.processor.original, a);

		ArrayList<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(Actogram a : selected) {
			if(displayed.containsKey(a))
				ac.add(displayed.get(a));
			else
				ac.add(new ActogramCanvas(
					a, zoomf, uLimit, ppl,
					nSubdivisions, whRatio, feedback));
		}
		clear();
		addAll(ac);
		invalidate();
		validateTree();
		getParent().doLayout();
	}

	public void update() {
		ArrayList<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(ActogramCanvas old : actograms)
			ac.add(new ActogramCanvas(
				old.processor.original, zoomf, uLimit, ppl,
				nSubdivisions, whRatio, feedback));

		clear();
		addAll(ac);

		invalidate();
		validateTree();
		getParent().doLayout();
	}
}

