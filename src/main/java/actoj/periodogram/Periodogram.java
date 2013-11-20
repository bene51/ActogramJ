package actoj.periodogram;

import actoj.core.Actogram;

public abstract class Periodogram {

	protected final int N;

	// in sample units
	protected final int fromPeriod;
	protected final int toPeriod;
	protected final float[] measurements;

	protected final float[] period;
	protected final float[] periodogramValues;
	protected final float[] pValues;

	// to exclusive
	public Periodogram(Actogram acto, int fromData, int toData,
		int fromPeriod, int toPeriod, double pLevel) {

		this.N = toData - fromData;
		this.measurements = new float[N];
		System.arraycopy(acto.getData(), fromData, measurements, 0, N);

		this.fromPeriod = fromPeriod;
		this.toPeriod = toPeriod;
		int nResult = toPeriod - fromPeriod;
		this.periodogramValues = new float[nResult];
		this.period = new float[nResult];
		this.pValues = new float[nResult];

		calculatePeriodogram(pLevel);
	}

	public boolean canCalculatePValues() {
		return true;
	}

	public float[] getPeriodogramValues() {
		return periodogramValues;
	}

	public float[] getPeriod() {
		return period;
	}

	public float[] getPValues() {
		return pValues;
	}

	protected abstract void calculatePeriodogram(double pLevel);

	public abstract String getMethod();

	public abstract String getResponseName();
}


