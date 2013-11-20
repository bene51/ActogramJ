package actoj;

import actoj.core.Actogram;

public class AverageActivity {

	/**
	 * Calculate the average activity pattern of an actogram.
	 * @param acto The actogram of which the activity pattern is calculated.
	 * @param fromData The index in the data which is used as the interval start.
	 * @param toData The index in the data which is used as the interval end (exclusively).
	 * @param period The assumed period length in sample units.
	 * @return
	 */
	public static float[] calculateAverageActivity(Actogram acto, int fromData, int toData, int period) {
		return new AverageActivity(acto, fromData, toData, period).getAverageActivity();
	}

	private final float[] averageActivities;
	private final int[] counts;

	private AverageActivity(Actogram acto, int fromData, int toData, int period) {

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

