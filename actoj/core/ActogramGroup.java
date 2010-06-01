package actoj.core;

public class ActogramGroup {

	public final String name;
	public final int length;

	private final Actogram[] actograms;

	public ActogramGroup(String name, Actogram[] actograms) {
		this.name = name;
		this.actograms = actograms;
		this.length = actograms.length;
	}

	public int size() {
		return length;
	}

	public int indexOf(Actogram actogram) {
		for(int i = 0; i < actograms.length; i++)
			if(actograms[i].equals(actogram))
				return i;
		return -1;
	}

	public Actogram get(int idx) {
		return actograms[idx];
	}

	public String toString() {
		return name;
	}
}

