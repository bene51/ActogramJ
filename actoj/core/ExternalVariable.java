package actoj.core;

import actoj.gui.DrawingBackend;
import java.awt.Font;
import java.awt.Color;

public class ExternalVariable {

	public final boolean[] values;

	public String name = "New bar";
	public int onColor  = Color.WHITE.getRGB();
	public int offColor = Color.BLACK.getRGB();

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

	public void paint(DrawingBackend g, int w, int h, int xfold) {
		int halfHeight = h / 2;
		int fontsize = halfHeight - 2;
		if(fontsize > 8) {
			int fh = 12;
			g.moveTo(0, halfHeight - 1);
			g.setFont(new Font("Helvetica", Font.PLAIN, fontsize));
			g.setLineColor(0, 0, 0, 255);
			g.setFillColor(0, 0, 0, 255);
			g.drawText(name);
		}

		float factor = xfold * values.length / (float)w;
		for(int x = 0; x < w; x++) {
			int idx = ((int)(factor * x)) % values.length;
			g.setFillColor(values[idx] ? onColor : offColor);
			g.moveTo(x, halfHeight + 2);
			g.fillRectangle(1, halfHeight - 3);
		}
	}

	public String toString() {
		return name;
	}
}

