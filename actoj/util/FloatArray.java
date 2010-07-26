package actoj.util;

/**
 * Utility class representing a simple growing float array.
 */
public class FloatArray {

	/** The initial length of the array. */
	public static final int INITIAL_LENGTH = 20;

	/** The array. */
	private float[] data;

	/** The current length of the array. */
	private int length;

	/**
	 * Constructor.
	 */
	public FloatArray() {
		data = new float[INITIAL_LENGTH];
		length = INITIAL_LENGTH;
	}

	/**
	 * Append the given value at the end of the array.
	 */
	public void add(float v) {
		if(length == data.length) {
			float[] tmp = data;
			data = new float[2 * length];
			System.arraycopy(tmp, 0, data, 0, length);
		}
		data[length++] = v;
	}

	/**
	 * Returns a new (trimmed) array with the current values.
	 */
	public float[] toArray() {
		float[] ret = new float[length];
		System.arraycopy(data, 0, ret, 0, length);
		return ret;
	}
}

