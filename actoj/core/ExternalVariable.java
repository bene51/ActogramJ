package actoj.core;

import actoj.gui.DrawingBackend;

public class ExternalVariable {

	public final boolean[] values;

	public String name = "New variable";
	public int onColor  = java.awt.Color.WHITE.getRGB();
	public int offColor = java.awt.Color.BLACK.getRGB();

	private java.awt.Font font = new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 10);

	public ExternalVariable(ExternalVariable ext) {
		this(ext.name, ext.values.length);
		System.arraycopy(ext.values, 0, this.values, 0, ext.values.length);
		this.onColor = ext.onColor;
		this.offColor = ext.offColor;
	}

	public ExternalVariable(String name, int perLength) {
		this.name = name;
		this.values = new boolean[perLength];
	}

	public void paint(DrawingBackend g, int w, int xfold) {
		int fh = 12;
		g.moveTo(0, fh);
		g.setFont(font);
		g.setLineColor(0, 0, 0, 255);
		g.setFillColor(0, 0, 0, 255);
		g.drawText(name);

		float factor = xfold * values.length / (float)w;
		for(int x = 0; x < w; x++) {
			int idx = ((int)(factor * x)) % values.length;
			g.setFillColor(values[idx] ? onColor : offColor);
			g.moveTo(x, fh + 2);
			g.fillRectangle(1, 10);
		}
	}

	public String toString() {
		return name;
	}
}

