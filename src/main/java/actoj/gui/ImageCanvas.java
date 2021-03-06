package actoj.gui;

import ij.IJ;
import ij.gui.GenericDialog;

import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JPanel;

import actoj.activitypattern.OnOffset;
import actoj.core.Actogram;
import actoj.core.TimeInterval;
import actoj.core.TimeInterval.Units;

@SuppressWarnings("serial")
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
	private float lLimit = 0f;
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
		zoomf = Zoom.LEVELS[zoom.getZoomIndex()];

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

	public void calculateAcrophase() {
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

		new Thread() {
			@Override
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection()) {
						try {
							ac.calculateAcrophase();
						} catch(Exception e) {
							IJ.error(e.getClass() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	public void calculateOnAndOffsets() {
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

		GenericDialog gd = new GenericDialog("Calculate on- and offsets");
		gd.addNumericField("Smooting gaussian std dev", 5, 4, 6, a.unit.toString());
		gd.addChoice("Threshold", OnOffset.thresholdMethods, OnOffset.thresholdMethods[OnOffset.ThresholdMethod.MedianWithoutZero.ordinal()]);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		final float sigma = (float)gd.getNextNumber();
		final OnOffset.ThresholdMethod thresholdMethod = OnOffset.ThresholdMethod.values()[gd.getNextChoiceIndex()];
		final boolean isManualThreshold = thresholdMethod == OnOffset.ThresholdMethod.Manual;
		float threshold = 0;
		if(isManualThreshold) {
			threshold = (float)IJ.getNumber("Manual threshold", threshold);
			if(threshold == IJ.CANCELED)
				return;
		}
		final float manualThreshold = threshold;
		new Thread() {
			@Override
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection()) {
						try {
							if(isManualThreshold)
								ac.calculateOnAndOffsets(sigma, manualThreshold);
							else
								ac.calculateOnAndOffsets(sigma, thresholdMethod);
						} catch(Exception e) {
							IJ.error(e.getClass() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
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

		float period = a.SAMPLES_PER_PERIOD * a.interval.intervalIn(a.unit);
		float sigma = 0;

		GenericDialog gd = new GenericDialog("Create Average Activity Pattern");
		gd.addNumericField("Period", period, 0, 6, a.unit.toString());
		gd.addNumericField("Smooting gaussian std dev", sigma, 0, 6, a.unit.toString());
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		final float p = (float)gd.getNextNumber();
		final float s = (float)gd.getNextNumber();
		final TimeInterval pi = new TimeInterval(p, a.unit);
		new Thread() {
			@Override
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

		float per = a.SAMPLES_PER_PERIOD * a.interval.intervalIn(a.unit);
		int fromPeriod = Math.round(per - per / 3);
		int toPeriod = Math.round(per + per / 3);
		int nPeaks = 1;
		int methodIdx = 0;
		int stepsize = 1;
		float sigma = 0f;
		double pLevel = 0.05;

		GenericDialog gd = new GenericDialog("Create Periodogram");
		String[] methods = new String[] {
			"Fourier", "Chi-Square", "Lomb-Scargle" };
		gd.addChoice("Method", methods, methods[methodIdx]);
		Vector<?> v = gd.getChoices();
		final Choice c = (Choice)v.get(v.size() - 1);
		gd.addNumericField("from_period", fromPeriod, 0, 6, a.unit.toString());
		gd.addNumericField("to_period", toPeriod, 0, 6, a.unit.toString());
		gd.addNumericField("Number of peaks", nPeaks, 0);
		gd.addNumericField("Smoothing gaussian std dev", sigma, 2);
		gd.addNumericField("Step size", stepsize, 0, 6, "samples");
		gd.addNumericField("p level", pLevel, 3);
		v = gd.getNumericFields();
		final TextField tf = (TextField)v.get(v.size() - 1);
		c.addItemListener(new ItemListener() {
			@Override
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
		final float sig = (float)gd.getNextNumber();
		final int steps = (int)gd.getNextNumber();
		final double pV = gd.getNextNumber();
		final TimeInterval fi = new TimeInterval(fp, a.unit);
		final TimeInterval ti = new TimeInterval(tp, a.unit);
		new Thread() {
			@Override
			public void run() {
				for(ActogramCanvas ac : actograms) {
					if(ac.hasSelection()) {
						try {
							ac.calculatePeriodogram(fi,
								ti, m, np, sig, steps, pV);
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

	public float getLowerLimit() {
		return lLimit;
	}

	public void setLowerLimit(float lLimit) {
		if(this.lLimit != lLimit) {
			this.lLimit = lLimit;
			update();
		}
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

	public void set(int ppl, float uLimit, float lLimit, int maxColumns, int nSubdivisions, float whRatio, Units fpUnits) {
		this.ppl = ppl;
		this.uLimit = uLimit;
		this.lLimit = lLimit;
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
					a, zoomf, uLimit, lLimit, ppl,
					nSubdivisions, whRatio,
					fpUnits, feedback));
		}
		clear();
		addAll(ac);
		invalidate();
		synchronized(getTreeLock()) {
			validateTree();
		}
		getParent().doLayout();
		repaint();
	}

	public void update() {
		ArrayList<ActogramCanvas> ac = new ArrayList<ActogramCanvas>();
		for(ActogramCanvas old : actograms)
			ac.add(new ActogramCanvas(
				old.processor.original, zoomf, uLimit, lLimit, ppl,
				nSubdivisions, whRatio, fpUnits, feedback));

		clear();
		addAll(ac);

		invalidate();
		synchronized(getTreeLock()) {
			validateTree();
		}
		getParent().doLayout();
		repaint();
	}
}

