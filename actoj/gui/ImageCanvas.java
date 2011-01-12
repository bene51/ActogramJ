package actoj.gui;

import actoj.core.Actogram;
import actoj.core.TimeInterval;
import actoj.core.TimeInterval.Units;

import java.util.Vector;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import java.awt.event.*;
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
	private double zoomf;
	private float uLimit = 1f;
	private int ppl = 2;
	private float whRatio = 2f / 3f;
	private Units fpUnits = Units.HOURS;

	private ActogramCanvas.Feedback feedback;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();

	private ArrayList<ModeChangeListener> listeners = new ArrayList<ModeChangeListener>();

	public ImageCanvas(ActogramCanvas.Feedback feedback) {
		this.feedback = feedback;
		zoom = new Zoom(this);
		zoomf = zoom.LEVELS[zoom.getZoomIndex()];

		setLayout(gridbag);
		c.gridx = maxColumns - 1; c.gridy = -1;
		c.insets = new Insets(PADDING, PADDING, PADDING, PADDING);
	}

	public void removeModeChangeListener(ModeChangeListener l) {
		listeners.remove(l);
	}

	public void addModeChangeListener(ModeChangeListener l) {
		listeners.add(l);
	}

	private void fireModeChanged(ActogramCanvas.Mode mode) {
		for(ModeChangeListener l : listeners)
			l.modeChanged(mode);
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

	public Actogram[] normalizeActograms() {
		int n = actograms.size();
		if(n < 2) {
			IJ.error("At least two actograms are required");
			return null;
		}
		String[] names = new String[n];
		for(int i = 0; i < n; i++)
			names[i] = actograms.get(i).processor.original.name;

		GenericDialog gd = new GenericDialog("Normalize actograms");
		gd.addChoice("Reference actogram: ", names, names[0]);
		gd.showDialog();
		if(gd.wasCanceled())
			return null;

		int refIdx = gd.getNextChoiceIndex();
		Actogram ref = actograms.get(refIdx).processor.original;
		Actogram[] res = new Actogram[n];
		float totalActivity = Actogram.sum(ref);
		for(int i = 0; i < n; i++) {
			Actogram c = actograms.get(i).processor.original;
			res[i] = new Actogram(c.name + "_normalized", c);
			if(i == refIdx)
				continue;
			float act = Actogram.sum(res[i]);
			float factor = totalActivity / act;
			Actogram.multiply(res[i], factor);
		}
		return res;
	}

	public void calculateAverageActivity() {
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

		int period = a.SAMPLES_PER_PERIOD;
		float sigma = 0;

		GenericDialog gd = new GenericDialog("Create Average Activity Pattern");
		gd.addNumericField("Period", period, 0, 6, a.unit.toString());
		gd.addNumericField("Smooting gaussian sigma", sigma, 0, 6, a.unit.toString());
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		final int p = (int)gd.getNextNumber();
		final float s = (int)gd.getNextNumber();
		final TimeInterval pi = new TimeInterval(p, a.unit);
		new Thread() {
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection()) {
						try {
							ac.calculateAverageActivity(pi, s);
						} catch(Exception e) {
							IJ.error(e.getClass() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
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
		float downsample = 1f;
		double pLevel = 0.05;

		GenericDialog gd = new GenericDialog("Create Periodogram");
		String[] methods = new String[] {
			"Fourier", "Chi-Square", "Lomb-Scargle" };
		gd.addChoice("Method", methods, methods[methodIdx]);
		Vector v = gd.getChoices();
		final Choice c = (Choice)v.get(v.size() - 1);
		gd.addNumericField("from_period", fromPeriod, 0, 6, a.unit.toString());
		gd.addNumericField("to_period", toPeriod, 0, 6, a.unit.toString());
		gd.addNumericField("Number of peaks", nPeaks, 0);
		gd.addNumericField("Downsampling factor", downsample, 2);
		gd.addNumericField("p level", pLevel, 3);
		v = gd.getNumericFields();
		final TextField tf = (TextField)v.get(v.size() - 1);
		c.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				tf.setEnabled(c.getSelectedIndex() != 0);
			}
		});
		tf.setEnabled(false);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		final int m  = gd.getNextChoiceIndex();
		final int fp = (int)gd.getNextNumber();
		final int tp = (int)gd.getNextNumber();
		final int np = (int)gd.getNextNumber();
		final float factor = (float)gd.getNextNumber();
		final double pV = gd.getNextNumber();
		final TimeInterval fi = new TimeInterval(fp, a.unit);
		final TimeInterval ti = new TimeInterval(tp, a.unit);
		new Thread() {
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection()) {
						try {
							ac.calculatePeriodogram(fi,
								ti, m, np, factor, pV);
						} catch(Exception e) {
							IJ.error(e.getClass() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	public void setCanvasMode(ActogramCanvas.Mode mode) {
		for(ActogramCanvas ac : actograms)
			ac.setMode(mode);
		fireModeChanged(mode);
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

	public void setZoom(double zoom) {
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

	public void set(int ppl, float uLimit, int maxColumns, int nSubdivisions, float whRatio, Units fpUnits) {
		this.ppl = ppl;
		this.uLimit = uLimit;
		this.maxColumns = maxColumns;
		this.nSubdivisions = nSubdivisions;
		this.whRatio = whRatio;
		this.fpUnits = fpUnits;
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
					nSubdivisions, whRatio,
					fpUnits, feedback));
		}
		clear();
		addAll(ac);
		invalidate();
		validateTree();
		getParent().doLayout();
		repaint();
	}

	public void update() {
		ArrayList<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(ActogramCanvas old : actograms)
			ac.add(new ActogramCanvas(
				old.processor.original, zoomf, uLimit, ppl,
				nSubdivisions, whRatio, fpUnits, feedback));

		clear();
		addAll(ac);

		invalidate();
		validateTree();
		getParent().doLayout();
		repaint();
	}
}

