package actoj.fitting;


public class FitPeriodicLine {

	public static final int INTERCEPT = 0;
	public static final int SLOPE = 1;

	private final double[] x;
	private final double[] y;
	private final double Ty;
	private final int N;

	public static double[] fitPeriodicLine(double[] x, double[] y, double Ty) {
		FitPeriodicLine f = new FitPeriodicLine(x, y, Ty);
		double[] ret = new double[2];
		ret[SLOPE] = f.findSlope();
		ret[INTERCEPT] = f.findIntercept(ret[SLOPE]);
		return ret;
	}

	public FitPeriodicLine(double[] x, double[] y, double Ty) {
		this.x = x;
		this.y = y;
		this.Ty = Ty;
		this.N = x.length;
	}

	private double findSlope() {
		int subd = 41;
		int min = -subd / 2;
		int max = +subd / 2;
		double dy = Ty / subd;
		double bestSlope = 0;

		while(true) {
			double mindiff = Double.POSITIVE_INFINITY;
			double slope = 0;
			for(int i = min; i <= max; i++) {
				double s = bestSlope + i * dy;
				double diff = fSlope(s);
				if(diff < mindiff) {
					slope = s;
					mindiff = diff;
				}
			}
			bestSlope = slope;
			if(dy <= 1.0) // allow 1 millisecond over one periods
				break;

			min = -2;
			max = +2;
			dy /= 2;
		}
		return bestSlope;
	}

	private double findIntercept(double slope) {
		double minx = x[0];
		for(int i = 1; i < N; i++)
			if(x[i] < minx)
				minx = x[i];
		// y = m * x + t => t = y - m * x
		double bestIntercept = Ty / 2 - minx * slope;

		int subd = 41;
		int min = -subd / 2;
		int max = +subd / 2;
		double dy = Ty / subd;

		while(true) {
			double mindiff = Double.POSITIVE_INFINITY;
			double intercept = 0;
			for(int i = min; i < max; i++) {
				double s = bestIntercept + i * dy;
				double diff = fSlopeIntercept(slope, s);
				if(diff < mindiff) {
					intercept = s;
					mindiff = diff;
				}
			}
			bestIntercept = intercept;
			if(dy <= 1)
				break;

			min = -2;
			max = +2;
			dy /= 2;
		}
		return bestIntercept;
	}

	private double fSlope(double slope) {
		double sumD = 0;
		double sumD2 = 0;
		for(int i = 0; i < N; i++) {
			double yi = slope * x[i];
			while(yi < 0)
				yi += Ty;
			yi = yi % Ty;
			double diff = y[i] - yi;
			sumD += diff;
			sumD2 += diff * diff;
		}
		return Math.sqrt(sumD2 / N - (sumD / N) * (sumD / N));
	}

	private double fSlopeIntercept(double slope, double intercept) {
		double sumD = 0;
		for(int i = 0; i < N; i++) {
			double yi = intercept + slope * x[i];
			while(yi < 0)
				yi += Ty;
			yi = yi % Ty;
			double diff = y[i] - yi;
			sumD += (diff * diff);
		}
		return Math.sqrt(sumD / N);
	}
}

