package actoj.core;

import java.util.Collection;

public class Actogram {

	private float[] data;

	/**
	 * Measurements per day
	 */
	public final int SAMPLES_PER_PERIOD;

	public final String name;
	public final TimeInterval interval;
	public final TimeInterval.Units unit;

	public Actogram(String name, float[] data, int SPP, TimeInterval interval, TimeInterval.Units unit) {
		this.name = name;
		this.data = data;
		this.interval = interval;
		this.SAMPLES_PER_PERIOD = SPP;
		this.unit = unit;
	}

	public float[] getData() {
		return data;
	}

	public float get(int idx) {
		return data[idx];
	}

	public int size() {
		return data.length;
	}

	public TimeInterval getTimeForIndex(int idx) {
		return new TimeInterval(idx * interval.millis);
	}

	public String getTimeStringForIndex(int idx) {
		return getTimeForIndex(idx).toString();
	}

	/**
	 * If the number of measurements is odd, the last measurement
	 * is cut off.
	 */
	public Actogram downsample() {
		return downsample(2);
	}

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

	public static void devide(Actogram actogram, float s) {
		for(int i = 0; i < actogram.data.length; i++)
			actogram.data[i] /= s;
	}

	public static Actogram average(Collection<Actogram> actograms) {
		Actogram res = sum(actograms);
		devide(res, actograms.size());
		return res;
	}

	public String toString() {
		return name;
	}
}

