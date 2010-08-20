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

	public final int width;
	public final int height;

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

		this.width = ppl * spp;
		this.height = (periods + 1) * baselineDist;

		this.processor = createProcessor();
		drawInto(downsampled, new Histogram(
			new GraphicsBackend(processor.getGraphics())), Color.BLACK);
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
		BufferedImage bImage = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_ARGB);

		clearBackground(new GraphicsBackend(bImage.getGraphics()));
		return bImage;
	}

	public void clearBackground(DrawingBackend ba) {
		ba.moveTo(0, 0);

		ba.setFillColor(255, 255, 255, 255);
		ba.fillRectangle(w, h);

		ba.setLineColor(50, 50, 50, 255);
		ba.drawRectangle(w, h);
	}

	void drawInto(Actogram actogram, Style style, Color color) {
		DrawingBackend g = style.getBackend();
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
			g.setLineColor(0, 0, 0, 255);
			g.moveTo(x, y);
			g.lineTo(x + length, y);
			// draw signal
			g.setFillColor(color.getRGB());
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
				g.setLineColor(0, 0, 0, 255);
				g.moveTo(x, y);
				g.lineTo(x + length, y);
				// draw signal
				g.setFillColor(color.getRGB());
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

