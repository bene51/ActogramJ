package actoj.core;

/**
 * Represents a group of actograms, which are measured together
 * and read from a common file.
 */
public class ActogramGroup {

	/**
	 * The name of this actogram group.
	 */
	public final String name;

	/**
	 * The number of individual actograms.
	 */
	public final int length;

	/**
	 * An array of the individual actograms.
	 */
	private final Actogram[] actograms;

	/**
	 * Constructor.
	 */
	public ActogramGroup(String name, Actogram[] actograms) {
		this.name = name;
		this.actograms = actograms;
		this.length = actograms.length;
	}

	/**
	 * Returns the number of individual actograms.
	 */
	public int size() {
		return length;
	}

	/**
	 * Returns the index of the specified actogram, -1 if it
	 * is not in the array of this actogram group.
	 */
	public int indexOf(Actogram actogram) {
		for(int i = 0; i < actograms.length; i++)
			if(actograms[i].equals(actogram))
				return i;
		return -1;
	}

	/**
	 * Returns the idx'th actogram from this group.
	 */
	public Actogram get(int idx) {
		return actograms[idx];
	}

	/**
	 * Returns the name of this actogram group.
	 */
	public String toString() {
		return name;
	}
}

