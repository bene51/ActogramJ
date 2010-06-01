package actoj.util;

public class FloatArray {

	public static final int INITIAL_LENGTH = 20;

	private float[] data;
	private int length;

	public FloatArray() {
		data = new float[INITIAL_LENGTH];
		length = INITIAL_LENGTH;
	}

	public void add(float v) {
		if(length == data.length) {
			float[] tmp = data;
			data = new float[2 * length];
			System.arraycopy(tmp, 0, data, 0, length);
		}
		data[length++] = v;
	}

	public float[] toArray() {
		float[] ret = new float[length];
		System.arraycopy(data, 0, ret, 0, length);
		return ret;
	}
}

