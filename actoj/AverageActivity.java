package actoj;

import actoj.core.*;

public class AverageActivity {

	public static float[] calculateAverageActivity(Actogram acto, int fromData, int toData, int period) {
		return new AverageActivity(acto, fromData, toData, period).getAverageActivity();
	}

	// in sample units
	private final int period;

	private final float[] averageActivities;
	private final int[] counts;

	// to exclusive
	private AverageActivity(Actogram acto, int fromData, int toData, int period) {

		this.period = period;
		this.averageActivities = new float[period];
		this.counts = new int[period];

		float[] data = acto.getData();

		for(int i = fromData; i < toData; i++) {
			int idx = i % period;
			counts[idx]++;
			averageActivities[idx] += data[i];
		}

		for(int i = 0; i < period; i++)
			averageActivities[i] /= counts[i];
	}

	public float[] getAverageActivity() {
		return averageActivities;
	}
}

