package actoj.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import ij.IJ;

/**
 * Representation of the data of one actogram.
 */
public class Actogram {

	/**
	 * A name for this actogram.
	 */
	public final String name;

	/**
	 * The raw data of this actogram.
	 */
	private float[] data;

	/**
	 * Measurements per period.
	 * The period may e.g. be one day. Having an measurements' interval
	 * of 1 minute, SAMPLES_PER_PERIOD would be 1440.
	 */
	public final int SAMPLES_PER_PERIOD;

	/**
	 * The time interval between individual measurements.
	 */
	public final TimeInterval interval;

	/**
	 * The units of the time interval.
	 */
	public final TimeInterval.Units unit;

	/**
	 * Array of external variables.
	 */
	private ExternalVariable[] externals;

	/**
	 * A list of marker sets.
	 */
	private ArrayList<MarkerList> markers = new ArrayList<MarkerList>();


	/**
	 * Constructur.
	 */
	public Actogram(String name, Actogram a) {
		this.name = name;
		this.data = new float[a.data.length];
		System.arraycopy(a.data, 0, this.data, 0, a.data.length);
		this.interval = a.interval;
		this.SAMPLES_PER_PERIOD = a.SAMPLES_PER_PERIOD;
		this.unit = a.unit;
		this.externals = new ExternalVariable[0];
	}

	/**
	 * Constructur.
	 */
	public Actogram(String name, float[] data, int SPP, TimeInterval interval, TimeInterval.Units unit) {
		this.name = name;
		this.data = data;
		this.interval = interval;
		this.SAMPLES_PER_PERIOD = SPP;
		this.unit = unit;
		this.externals = new ExternalVariable[0];
	}

	public void addExternalVariable(ExternalVariable v) {
		int l = externals.length;
		ExternalVariable[] tmp = new ExternalVariable[l + 1];
		System.arraycopy(externals, 0, tmp, 0, l);
		tmp[l] = v;
		this.externals = tmp;
	}

	public void removeExternalVariable(int idx) {
		int l = externals.length;
		if(idx < 0 || idx >= l)
			throw new IllegalArgumentException(idx + " out of range");
		ExternalVariable[] tmp = new ExternalVariable[l - 1];
		System.arraycopy(externals, 0, tmp, 0, idx);
		System.arraycopy(externals, idx + 1, tmp, idx, l - idx - 1);
		this.externals = tmp;
	}

	public ExternalVariable[] getExternalVariables() {
		return externals;
	}

	public void setExternalVariables(ExternalVariable[] ext) {
		this.externals = ext;
	}

	public void addMarker(MarkerList m) {
		markers.add(m);
	}

	public MarkerList getMarker(int i) {
		return markers.get(i);
	}

	public int nMarkers() {
		return markers.size();
	}

	public void removeMarker(int i) {
		markers.remove(i);
	}

	public void replaceMarker(int i, MarkerList ml) {
		markers.set(i, ml);
	}

	public void exportMarkers(String path) throws IOException {
		if(this.markers.size() == 0) {
			IJ.error("No markers");
			return;
		}
		TimeInterval T = new TimeInterval(this.SAMPLES_PER_PERIOD * this.interval.millis);

		int nRows = 0;
		int nCols = this.markers.size() + 1;

		for(MarkerList m : this.markers) {
			double pos = m.getCalibration() * m.getPosition(m.size() - 1);
			int period = (int) Math.floor(pos / T.millis);
			if (period > nRows)
				nRows = period;
		}
		nRows++;

		String[][] data = new String[nRows][nCols];

		for(int markerListIndex = 0; markerListIndex < this.markers.size(); markerListIndex++) {
			MarkerList ml = this.markers.get(markerListIndex);
			for(int posI : ml) {
				double pos = ml.getCalibration() * posI;
				int period = (int) Math.floor(pos / T.millis);
				double millisWithinPeriod = pos - period * T.millis;
				data[period][markerListIndex + 1] = (new TimeInterval(millisWithinPeriod)).toString();
				data[period][0] = Integer.toString(period);
			}
		}

		PrintStream out = new PrintStream(new FileOutputStream(path));

		out.print("Period");
		for (MarkerList m : this.markers)
			out.print("\t" + m.getName());
		out.println();

		for (int row = 0; row < data.length; row++) {
			if (data[row][0] != null) {
				out.print(data[row][0]);
				for (int col = 1; col < (data[row]).length; col++) {
					String s = data[row][col];
					if (s == null)
						s = "*";
					out.print("\t" + s);
				}
				out.println();
			}
		}
		out.close();
	}

	/**
	 * Returns a reference to the data array.
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * Returns the data at the specified index.
	 */
	public float get(int idx) {
		return data[idx];
	}

	/**
	 * Returns the size of the data array.
	 */
	public int size() {
		return data.length;
	}

	/**
	 * Returns the index for a given time interval.
	 */
	public int getIndexForTime(TimeInterval time) {
		return (int)Math.round(time.millis / interval.millis);
	}

	/**
	 * Returns the time for the specified index.
	 * This is the start time of the corresponding interval.
	 */
	public TimeInterval getTimeForIndex(int idx) {
		return new TimeInterval(idx * interval.millis);
	}

	/**
	 * Returns a String representation for the time for the
	 * given index.
	 */
	public String getTimeStringForIndex(int idx) {
		return getTimeForIndex(idx).toString();
	}

	/**
	 * Calls downsample(2).
	 */
	public Actogram downsample() {
		return downsample(2);
	}

	public Actogram downsample(double factor) {
		if(factor == (int)(factor))
			return downsampleInt((int)factor);
		return downsampleDouble(factor);
	}

	/**
	 * Downsamples the data array by the given factor, and thus returns
	 * a new actogram with length original_length / f;
	 * Calculates for each entry in the new array the average of the
	 * corresponding f entries in the old array;
	 * Consequently, the SAMPLES_PER_PERIOD variable of the new actogram
	 * is the old value, devided by f, and the interval variable of the
	 * new actogram is the old value, multiplied by f;
	 * if the number of measurements is not devidable by the given factor,
	 * the last measurements is cut off.
	 */
	public Actogram downsampleInt(int f) {
		if(SAMPLES_PER_PERIOD % f != 0)
			throw new IllegalArgumentException("Invalid zoom factor: " + f);

		int newlength = data.length / f;
		float[] newdata = new float[newlength];
		for(int i = 0; i < newlength; i++) {
			newdata[i] = 0;
			int offs = f * i;
			for(int j = 0; j < f; j++)
				newdata[i] += data[offs + j];
			newdata[i] /= f;
		}
		return new Actogram(name, newdata, SAMPLES_PER_PERIOD / f, interval.mul(f), unit);
	}

	public Actogram downsampleDouble(double factor) {
		double d = Math.IEEEremainder(SAMPLES_PER_PERIOD, factor);
		if(Math.abs(d) > 10e-6)
			throw new IllegalArgumentException("Invalid zoom factor: " + factor);

		int l = (int)Math.ceil(data.length / factor);
		float[] newdata = new float[l];

		double[] cumOld = new double[data.length + 1];
		cumOld[0] = 0;
		for(int i = 0; i < data.length; i++)
			cumOld[i + 1] = cumOld[i] + data[i];

		double[] cumNew = new double[l + 1];
		cumNew[0] = 0;
		for(int newIdx = 0; newIdx < l; newIdx++) {
			int uInt = (int)Math.floor((newIdx + 1) * factor);
			double partialOver = (newIdx + 1) * factor - uInt;
			double c = uInt >= cumOld.length ? cumOld[cumOld.length - 1] : cumOld[uInt];
			c -= cumNew[newIdx];
			if(partialOver > 10e-6 && uInt < data.length)
				c += partialOver * data[uInt];
			newdata[newIdx] = (float)(c / factor);
			cumNew[newIdx + 1] = cumNew[newIdx] + c;
		}

		return new Actogram(name, newdata,
			(int)Math.round(SAMPLES_PER_PERIOD / factor),
			new TimeInterval(interval.millis * factor),
			unit);
	}

	/**
	 * Calculates the sum of the specified actograms.
	 * @throws IllegalArgumentException if the given actograms don't agree in
	 *         SAMPLES_PER_PERIOD, interval and interval unit.
	 */
	public static Actogram sum(Collection<Actogram> actograms) {
		// TODO start time?
		int n = 0;
		int spp = -1;
		TimeInterval interval = null;
		TimeInterval.Units unit = null;
		boolean first = true;
		for(Actogram a : actograms) {
			// check interval
			if(first) {
				spp = a.SAMPLES_PER_PERIOD;
				interval = a.interval;
				first = false;
				unit = a.unit;
			}

			if(a.SAMPLES_PER_PERIOD != spp)
				throw new IllegalArgumentException("Given actograms don't have the same number of samples per period");
			if(!a.interval.equals(interval))
				throw new IllegalArgumentException("Given actograms don't have the same interval duration");
			if(!a.unit.equals(unit))
				throw new IllegalArgumentException("Given actograms don't have the same units");

			int count = a.data.length;
			if(count > n)
				n = count;
		}
		float[] res = new float[n];
		for(Actogram a : actograms) {
			for(int i = 0; i < a.data.length; i++)
				res[i] += a.data[i];
		}
		return new Actogram("#sum", res, spp, interval, unit);
	}

	/**
	 * Devides each data value by the given value.
	 */
	public static void devide(Actogram actogram, float s) {
		for(int i = 0; i < actogram.data.length; i++)
			actogram.data[i] /= s;
	}

	/**
	 * Multiplies each data value with the given value.
	 */
	public static void multiply(Actogram actogram, float s) {
		for(int i = 0; i < actogram.data.length; i++)
			actogram.data[i] *= s;
	}

	/**
	 * Calculates the sum of all data values.
	 */
	public static float sum(Actogram actogram) {
		double s = 0.0;
		for(int i = 0; i < actogram.data.length; i++)
			s += actogram.data[i];
		return (float)s;
	}


	/**
	 * Convenience method, equivalent to
	 * devide(sum(actograms), actograms.size()).
	 */
	public static Actogram average(Collection<Actogram> actograms) {
		Actogram res = sum(actograms);
		devide(res, actograms.size());
		return new Actogram("#average", res);
	}

	/**
	 * Convolve the actogram with the given kernel.
	 */
	public Actogram convolve(float[] kernel) {
		int dl = data.length;
		int kl = kernel.length;

		float[] n = new float[data.length];

		for(int i = 0; i < dl; i++) {
			n[i] = 0;
			for(int j = 0; j < kl; j++) {
				int idx = i + j - kl/2;
				float v = (idx < 0 || idx >= dl) ? 0 : data[idx];
				n[i] += kernel[j] * v;
			}
		}
		return new Actogram(name + "_smoothed", n,
			SAMPLES_PER_PERIOD, interval, unit);
	}

	/**
	 * Returns the name of this actogram.
	 */
	@Override
	public String toString() {
		return name;
	}

	public static void main(String[] args) {
		System.out.println("before");
		float[] ex = new float[] {
			0, 1, 2, 3, 4, 5 , 6, 7, 8, 9, 10, 11, 12, 13 };
		for(int i = 0; i < ex.length; i++)
			System.out.print(ex[i] + " ");
		System.out.println();

		Actogram a = new Actogram("lkj", ex, 14, // 6,
			new TimeInterval(1, TimeInterval.Units.MINUTES),
			TimeInterval.Units.MINUTES);
		a = a.downsample(1.75);

		for(int i = 0; i < a.data.length; i++)
			System.out.print(a.data[i] + " ");
		System.out.println();
	}
}

