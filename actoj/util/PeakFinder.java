package actoj.util;

import java.util.TreeSet;

public class PeakFinder {

	public static int[] findPeaks(float[] signal) {
		TreeSet<Value> s = new TreeSet<Value>();

		int firstWithMaxValue = 0;
		float lastValue = signal[0];
		for(int i = 1; i < signal.length; i++) {
			float v = signal[i];
			if(v > lastValue) {
				firstWithMaxValue = i;
			} else if(v < lastValue && firstWithMaxValue != -1) {
				int idx = (firstWithMaxValue + (i - 1)) / 2;
				s.add(new Value(idx, signal[idx]));
				firstWithMaxValue = -1;
			}
			lastValue = v;
		}
		int[] peaks = new int[s.size()];
		int i = peaks.length - 1;
		for(Value v : s)
			peaks[i--] = v.x;

		return peaks;
	}

	private static final class Value implements Comparable<Value> {
		private int x;
		private float fx;

		Value(int x, float fx) {
			this.x = x;
			this.fx = fx;
		}

		public int compareTo(Value v) {
			if(fx != v.fx)
				return Float.compare(fx, v.fx);
			if(x < v.x) return -1;
			if(x > v.x) return +1;
			return 0;
		}
	}
}
