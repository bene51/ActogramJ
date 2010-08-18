package actoj.util;

public class Filters {
	public static float[] makeUniformKernel(int length) {
		float[] kernel = new float[length];
		for(int i = 0; i < length; i++)
			kernel[i] = 1f / length;
		return kernel;
	}

	public static float[] makeGaussianKernel(float sigma) {
		int l = 2 * 3 * (int)Math.ceil(sigma) + 1;
		float[] k = new float[l];

		int m = l/2;
		k[m] = 1f;
		float s2 = sigma * sigma;
		float sum = 1;
		for(int i = 1; i <= m; i++) {
			float v = (float)(Math.exp(-0.5 * i * i / s2));
			k[m + i] = k[m - i] = v;
			sum += (2 * v);
		}
		for(int i = 0; i < l; i++)
			k[i] /= sum;

		return k;
	}
}
