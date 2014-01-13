package actoj.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import actoj.fitting.FitPeriodicLine;

public class MarkerList implements Iterable<Integer> {

	private final String name;

	private final ArrayList<Integer> positions;

	private Color color;

	private final double calibration;

	private RegressionLine regression = null;

	private float linewidth = 2;

	private int indexInPlotPerLine = 1;

	public interface MarkerChangeListener {
		public void markerChanged(MarkerList m);
	}

	private ArrayList<MarkerChangeListener> listeners = new ArrayList<MarkerChangeListener>();

	public void addMarkerChangeListener(MarkerChangeListener l) {
		listeners.add(l);
	}

	public void removeMarkerChangeListener(MarkerChangeListener l) {
		listeners.remove(l);
	}

	private void fireMarkerChanged() {
		for(MarkerChangeListener l : listeners)
			l.markerChanged(this);
	}

	public MarkerList(MarkerList m) {
		this.name = m.name;
		this.positions = new ArrayList<Integer>(m.positions);
		this.color = m.color;
		this.calibration = m.calibration;
		this.regression = m.regression;
		this.linewidth = m.linewidth;
		this.indexInPlotPerLine = m.indexInPlotPerLine;
	}

	public MarkerList(String name, ArrayList<Integer> positions, double calibration, Color color) {
		this.name = name;
		this.positions = positions;
		this.calibration = calibration;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		fireMarkerChanged();
	}

	public float getLinewidth() {
		return linewidth;
	}

	public void setLinewidth(float linewidth) {
		this.linewidth = linewidth;
		fireMarkerChanged();
	}

	public int getPosition(int i) {
		return positions.get(i);
	}

	public double getCalibration() {
		return calibration;
	}

	public int getIndexInPlotPerLine() {
		return indexInPlotPerLine;
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		this.positions.clear();
		this.positions.addAll(positions);
		regression = null;
		fireMarkerChanged();
	}

	public void setIndexInPlotPerLine(int indexInPlotPerLine) {
		this.indexInPlotPerLine = indexInPlotPerLine;
		fireMarkerChanged();
	}

	public void removePosition(int i) {
		this.positions.remove(i);
		regression = null;
		fireMarkerChanged();
	}

	public int size() {
		return positions.size();
	}

	@Override
	public Iterator<Integer> iterator() {
		return positions.iterator();
	}

	public void invalidateRegression() {
		regression = null;
		fireMarkerChanged();
	}

	public RegressionLine getRegression() {
		return regression;
	}

	public void calculateRegressionOld(TimeInterval T) {

		@SuppressWarnings("unused")
		double x, y, x2, y2, xy;
		x = y = x2 = y2 = xy = 0;

		int n = positions.size();

		for(int p = 0; p < n; p++) {
			double pos = calibration * positions.get(p);
			double period = Math.floor(pos / T.millis);
			double millisWithinPeriod = pos - period * T.millis;

			x += period;
			y += millisWithinPeriod;
			x2 += period * period;
			y2 += millisWithinPeriod * millisWithinPeriod;
			xy += period * millisWithinPeriod;
		}


		double m = (n * xy - x * y) / (n * x2 - x * x);
		double t = (y - m * x) / n;
		System.out.println("m = " + m + " t = " + t);
		int firstPeriod = (int)Math.floor(calibration * positions.get(0) / T.millis);
		int lastPeriod = (int)Math.floor(calibration * positions.get(n - 1) / T.millis);

		regression = new RegressionLine(m, t, firstPeriod, lastPeriod);
		fireMarkerChanged();
	}

	public void calculateRegression(TimeInterval T) {

		int n = positions.size();
		double[] x = new double[n];
		double[] y = new double[n];

		for(int p = 0; p < n; p++) {
			double pos = calibration * positions.get(p);
			double period = Math.floor(pos / T.millis);
			double millisWithinPeriod = pos - period * T.millis;

			x[p] = period;
			y[p] = millisWithinPeriod;
		}

		double[] param = FitPeriodicLine.fitPeriodicLine(x, y, T.millis);
		double m = param[FitPeriodicLine.SLOPE];
		double t = param[FitPeriodicLine.INTERCEPT];

		// calculate shifted y values and use them for a final ordinary linear regression:
		double sumX, sumY, sumX2, sumXY;
		sumX = sumY = sumX2 = sumXY = 0;
		for(int p = 0; p < n; p++) {
			double yi = t + m * x[p];
			double xp = x[p];
			double yp = y[p];
			while(yi < 0) {
				yi += T.millis;
				yp -= T.millis;
			}
			while(yi >= T.millis){
				yi -= T.millis;
				yp += T.millis;
			}

			sumX  += xp;
			sumY  += yp;
			sumX2 += xp * xp;
			sumXY += xp * yp;
		}
		m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
		t = (sumY - m * sumX) / n;

		System.out.println("m = " + m + " t = " + t);

		int firstPeriod = (int)Math.floor(calibration * positions.get(0) / T.millis);
		int lastPeriod = (int)Math.floor(calibration * positions.get(n - 1) / T.millis);

		regression = new RegressionLine(m, t, firstPeriod, lastPeriod);
		fireMarkerChanged();
	}

	@Override
	public String toString() {
		return name;
	}

	public static class RegressionLine {
		public final double m;
		public final double t;
		public final int firstPeriod;
		public final int lastPeriod;

		public RegressionLine(double m, double t, int firstPeriod, int lastPeriod) {
			this.m = m;
			this.t = t;
			this.firstPeriod = firstPeriod;
			this.lastPeriod = lastPeriod;
		}
	}
}
