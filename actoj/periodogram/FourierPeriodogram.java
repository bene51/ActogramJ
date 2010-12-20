package actoj.periodogram;

import ij.IJ;
import actoj.core.Actogram;

public class FourierPeriodogram extends Periodogram {

	public FourierPeriodogram(Actogram acto, int fromData,
		int toData, int fromPeriod, int toPeriod, double pLevel) {
		super(acto, fromData, toData, fromPeriod, toPeriod, pLevel);
	}

	public String getMethod() {
		return "Fourier";
	}

	public String getResponseName() {
		return "R^2";
	}

	public boolean canCalculatePValues() {
		return false;
	}

	protected void calculatePeriodogram(double pLevel) {
		int n = toPeriod - fromPeriod;
		float sumR2 = 0f;
		for(int i = 0; i < n; i++) {
			int p = fromPeriod + i;
			float j = N / (float)p;
			period[i] = p;

			float r2 = R2(j);
			periodogramValues[i] = r2;
			sumR2 += r2;
			IJ.showProgress(i + 1, n);
		}
		double pV = sumR2 * (1 - Math.pow(pLevel / N, 1.0 / (N - 1)));
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

