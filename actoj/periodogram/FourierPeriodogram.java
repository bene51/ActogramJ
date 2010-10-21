package actoj.periodogram;

import ij.IJ;
import actoj.core.Actogram;

public class FourierPeriodogram {

	private final int N;

	// in sample units
	private final int fromPeriod;
	private final int toPeriod;
	private final float[] measurements;

	private final float[] period;
	private final float[] periodogramValues;
	private final float[] pValues;

	// to exclusive
	public FourierPeriodogram(Actogram acto,
			int fromData, int toData, int fromPeriod, int toPeriod) {
		this.N = toData - fromData;
		this.measurements = new float[N];
		System.arraycopy(acto.getData(), fromData, measurements, 0, N);

		this.fromPeriod = fromPeriod;
		this.toPeriod = toPeriod;
		int nResult = toPeriod - fromPeriod;
		this.periodogramValues = new float[nResult];
		this.period = new float[nResult];
		this.pValues = new float[nResult];

		calculatePeriodogram();
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

	public void calculatePeriodogram() {
		int n = toPeriod - fromPeriod;
		float maxR2 = 0f;
		float sumR2 = 0f;
		for(int i = 0; i < n; i++) {
			int p = fromPeriod + i;
			float j = N / (float)p;
			period[i] = p;

			float r2 = R2(j);
			periodogramValues[i] = r2;
			sumR2 += r2;
			if(r2 > maxR2)
				maxR2 = r2;
			IJ.showProgress(i + 1, n);
		}
		double pV = 1 - maxR2 / sumR2;
		pV = Math.pow(pV, N - 1);
		pV *= N;
		for(int i = 0; i < n; i++)
			pValues[i] = (float)pV;
	}

	private float R2(float j) {
		double aj = 0.0, bj = 0.0;
		for(int i = 0; i < N; i++) {
			double arg = 2 * Math.PI * j * i / N;
			aj += measurements[i] * Math.cos(arg);
			bj += measurements[i] * Math.sin(arg);
		}
		aj = aj * 2 / N;
		bj = bj * 2 / N;
		return (float)(aj * aj + bj * bj);
	}
}

