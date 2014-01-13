package actoj.activitypattern;

import java.util.ArrayList;

import actoj.core.Actogram;
import actoj.core.TimeInterval;

public class Acrophase {
	public static ArrayList<Integer> calculateActivityPattern(Actogram a, int from, int to) {
		return calculate(a, from, to, new TimeInterval(a.SAMPLES_PER_PERIOD * a.interval.millis));
	}

	public static ArrayList<Integer> calculate(Actogram a, int from, int to, TimeInterval T) {
		int period = (int)Math.round(T.millis / a.interval.millis); // a.SAMPLES_PER_PERIOD;

		float[] data = a.getData();
		int l = (to - from) / period;

		ArrayList<Integer> positions = new ArrayList<Integer>(l);
		for(int d = 0; d < l; d++) {
			int offs = from + d * period;
			double s = 0;
			double c = 0;
			for(int i = 0; i < period; i++) {
				float yi = data[offs + i];
				double b = 2 * Math.PI * i / period;
				s += yi * Math.sin(b);
				c += yi * Math.cos(b);
			}
			double phi = Math.atan2(s, c);
			int shift = (int)Math.round(phi * period / (2 * Math.PI));
			while(shift < 0)
				shift += period;
			shift = shift % period;
			positions.add(offs + shift);
		}

		return positions;
	}
}
