package actoj.fitting;

import java.util.Arrays;

import actoj.core.Actogram;
import actoj.util.Filters;

import pal.math.*;

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
		float[] sigmas = new float[] {100f, 50f, 20f, 10f, 5f, 2f};
		Actogram a = actogram.convolve(
				Filters.makeGaussianKernel(sigmas[0]));
		calculateInitials(a);
		double[] opt = optimize(a, initial);
		for(int i = 1; i < sigmas.length; i++) {
			a = actogram.convolve(Filters.makeGaussianKernel(sigmas[i]));
			opt = optimize(a, opt);
			System.out.println(i);
		}
		return opt;
	}

	private double[] optimize(Actogram gram, double[] init) {
		ConjugateDirectionSearch CG = new ConjugateDirectionSearch();
		SineFunction fun = new SineFunction(gram, from, to, min, max, init);
		double[] x = new double[4];
		CG.optimize(fun, x, 0.01, 0.01);
		return fun.getRealParameters(x);
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

	private static final void print(double[] par) {
		System.out.print("parameters: ");
		for(double d : par)
			System.out.print(d + " ");
		System.out.println();
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
		private final int N;

		NormalizedMultivariateFunction(double[] min, double[] max, double[] initial) {
			this.min = min;
			this.max = max;
			this.initial = initial;

			N = min.length;

			factor = new double[N];
			double ax = 0;

			for(int i = 0; i < N; i++) {
				factor[i] = Math.abs(max[i] - min[i]);
				if(factor[i] > ax)
					ax = factor[i];
			}

			for(int i = 0; i < N; i++)
				factor[i] = ax / factor[i];
		}

		public double getLowerBound(int n) {
			return (min[n] - initial[n]) * factor[n];
		}

		public double getUpperBound(int n) {
			return (max[n] - initial[n]) * factor[n];
		}

		public int getNumArguments() {
			return N;
		}

		public double[] getRealParameters(double[] args) {
			double[] param = new double[N];
			for(int i = 0; i < N; i++)
				param[i] = initial[i] + args[i] / factor[i];
			return param;
		}

		public abstract double evaluate(double[] args);
	}

	
	private static class SineFunction extends NormalizedMultivariateFunction {

		private final Actogram actogram;
		private final int from, to;

		public SineFunction(Actogram actogram, int from, int to, double[] min, double[] max, double[] init) {
			super(min, max, init);
			this.actogram = actogram;
			this.from = from;
			this.to = to;
		}

		public double evaluate(double[] args) {
			double[] param = getRealParameters(args);
			double diff = 0.0;
			float[] data = actogram.getData();
			for(int i = from; i < to; i++)
				diff += Math.abs(data[i] - calculate(i, param));
			return diff;
		}
	}

// 	private static class A {
// 		float[] data;
// 		int from;
// 		int to;
// 		int factor;
// 
// 		A(float[] data, int from, int to, int factor) {
// 			this.data = data;
// 			this.from = from;
// 			this.to = to;
// 			this.factor = factor;
// 		}
// 
// 		A downsample() {
// 			A smoothed = convolve(Filters.makeGaussianKernel(2f));
// 			int newlength = smoothed.data.length / 2;
// 			float[] newdata = new float[newlength];
// 			for(int i = 0; i < newlength; i++) {
// 				newdata[i] = 0;
// 				newdata[i] += smoothed.data[2 * i + 0];
// 				newdata[i] += smoothed.data[2 * i + 1];
// 				newdata[i] /= 2f;
// 			}
// 			return new A(newdata, from * 2, Math.min(to * 2, newdata.length), factor * 2);
// 		}
// 
// 		A convolve(float[] kernel) {
// 			int dl = data.length;
// 			int kl = kernel.length;
// 
// 			float[] n = new float[data.length];
// 
// 			for(int i = 0; i < dl; i++) {
// 				n[i] = 0;
// 				for(int j = 0; j < kl; j++) {
// 					int idx = i + j - kl/2;
// 					float v = (idx < 0 || idx >= dl) ? 0 : data[idx];
// 					n[i] += kernel[j] * v;
// 				}
// 			}
// 			return new A(n, from, to, factor);
// 		}
// 	}
}

