package actoj.activitypattern;

import ij.IJ;

import java.util.ArrayList;
import java.util.Arrays;

import actoj.core.Actogram;
import actoj.core.TimeInterval;

public class OnOffset {

	private ArrayList<Integer> onsets;
	private ArrayList<Integer> offsets;

	public static enum ThresholdMethod {
		MedianWithZero {
			@Override
			float calculateThreshold(float[] data, int offs, int len) {
				float[] tmp = new float[len];
				System.arraycopy(data, offs, tmp, 0, len);
				Arrays.sort(tmp);
				return tmp[len / 2];
			}
		},
		MedianWithoutZero {
			@Override
			float calculateThreshold(float[] data, int offs, int len) {
				int count = 0;
				float[] tmp = new float[len];
				for(int i = 0; i < len; i++) {
					float v = data[offs + i];
					if(v > 0)
						tmp[count++] = v;
				}
				Arrays.sort(tmp, 0, count);
				return tmp[count / 2];
			}
		},
		Mean {
			@Override
			float calculateThreshold(float[] data, int offs, int len) {
				float mean = 0;
				for(int i = 0; i < len; i++) {
					float v = data[offs + i];
					mean += v;
				}
				return mean;
			}
		},
		Zero {
			@Override
			float calculateThreshold(float[] data, int offs, int len) {
				return 0;
			}
		},
		Manual {
			@Override
			float calculateThreshold(float[] data, int offs, int len) {
				return (float)IJ.getNumber("Manual threshold", 0);
			}
		};

		abstract float calculateThreshold(float[] data, int offs, int len);
	}

	public static String[] thresholdMethods = new String[] {
			"Median (including zero activity)",
			"Median (without zero activity)",
			"Mean",
			"Zero",
			"Manual"
	};

	public void calculate(Actogram a, int from, int to, TimeInterval T, ThresholdMethod thresholdMethod) {
		int period = (int)Math.round(T.millis / a.interval.millis); // a.SAMPLES_PER_PERIOD;

		float[] data = a.getData();
		int l = (to - from) / period;



		onsets  = new ArrayList<Integer>(l);
		offsets = new ArrayList<Integer>(l);
		int[] h1h2 = new int[2];
		for(int d = 0; d < l; d++) {
			int offs = from + d * period;
			float threshold = thresholdMethod.calculateThreshold(data, offs, period);
			optimizePeriod(data, offs, period, h1h2, threshold);
			onsets.add(offs + h1h2[0]);
			offsets.add(offs + h1h2[1]);
		}
	}

	public void calculate(Actogram a, int from, int to, TimeInterval T, float threshold) {
		int period = (int)Math.round(T.millis / a.interval.millis); // a.SAMPLES_PER_PERIOD;

		float[] data = a.getData();
		int l = (to - from) / period;

		onsets  = new ArrayList<Integer>(l);
		offsets = new ArrayList<Integer>(l);
		int[] h1h2 = new int[2];
		for(int d = 0; d < l; d++) {
			int offs = from + d * period;
			optimizePeriod(data, offs, period, h1h2, threshold);
			onsets.add(offs + h1h2[0]);
			offsets.add(offs + h1h2[1]);
		}
	}

	public ArrayList<Integer> getOnsets() {
		return onsets;
	}

	public ArrayList<Integer> getOffsets() {
		return offsets;
	}

	private static void optimizePeriod(float[] data, int offs, int len, int[] ret, float threshold) {
		float amplitude = 1;
		float[] tmp = new float[len];
		for(int i = 0; i < len; i++)
			tmp[i] = data[offs + i] > threshold ? amplitude : 0;
		offs = 0;
		data = tmp;

		double[][] table = new double[3][len];
		final int LEFT = 1;
		final int DIAG = 2;
		int[][] direction = new int[3][len];

		table[0][0] = data[offs];
		// initialize first row
		for(int c = 1; c < table[0].length; c++) {
			table[0][c] = table[0][c - 1] + data[offs + c];
			direction[0][c] = LEFT;
		}
		// initialize first column
		table[2][0] = table[0][0];
		table[1][0] = Math.abs(data[offs] - amplitude);

		for(int r = 1; r < table.length; r++) {
			float boxv = r == 1 ? amplitude : 0;
			for(int c = 1; c < table[r].length; c++) {
				double diff = Math.abs(data[offs + c] - boxv);
				double l = table[r][c - 1];
				double d = table[r - 1][c - 1];
				if(l <= d) {
					table[r][c] = l + diff;
					direction[r][c] = LEFT;
				} else {
					table[r][c] = d + diff;
					direction[r][c] = DIAG;
				}
			}
		}

		int r = table.length - 1, c = table[0].length - 1;
		int h1 = 0, h2 = 0; // if we don't find them, they are both zero
		while(c > 0) {
			if(direction[r][c] == DIAG) {
				if(h2 == 0) {
					h2 = c;
				} else {
					h1 = c;
					break;
				}
				r--;
			}
			c--;
		}

		ret[0] = h1;
		ret[1] = h2 - 1;
	}

	public static void main(String[] args) {
		float[] data = new float[] {
				0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0
		};
		int[] h1h2 = new int[2];
		optimizePeriod(data, 0, data.length, h1h2, 0);
		int h1 = h1h2[0];
		int h2 = h1h2[1];
		System.out.println(h1 + ", " + h2);
	}
}
