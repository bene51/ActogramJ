package actoj.periodogram;

import ij.IJ;
import actoj.core.Actogram;

public class LombScarglePeriodogram extends Periodogram {

	// to exclusive
	public LombScarglePeriodogram(Actogram acto,
		int fromData, int toData, int fromPeriod, int toPeriod) {
		super(acto, fromData, toData, fromPeriod, toPeriod);
	}

	public String getMethod() {
		return "Lomb-Scargle";
	}

	public String getResponseName() {
		return "PN";
	}

	protected void calculatePeriodogram() {
		int n = toPeriod - fromPeriod;

		// calculate mean
		double M = 0;
		for(int i = 0; i < N; i++)
			M += measurements[i];
		M /= N;

		// calculate variance
		double sigma = 0;
		for(int i = 0; i < N; i++) {
			double diff = measurements[i] - M;
			sigma += diff * diff;
		}
		sigma /= N;

		for(int P = fromPeriod; P < toPeriod; P++) {
			// calculate delta
			double fourPiByP = 4 * Math.PI / P;
			double sin = 0;
			double cos = 0;
			for(int i = 0; i < N; i++) {
				double t = i * fourPiByP;
				sin += Math.sin(t);
				cos += Math.cos(t);
			}
			double delta = Math.atan2(cos, sin) / fourPiByP;

			// calculate PN
			double nom1 = 0, denom1 = 0, nom2 = 0, denom2 = 0;
			for(int i = 0; i < N; i++) {
				double arg1 = measurements[i] - M;
				double arg2 = 2 * Math.PI * (i - delta) / P;
				double c = Math.cos(arg2);
				double s = Math.sin(arg2);
				nom1 += (arg1 * c);
				nom2 += (arg1 * s);
				denom1 += (c * c);
				denom2 += (s * s);
			}
			double PN = ((nom1 * nom1 / denom1) +
					(nom2 * nom2 / denom2)) /
				(2 * sigma * sigma);
			period[P - fromPeriod] = P;
			periodogramValues[P - fromPeriod] = (float)PN;
			IJ.showProgress(P - fromPeriod + 1, n);
		}

		// calculate p values
		double pLevel = 0.05;
		double pV = -Math.log(1 - Math.pow(1 - pLevel, 1.0 / N));

		for(int i = 0; i < pValues.length; i++)
			pValues[i] = (float)pV;
	}
}

