package actoj.core;

import java.util.Collection;

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

	public ExternalVariable[] getExternalVariables() {
		return externals;
	}

	public void setExternalVariables(ExternalVariable[] ext) {
		this.externals = ext;
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
	public Actogram downsample(int f) {
		if(SAMPLES_PER_PERIOD % f != 0)
			return null;

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
		return new Actogram(name + "_convolved", n,
			SAMPLES_PER_PERIOD, interval, unit);
	}

	/**
	 * Returns the name of this actogram.
	 */
	public String toString() {
		return name;
	}
}

