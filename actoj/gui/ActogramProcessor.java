package actoj.gui;

import actoj.core.Actogram;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ActogramProcessor {

	public final Actogram original;
	public final Actogram downsampled;
	public final BufferedImage processor;

	public final int zoom;
	public final float uLimit;
	public final int ppl;

	public final int baselineDist;
	public final int signalHeight;
	public final int periods;

	public ActogramProcessor(Actogram actogram, int zoom, float uLimit, int ppl) {
		this.original = actogram;
		this.downsampled = actogram.downsample(zoom);

		this.zoom = zoom;
		this.uLimit = uLimit;
		this.ppl = ppl;

		this.periods = (int)Math.ceil(downsampled.size() /
			(float)downsampled.SAMPLES_PER_PERIOD);
		int spp = downsampled.SAMPLES_PER_PERIOD;
		// w:h ~ 2:3
		this.baselineDist = (int)Math.ceil(3f * ppl * spp / (2f * (periods + 1)));
		this.signalHeight = (int)Math.ceil(baselineDist * 0.75);
		this.processor = createProcessor();
		drawInto(downsampled, new Histogram(processor.createGraphics()), Color.BLACK);
	}

	public int getIndex(int x, int y) {
		int spp = downsampled.SAMPLES_PER_PERIOD;
		int xIdx = x % spp;
		int yIdx = (y-1) / baselineDist - (ppl - x / spp - 1);
		if(xIdx < 0 || yIdx < 0 || yIdx >= periods + 1)
			return -1;
		return yIdx * spp + xIdx;
	}

	// points[xfold]
	public void getPoint(int index, Point[] points) {
		int spp = downsampled.SAMPLES_PER_PERIOD;
		int period = index / spp;
		int mod = index % spp;

		points[0].x = (ppl - 1) * spp + mod;
		points[0].y = (period + 1) * baselineDist;

		for(int i = 1; i < ppl; i++) {
			points[i].x = (i - 1) * spp + mod;
			points[i].y = points[0].y + baselineDist;
		}
	}

	private BufferedImage createProcessor() {
		Actogram actogram = downsampled;
		int spp = downsampled.SAMPLES_PER_PERIOD;

		int w = ppl * spp;
		int h = (periods + 1) * baselineDist;
		BufferedImage bImage = new BufferedImage(w, h,
			BufferedImage.TYPE_INT_ARGB);

		Graphics g = bImage.createGraphics();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		g.setColor(Color.DARK_GRAY);
		g.drawRect(0, 0, w, h);

		return bImage;
	}

	void drawInto(Actogram actogram, Style style, Color color) {
		Graphics g = style.getGraphics();
		int spp = actogram.SAMPLES_PER_PERIOD;

		int offs = 0;
		for(int d = 0; d < periods; d++) {
			offs = spp * d;
			int length = offs + spp < actogram.size() ?
					spp : actogram.size() - offs;

			// first half
			int y = (d + 1) * baselineDist;
			int x = (ppl - 1) * spp;
			// draw baseline
			g.setColor(Color.BLACK);
			g.drawLine(x, y, x + length, y);
			// draw signal
			g.setColor(color);
			style.newline(x, y);
			for(int i = offs; i < offs + length; i++, x++) {
				float v = actogram.get(i);
				int sh = (int)Math.round(signalHeight * Math.min(uLimit, v) / uLimit);
				style.newData(sh);
// 				p.drawLine(x, y, x, y - sh);
			}

			// rest
			y += baselineDist;

			for(int r = 1; r < ppl; r++) {
				x = (r - 1) * spp;
				// draw baseline
				g.setColor(Color.BLACK);
				g.drawLine(x, y, x + length, y);
				// draw signal
				g.setColor(color);
				style.newline(x, y);
				for(int i = offs; i < offs + length; i++, x++) {
					float v = actogram.get(i);
					int sh = (int)Math.round(signalHeight * Math.min(uLimit, v) / uLimit);
					style.newData(sh);
// 					p.drawLine(x, y, x, y - sh);
				}
			}
		}
	}

	public static interface Style {
		public void newline(int x, int y);
		public void newData(int d);
		public Graphics getGraphics();
	}

	public static class Histogram implements Style {

		int x, y;
		final Graphics g;

		public Histogram(Graphics g) {
			this.g = g;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void newData(int d) {
			g.drawLine(x, y, x, y - d);
			this.x++;
		}

		public Graphics getGraphics() {
			return g;
		}
	}

	public static class Lines implements Style {

		int last = -1;

		int x, y;
		final Graphics g;

		public Lines(Graphics g) {
			this.g = g;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
			last = -1;
		}

		public void newData(int d) {
			if(last == -1)
				last = d;
			g.drawLine(x-1, y - last, x, y - d);
			last = d;
			this.x++;
		}

		public Graphics getGraphics() {
			return g;
		}
	}
}

