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

	public final double zoom;
	public final float uLimit;
	public final int ppl;

	public final int baselineDist;
	public final int signalHeight;
	public final int periods;

	public final int width;
	public final int height;

	public final float whRatio;

	public ActogramProcessor(Actogram actogram, double zoom, float uLimit, int ppl, float whRatio) {
		this.original = actogram;

		int newSPP = (int)Math.round(actogram.SAMPLES_PER_PERIOD / zoom);
		this.zoom = actogram.SAMPLES_PER_PERIOD / (double)newSPP;
		this.downsampled = actogram.downsample(this.zoom);

		this.uLimit = uLimit;
		this.ppl = ppl;
		this.whRatio = whRatio;

		this.periods = (int)Math.ceil(downsampled.size() /
			(float)downsampled.SAMPLES_PER_PERIOD);
		int spp = downsampled.SAMPLES_PER_PERIOD;

		int nlines = periods + 1;

		this.baselineDist = (int)Math.ceil(ppl * spp / (whRatio * nlines));
		this.signalHeight = (int)Math.ceil(baselineDist * 0.75);

		this.width = ppl * spp + 2;
		this.height = nlines * baselineDist;

		this.processor = createProcessor();
		drawInto(downsampled, new Histogram(
			new GraphicsBackend(processor.getGraphics())), Color.BLACK);
	}

	public int getIndexInOriginal(int i) {
		return (int)Math.floor(i * zoom);
	}

	public int getIndex(int x, int y) {
		if(x < 1 || x >= width - 1)
			return -1;
		x -= 1; // there's a 1px border
		int spp = downsampled.SAMPLES_PER_PERIOD;
		int lineIdx = (y - 1) / baselineDist;
		int colIdx = x / spp;

		int period = lineIdx - 1 + colIdx;
		if(period < 0 || period >= periods)
			return -1;

		int index = period * spp + x % spp;
		if(index < 0 || index >= downsampled.size())
			return -1;

		return index;
	}

	// points[xfold]
	public void getPoint(int index, Point[] points) {
		int spp = downsampled.SAMPLES_PER_PERIOD;
		int period = index / spp;
		int mod = index % spp;

		// + 1 for the 1px border
		points[0].x = (ppl - 1) * spp + mod + 1;
		points[0].y = (period + 1) * baselineDist;

		for(int i = 1; i < ppl; i++) {
			points[i].x = (i - 1) * spp + mod + 1;
			points[i].y = points[0].y + baselineDist;
		}
	}

	private BufferedImage createProcessor() {
		BufferedImage bImage = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_ARGB);

		clearBackground(new GraphicsBackend(bImage.getGraphics()));
		return bImage;
	}

	public void clearBackground(DrawingBackend ba) {
		ba.moveTo(0, 0);

		ba.setFillColor(255, 255, 255, 255);
		ba.fillRectangle(width, height);

		ba.setLineColor(50, 50, 50, 255);
		ba.drawRectangle(width, height);
	}

	public void drawInto(Actogram actogram, Style style, Color color) {
		DrawingBackend g = style.getBackend();
		int spp = actogram.SAMPLES_PER_PERIOD;
		int nlines = periods + 1;

		for(int l = 0; l < nlines; l++) {
			for(int c = 0; c < ppl; c++) {
				int d = l - 1 + c;
				if(d < 0 || d >= periods)
					continue;
				int y = (l + 1) * baselineDist;
				int x = c * spp + 1;
				drawPeriod(actogram, d, style, color, x, y);
			}
		}
	}

	private void drawPeriod(Actogram actogram, int d, Style style, Color color, int x, int y) {
		DrawingBackend g = style.getBackend();
		int spp = actogram.SAMPLES_PER_PERIOD;

		int offs = spp * d;
		int length = offs + spp < actogram.size() ?
				spp : actogram.size() - offs;
		// draw baseline
		g.setLineColor(0, 0, 0, 255);
		g.moveTo(x, y);
		g.lineTo(x + length - 1, y);
		// draw signal
		g.setFillColor(color.getRGB());
		style.newline(x, y);
		for(int i = offs; i < offs + length; i++, x++) {
			float v = actogram.get(i);
			int sh = (int)Math.round(signalHeight * Math.min(uLimit, v) / uLimit);
			style.newData(sh);
		}
	}

	public static interface Style {
		public void newline(int x, int y);
		public void newData(int d);
		public DrawingBackend getBackend();
	}

	public static class Histogram implements Style {

		int x, y;
		final DrawingBackend ba;

		public Histogram(DrawingBackend ba) {
			this.ba = ba;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void newData(int d) {
			ba.moveTo(x, y - d);
			ba.fillRectangle(1, d);
			this.x++;
		}

		public DrawingBackend getBackend() {
			return ba;
		}
	}

	public static class Lines implements Style {

		int last = -1;

		int x, y;
		final DrawingBackend ba;

		public Lines(DrawingBackend ba) {
			this.ba = ba;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
			last = -1;
		}

		public void newData(int d) {
			if(last == -1)
				last = d;
			ba.moveTo(x - 1, y - last);
			ba.lineTo(x, y - d);
			last = d;
			this.x++;
		}

		public DrawingBackend getBackend() {
			return ba;
		}
	}
}

