package actoj.gui;

import actoj.core.Actogram;

import ij.process.ImageProcessor;
import ij.process.ByteProcessor;

import java.awt.Point;

public class ActogramProcessor {

	public final Actogram original;
	public final Actogram downsampled;
	public final ImageProcessor processor;

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
	}

	public int getIndex(int x, int y) {
		int spp = downsampled.SAMPLES_PER_PERIOD;
		int xIdx = x % spp;
		int yIdx = y / baselineDist - (ppl - x / spp - 1);
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

	private ImageProcessor createProcessor() {
		Actogram actogram = downsampled;
		int spp = downsampled.SAMPLES_PER_PERIOD;

		int w = ppl * spp;
		int h = (periods + 1) * baselineDist;
		ByteProcessor p = new ByteProcessor(w, h);
		Style style = new Histogram(p);

		p.setValue(155);
		p.drawRect(0, 0, w, h);
		p.setValue(255);

		int offs = 0;
		for(int d = 0; d < periods; d++) {
			offs = spp * d;
			int length = offs + spp < downsampled.size() ?
					spp : downsampled.size() - offs;

			// first half
			int y = (d + 1) * baselineDist;
			int x = (ppl - 1) * spp;
			// draw baseline
			p.drawLine(x, y, x + length, y);
			// draw signal
			style.newline(x, y);
			for(int i = offs; i < offs + length; i++, x++) {
				float v = downsampled.get(i);
				int sh = (int)Math.round(signalHeight * Math.min(uLimit, v) / uLimit);
				style.newData(sh);
// 				p.drawLine(x, y, x, y - sh);
			}

			// rest
			y += baselineDist;

			for(int r = 1; r < ppl; r++) {
				x = (r - 1) * spp;
				// draw baseline
				p.drawLine(x, y, x + length, y);
				// draw signal
				style.newline(x, y);
				for(int i = offs; i < offs + length; i++, x++) {
					float v = downsampled.get(i);
					int sh = (int)Math.round(signalHeight * Math.min(uLimit, v) / uLimit);
					style.newData(sh);
// 					p.drawLine(x, y, x, y - sh);
				}
			}
		}
		p.invert();
		return p;
	}

	private interface Style {
		public void newline(int x, int y);
		public void newData(int d);
	}

	private class Histogram implements Style {

		int x, y;
		final ImageProcessor p;

		public Histogram(ImageProcessor p) {
			this.p = p;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void newData(int d) {
			p.drawLine(x, y, x, y - d);
			this.x++;
		}
	}

	private class Lines implements Style {

		int last = -1;

		int x, y;
		final ImageProcessor p;

		public Lines(ImageProcessor p) {
			this.p = p;
		}

		public void newline(int x, int y) {
			this.x = x;
			this.y = y;
			last = -1;
		}

		public void newData(int d) {
			if(last == -1)
				last = d;
			p.drawLine(x-1, y - last, x, y - d);
			last = d;
			this.x++;
		}
	}
}

