package actoj.core;

import java.text.DecimalFormat;

public class TimeInterval {

	/**
	 * Enum representing different time units, each holding
	 * the appropriate number of milliseconds in the inMillis field.
	 */
	public static enum Units {
		MILLISECONDS (1, "ms"),
		SECONDS      (1000, "s"),
		MINUTES      (1000 * 60, "m"),
		HOURS        (1000 * 60 * 60, "h"),
		DAYS         (1000 * 60 * 60 * 24, "d"),
		YEARS        (1000 * 60 * 60 * 24 * 365L, "y");

		/** Number of milliseconds of this time unit. */
		public final long inMillis;

		/** Abbreviation char */
		public final String abbr;

		/** Constructor. */
		Units(long inMillis, String abbr) {
			this.inMillis = inMillis;
			this.abbr = abbr;
		}
	}

	/** Length of this time interval in milliseconds. */
	public final double millis;

	/**
	 * Constructor.
	 */
	public TimeInterval(double ms) {
		this.millis = ms;
	}

	/**
	 * Constructor.
	 */
	public TimeInterval(double mul, Units unit) {
		this.millis = mul * unit.inMillis;
	}

	/**
	 * Multiply this interval with the given factor.
	 */
	public TimeInterval mul(double factor) {
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
	DecimalFormat df = new DecimalFormat("#.##");
	public String toString() {
		StringBuffer b = new StringBuffer();
		double r = millis;
		double h = r / Units.YEARS.inMillis;
		if(h > 0) {
			b.append((int)Math.floor(h) + "y ");
			r = r - h;
		}
		h = r / Units.DAYS.inMillis;
		if(h > 0) {
			b.append((int)Math.floor(h) + "d ");
			r = r - h;
		}
		h = r / Units.HOURS.inMillis;
		if(h > 0) {
			b.append((int)Math.floor(h) + "h ");
			r = r - h;
		}
		h = r / Units.MINUTES.inMillis;
		if(h > 0) {
			b.append((int)Math.floor(h) + "m ");
			r = r - h;
		}
		h = r / Units.SECONDS.inMillis;
		if(h > 0) {
			b.append((int)Math.floor(h) + "s ");
			r = r - h;
		}
		if(r > 0)
			b.append(df.format(r) + "ms ");

		if(b.length() == 0)
			b.append('0');

		return b.toString();
	}
}

