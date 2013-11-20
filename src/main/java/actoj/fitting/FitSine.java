package actoj.fitting;

import java.util.Arrays;

import pal.math.ConjugateDirectionSearch;
import pal.math.MultivariateFunction;
import actoj.core.Actogram;
import actoj.util.Filters;

/**
 * Fit a function max(0, a * sin(b * t + c) + d)
 * Initial guess:
 *     T = SAMPLES_PER_PERIOD
 *   * a: max(data);                between 0 - initial
 *   * b: 2PI / T;                  between (0.5 - 1.5) * initial
 *   * c: 0;                        between 0 and T
 *   * d: 0;                        between -init(a) - +init(a)
 */
public class FitSine {

	private final Actogram actogram;
	private final int from, to;

	private double[] initial = new double[4];
	private double[] min = new double[4];
	private double[] max = new double[4];

	public static double[] fit(Actogram a, int from, int to) {
		FitSine fs = new FitSine(a, from, to);
		return fs.optimizeMultiRes();
	}

	public static Actogram getCurve(Actogram a, double[] param) {
		float[] ret = new float[a.size()];
		for(int i = 0; i < ret.length; i++)
			ret[i] = (float)calculate(i, param);

		return new Actogram("fitted", ret, a.SAMPLES_PER_PERIOD,
			a.interval, a.unit);
	}

	private FitSine(Actogram actogram, int from, int to) {
		this.actogram = actogram;
		this.from = from;
		this.to = to;
	}

	private double[] optimizeMultiRes() {
		A org = new A(actogram.getData(), 1);

		int l = org.data.length;
		int n = 1;
		while((l /= 2) > 1000)
			n++;

		A[] pyramid = new A[n];
		pyramid[0] = org;
		float sigma = actogram.SAMPLES_PER_PERIOD / 30f / (n - 1);
		for(int i = 1; i < n; i++) {
			pyramid[i] = pyramid[i - 1].smooth(sigma);
			pyramid[i] = pyramid[i].downsample();
		}

		calculateInitials(pyramid[n - 1]);
		double[] opt = initial;
		for(int i = n - 1; i >= 1; i--)
			opt = optimize(pyramid[i], opt);
		return opt;
	}

	private double[] optimize(A gram, double[] init) {
		ConjugateDirectionSearch CG = new ConjugateDirectionSearch();
		SineFunction fun = new SineFunction(gram, from, to, min, max, init);
		double[] x = new double[4];
		CG.optimize(fun, x, 0.01, 0.01);
		return fun.bestParameters;
	}

	private void calculateInitials(A gram) {
		int f = this.from / gram.factor;
		int t = Math.min(this.to / gram.factor, gram.data.length);

		float[] sorted = new float[t - f];

		System.arraycopy(gram.data, f, sorted, 0, t -f);
		Arrays.sort(sorted);

		float maximum = sorted[sorted.length - 1];
		float median  = sorted[sorted.length / 2];

		initial[0] = maximum;
		initial[1] = 2 * Math.PI / actogram.SAMPLES_PER_PERIOD;
		initial[2] = 0;
		initial[3] = 0;

		min[0] = median;            max[0] = 2 * maximum;
		min[1] = 0.5 * initial[1];  max[1] = 1.5 * initial[1];
		min[2] = 0;                 max[2] = actogram.SAMPLES_PER_PERIOD;
		min[3] = -maximum;          max[3] = +maximum;
	}

	static double calculate(double t, double[] args) {
		return Math.max(0, args[0] * Math.sin(args[1] * t + args[2]) + args[3]);
	}

	private static abstract class NormalizedMultivariateFunction implements MultivariateFunction {
		private final double[] min;     // the minimum values before normalization
		private final double[] max;     // the maximum values before normalization
		private final double[] factor;  // the noralization factors (multiplication results in
		                                // normalized values
		private final double[] initial;
		protected final int N;

		protected double minDiff = Double.POSITIVE_INFINITY;
		protected double[] bestParameters;

		NormalizedMultivariateFunction(double[] min, double[] max, double[] initial) {
			this.min = min;
			this.max = max;
			this.initial = initial;

			N = min.length;

			factor = new double[N];
			bestParameters = new double[N];
			double ax = 0;

			for(int i = 0; i < N; i++) {
				factor[i] = Math.abs(max[i] - min[i]);
				if(factor[i] > ax)
					ax = factor[i];
			}

			for(int i = 0; i < N; i++)
				factor[i] = ax / factor[i];
		}

		@Override
		public double getLowerBound(int n) {
			return (min[n] - initial[n]) * factor[n];
		}

		@Override
		public double getUpperBound(int n) {
			return (max[n] - initial[n]) * factor[n];
		}

		@Override
		public int getNumArguments() {
			return N;
		}

		public double[] getRealParameters(double[] args) {
			double[] param = new double[N];
			for(int i = 0; i < N; i++)
				param[i] = initial[i] + args[i] / factor[i];
			return param;
		}

		@Override
		public abstract double evaluate(double[] args);
	}


	private static class SineFunction extends NormalizedMultivariateFunction {

		private final A actogram;
		private final int from, to;

		public SineFunction(A actogram, int from, int to, double[] min, double[] max, double[] init) {
			super(min, max, init);
			this.actogram = actogram;
			this.from = from;
			this.to = to;
		}

		@Override
		public double evaluate(double[] args) {
			double[] param = getRealParameters(args);
			double diff = 0.0;
			float[] data = actogram.getData();
			int from = this.from / actogram.factor;
			int to = this.to / actogram.factor;
			for(int i = from; i < to && i < data.length; i++)
				diff += Math.abs(data[i] - calculate(actogram.factor * i, param));

			if(diff < minDiff) {
				minDiff = diff;
				System.arraycopy(param, 0, bestParameters, 0, N);
			}
			return diff;
		}
	}

	private static class A {
		float[] data;
		int factor;

		A(float[] data, int factor) {
			this.data = data;
			this.factor = factor;
		}

		public float[] getData() {
			return data;
		}

		A downsample() {
			int newlength = data.length / 2;
			float[] newdata = new float[newlength];
			for(int i = 0; i < newlength; i++) {
				newdata[i] = 0;
				newdata[i] += data[2 * i + 0];
				newdata[i] += data[2 * i + 1];
				newdata[i] /= 2f;
			}
			return new A(newdata, factor * 2);
		}

		A smooth(float sigma) {
			float[] kernel = Filters.makeGaussianKernel(sigma);
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
			return new A(n, factor);
		}
	}
}

