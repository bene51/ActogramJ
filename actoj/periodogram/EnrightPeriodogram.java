package actoj.periodogram;

import ij.IJ;
import actoj.core.Actogram;

public class EnrightPeriodogram extends Periodogram {

	// to exclusive
	public EnrightPeriodogram(Actogram acto,
			int fromData, int toData, int fromPeriod, int toPeriod) {
		super(acto, fromData, toData, fromPeriod, toPeriod);
	}

	public String getMethod() {
		return "Chi-Square";
	}

	public String getResponseName() {
		return "Qp";
	}

	protected void calculatePeriodogram() {
		int n = toPeriod - fromPeriod;

		double M = 0;
		for(int i = 0; i < N; i++)
			M += measurements[i];
		M /= N;

		double mse = 0;
		for(int i = 0; i < N; i++) {
			double diff = measurements[i] - M;
			mse += diff * diff;
		}
		mse /= N;

		for(int P = fromPeriod; P < toPeriod; P++) {
			double Qp = 0;
			int K = N / P; // integer division
			for(int h = 0; h < P; h++) {
				double Mh = 0;
				for(int k = 0; k < K; k++)
					Mh += measurements[h + k * P];
				Mh /= K;
				double diff = Mh - M;
				Qp += diff * diff;
			}
			Qp = Qp * K / mse;
			period[P - fromPeriod] = P;
			periodogramValues[P - fromPeriod] = (float)Qp;
			pValues[P - fromPeriod] = (float)chisquare_cdf_inv(0.05, P);
		}
	}

	private static double chiSquare(float x, int v) {
		double g = v % 2 == 0 ? gammaN(v / 2) : gammaNPlusHalf(v / 2);
		double halfX = x / 2.0;
		double halfV = v / 2.0;
		return Math.pow(x, halfV - 1) *
			Math.exp(-halfX) /
			Math.pow(2, halfV) /
			g;
	}

	/**
	 * Returns gamma(n)
	 */
	private static double gammaN(int n) {
		return factorial(n - 1);
	}

	/**
	 * Returns gamma(n + 0.5)
	 */
	private static double gammaNPlusHalf(int n) {
		return Math.sqrt(Math.PI) * doubleFactorial(n) / Math.pow(2, n);
	}

	private static double factorial(int n) {
		int ret = 1;
		for(int i = 1; i <= n; i++)
			ret *= i;
		return ret;
	}

	/**
	 * For an odd positive integer n = 2k - 1, k>=1,
	 * (2k - 1)!! = Prod_{i=1}^k (2i - 1).
	 */
	private static int doubleFactorial(int k) {
		int ret = 1;
		for(int i = 1; i <= k; i++)
			ret *= (2 * i - 1);
		return ret;
	}

	/**
	 * see http://en.wikipedia.org/wiki/Noncentral_chi-square_distribution#Approximation
	 */
	public static double chisquare_cdf_inv(double x, int k) {

		double arg = norm_cdf_inv(x);

		double h = 1 - (2.0 * k * k) / (3.0 * k * k);
		double p = 1.0 / k;
		double m = (h - 1) * (1 - 3 * h);

		double s = arg * h * Math.sqrt(2 * p) * (1 + 0.5 * m * p) +
			(1 + h * p * (h - 1 - 0.5 * (2 - h) * m * p));
		return k * Math.pow(s, 1.0 / h);

	}

	private static final double chisquare_cdf(double x, int k) {
		double h = 1 - (2.0 * k * k) / (3.0 * k * k);
		double p = 1.0 / k;
		double m = (h - 1) * (1 - 3 * h);

		double arg = Math.pow(x / k, h);
		arg = arg - (1 + h * p * (h - 1 - 0.5 * (2 -h) * m * p));
		arg = arg / (h * Math.sqrt(2 * p) * (1 + 0.5 * m * p));

		System.out.println("arg = " + arg);

		return norm_cdf(arg);
	}

	/**
	 * see http://en.wikipedia.org/wiki/Standard_normal
	 */
	private static final double norm_cdf_inv(double p) {
		return Math.sqrt(2) * erf_inv(2 * p - 1);
	}

	private static final double norm_cdf(double x) {
		return (1 + erf(x / Math.sqrt(2))) / 2.0;
	}

	private static final double erf(double x) {
		double ax2 = a * x * x;
		return x / Math.abs(x) * Math.sqrt(1 - Math.exp(-x * x * (4 / Math.PI + ax2) / (1 + ax2)));
	}

	/**
	 * see http://en.wikipedia.org/wiki/Error_function
	 */
	private static final double a =
		8 * (Math.PI - 3) / 3 / Math.PI / (4 - Math.PI);

	private static double erf_inv(double x) {
		double lg = Math.log(1 - x * x);
		double s = 2 / (Math.PI * a) + lg / 2;
		return x / Math.abs(x) * Math.sqrt(Math.sqrt(s * s - lg / a) - s);
	}
}
