package actoj.core;

public class TimeInterval {

	/**
	 * Enum representing different time units, each holding
	 * the appropriate number of milliseconds in the inMillis field.
	 */
	public static enum Units {
		MILLISECONDS (1),
		SECONDS      (1000),
		MINUTES      (1000 * 60),
		HOURS        (1000 * 60 * 60),
		DAYS         (1000 * 60 * 60 * 24),
		YEARS        (1000 * 60 * 60 * 24 * 365L);

		/** Number of milliseconds of this time unit. */
		final long inMillis;

		/** Constructor. */
		Units(long inMillis) {
			this.inMillis = inMillis;
		}
	}

	/** Length of this time interval in milliseconds. */
	public final long millis;

	/**
	 * Constructor.
	 */
	public TimeInterval(long ms) {
		this.millis = ms;
	}

	/**
	 * Constructor.
	 */
	public TimeInterval(long mul, Units unit) {
		this.millis = mul * unit.inMillis;
	}

	/**
	 * Multiply this interval with the given factor.
	 */
	public TimeInterval mul(int factor) {
		return new TimeInterval(factor * millis);
	}

	/**
	 * Returns true if both intervals are of the same length.
	 */
	public boolean equals(Object o) {
		return millis == ((TimeInterval)o).millis;
	}

	/**
	 * Transfers this interval in the given unit.
	 */
	public float intervalIn(Units unit) {
		return (float)millis / unit.inMillis;
	}

	/**
	 * Returns a string representation of this time interval.
	 */
	public String toString() {
		StringBuffer b = new StringBuffer();
		long r = millis;
		if(r / Units.YEARS.inMillis > 0) {
			b.append((r / Units.YEARS.inMillis) + "y ");
			r %= Units.YEARS.inMillis;
		}
		if(r / Units.DAYS.inMillis > 0) {
			b.append((r / Units.DAYS.inMillis) + "d ");
			r %= Units.DAYS.inMillis;
		}
		if(r / Units.HOURS.inMillis > 0) {
			b.append((r / Units.HOURS.inMillis) + "h ");
			r %= Units.HOURS.inMillis;
		}
		if(r / Units.MINUTES.inMillis > 0) {
			b.append((r / Units.MINUTES.inMillis) + "m ");
			r %= Units.MINUTES.inMillis;
		}
		if(r / Units.SECONDS.inMillis > 0) {
			b.append((r / Units.SECONDS.inMillis) + "s ");
			r %= Units.SECONDS.inMillis;
		}
		if(r > 0)
			b.append(r + "ms ");

		if(b.length() == 0)
			b.append('0');

		return b.toString();
	}
}

