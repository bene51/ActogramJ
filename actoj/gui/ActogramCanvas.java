package actoj.gui;

import actoj.core.*;
import actoj.util.PeakFinder;
import actoj.fitting.FitSine;
import actoj.periodogram.*;

import javax.swing.JPanel;
import javax.swing.JComponent;

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import java.awt.image.BufferedImage;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ij.IJ;
import ij.gui.Plot;

/**
 * A JComponent representing one actogram plus accompanying data, like
 * calibration. It uses a ActogramProcessor to draw the actogram itself.
 */
public class ActogramCanvas extends JPanel
			implements MouseMotionListener, MouseListener {

	public static enum Mode {
		POINTING, FREERUNNING_PERIOD, FITTING;
	}

	/** The ActogramProcessor for drawing the actogram itself. */
	public final ActogramProcessor processor;

	/** The width of this component. */
	public final int width;

	/** The height of this component. */
	public final int height;

	/**
	 * The Feedback which is called when the freerunning period triangle
	 * has changed.
	 */
	private final Feedback feedback;

	/**
	 * Start point of the freerunning period triangle within the
	 * ActogramProcessor coordinate system.
	 */
	private Point fpStart = null;

	/**
	 * Current(end) point of the freerunning period triangle within the
	 * ActogramProcessor coordinate system.
	 */
	private Point fpCurr = null;

	/**
	 * Start point of the fitting interval within the
	 * ActogramProcessor coordinate system.
	 */
	private Point fitStart = null;

	/**
	 * Current(end) point of the fitting interval within the
	 * ActogramProcessor coordinate system.
	 */
	private Point fitCurr = null;

	/** Distance between left border and the actogram. */
	private int INT_LEFT = 5;

	/** Distance between right border and the actogram. */
	private int INT_RIGHT = 5;

	/** Distance between top border and the actogram. */
	private int INT_TOP = 20;

	private int INT_TOP_ALL;

	/** Distance between bottom border and the actogram. */
	private int INT_BOTTOM = 5;

	/** Stroke for the freerunning period triangle */
	private final float stroke = 3f;

	/** Font for the freerunning period triangle */
	private final Font font = new Font("Helvetica", Font.BOLD, 14);

	/** Text color for the freerunning period triangle */
	private final Color text = new Color(149, 255, 139);

	/** Color for the freerunning period triangle */
	private final Color color = new Color(26, 93, 0);

	/** Background color for the canvas */
	private final Color background = new Color(139, 142, 255);

	private int nSubdivisions = 8;

	private Mode mode = Mode.POINTING;


	public ActogramCanvas(
				Actogram actogram,
				int zoom,
				float uLimit,
				int ppl,
				int subd,
				float whRatio,
				Feedback f) {
		super();
		this.processor = new ActogramProcessor(actogram, zoom, uLimit, ppl, whRatio);
		this.feedback = f;
		this.nSubdivisions = subd;

		int nExternals = actogram.getExternalVariables().length;
		INT_TOP_ALL = INT_TOP + nExternals * 25;

		BufferedImage ip = processor.processor;
		width = ip.getWidth() + INT_LEFT + INT_RIGHT;
		height = ip.getHeight() + INT_TOP_ALL + INT_BOTTOM;

		this.setPreferredSize(new Dimension(width, height));

		addMouseListener(this);
		addMouseMotionListener(this);

		setBackground(background);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void setNSubdivisions(int n) {
		this.nSubdivisions = n;
		repaint();
	}

	// x and y are coordinates within this component
	public String getPositionString(int x, int y) {
		if(x < INT_LEFT || y < INT_TOP_ALL || x >= width - INT_RIGHT || y >= height - INT_BOTTOM)
			return "outside";
		int idx = processor.getIndex(x - INT_LEFT, y - INT_TOP_ALL);
		if(idx < 0)
			return "outside";
		Actogram a = processor.downsampled;
		return a.name + ": " + a.getTimeStringForIndex(idx) + " ("
			+ a.get(idx) + ")";
	}

	public TimeInterval getFreerunningPeriod() {
		if(fpStart == null || fpCurr == null)
			return null;
		Point st = upper(fpStart, fpCurr);
		Point cu = lower(fpStart, fpCurr);
		int dy = (cu.y - st.y) / processor.baselineDist; // in periods
		if(dy == 0)
			return null;
		int dx = cu.x - st.x;

		long i = processor.downsampled.interval.millis;
		int spp = processor.downsampled.SAMPLES_PER_PERIOD;
		return new TimeInterval(i * (dy * spp + dx) / dy);
	}

	public String getPeriodString() {
		TimeInterval tv = getFreerunningPeriod();
		return tv == null ? "" : Float.toString(
			tv.intervalIn(processor.downsampled.unit));
	}

	public void calculatePeriodogram() {
		if(fitStart == null || fitCurr == null)
			throw new RuntimeException("Interval required");

		Point st = upper(fitStart, fitCurr);
		Point cu = lower(fitStart, fitCurr);
		if(st.y == cu.y && st.x > cu.x) {
			Point tmp = st; st = cu; cu = tmp;
		}
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;

		sIdx *= processor.zoom;
		cIdx *= processor.zoom;

		Actogram org = processor.original;
		int fromPeriod = org.SAMPLES_PER_PERIOD / 2;
		int toPeriod = org.SAMPLES_PER_PERIOD * 2;

		FourierPeriodogram fp = new FourierPeriodogram(
				org, sIdx, cIdx, fromPeriod, toPeriod);

		float[] values = fp.getPeriodogramValues();
		float factor = org.interval.intervalIn(org.unit);
		for(int i = 0; i < values.length; i++)
			values[i] *= factor;

		int[] peaks = PeakFinder.findPeaks(values);

		String unit = org.unit.abbr;
		Plot plot = new Plot(
				"Periodogram (Fourier)",
				"Period (" + unit + ")",
				"R^2",
				fp.getPeriod(),
				values,
				Plot.LINE);
		int W = 450 + Plot.LEFT_MARGIN + Plot.RIGHT_MARGIN;
		int H = 200 + Plot.TOP_MARGIN + Plot.BOTTOM_MARGIN;
		plot.setSize(W, H);

		plot.setColor(Color.BLUE);
		plot.draw();
		plot.setColor(Color.RED);
		plot.addPoints(fp.getPeriod(), fp.getPValues(), Plot.LINE);
		plot.draw();
		plot.setColor(Color.BLACK);
		float maxV = 0;
		for(float v : values)
			if(v > maxV)
				maxV = v;
		for(int i = 0; i < 5 && i < peaks.length; i++) {
			int p = peaks[i];
			plot.drawLine(p + fromPeriod, 0, p + fromPeriod, values[p]);
			float x = p / (float)(toPeriod - fromPeriod);
			float y = (maxV - values[p]) / (float)(maxV);
			plot.addLabel(x, y, Integer.toString((fromPeriod + p)));
		}
		plot.show();
	}

	public void fitSine() {
		if(fitStart == null || fitCurr == null)
			throw new RuntimeException("Interval required");

		Point st = upper(fitStart, fitCurr);
		Point cu = lower(fitStart, fitCurr);
		if(st.y == cu.y && st.x > cu.x) {
			Point tmp = st; st = cu; cu = tmp;
		}
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;

		sIdx *= processor.zoom;
		cIdx *= processor.zoom;

		Actogram org = processor.original;

		double[] param = FitSine.fit(org, sIdx, cIdx);
		Actogram pred = FitSine.getCurve(org, param);

		pred = pred.downsample(processor.zoom);
		processor.drawInto(pred,
			new ActogramProcessor.Histogram(
				new GraphicsBackend(processor.processor.createGraphics())),
			new Color(1f, 0f, 0f, 0.5f));

		fitStart = null;
		fitCurr = null;
		repaint();

		StringBuffer msg = new StringBuffer();
		msg.append("Start:    ").append(org.getTimeStringForIndex(sIdx));
		msg.append("\n");
		msg.append("End:      ").append(org.getTimeStringForIndex(cIdx));
		msg.append("\n \n");
		msg.append("Function: min(0, a * sin(b * t + c) + d)\n \n");
		msg.append("a = ").append((float)param[0]).append("\n");
		double w = param[1] / org.interval.millis;
		msg.append("b = ").append((float)w).append("/ms").append("\n");
		msg.append("c = ").append((float)param[2]).append("\n");
		msg.append("d = ").append((float)param[3]).append("\n \n");
		double T = 2 * Math.PI / w; // in ms
		msg.append("T = " + new TimeInterval(Math.round(T)).
						intervalIn(org.unit));

		IJ.showMessage(msg.toString());
	}

	public void paintComponent(Graphics g) {
		GraphicsBackend gb = new GraphicsBackend(g);

		clearBackground(gb);
		drawCalibration(gb);

		ExternalVariable[] ev = processor.original.getExternalVariables();
		for(int i = 0; i < ev.length; i++) {
			gb.setOffsX(INT_LEFT);
			gb.setOffsY(INT_TOP + i * 25);
			ev[i].paint(gb, processor.width);
		}

		g.drawImage(processor.processor, INT_LEFT, INT_TOP_ALL, null);

		gb.setOffsX(0);
		gb.setOffsY(0);
		drawFittingInterval(gb);
		drawFPTriangle(gb);
	}

	public void paint(DrawingBackend gb) {
		clearBackground(gb);
		drawCalibration(gb);

		float offX = gb.getOffsX(), offY = gb.getOffsY();

		ExternalVariable[] ev = processor.original.getExternalVariables();
		for(int i = 0; i < ev.length; i++) {
			gb.setOffsX(offX + gb.getFactorX() * INT_LEFT);
			gb.setOffsY(offY + gb.getFactorY() * (INT_TOP + i * 25));
			ev[i].paint(gb, processor.width);
		}

		gb.setOffsX(offX + gb.getFactorX() * INT_LEFT);
		gb.setOffsY(offY + gb.getFactorY() * INT_TOP_ALL);
		processor.clearBackground(gb);
		processor.drawInto(processor.downsampled,
				new ActogramProcessor.Histogram(gb), Color.BLACK);
		gb.setOffsX(offX);
		gb.setOffsY(offY);

		drawFittingInterval(gb);
		drawFPTriangle(gb);
	}

	private void clearBackground(DrawingBackend g) {
		g.setFillColor(background.getRGB());
		g.moveTo(0, 0);
		g.fillRectangle(width, height);
	}

	private void drawFittingInterval(DrawingBackend g) {
		if(fitStart == null || fitCurr == null)
			return;

		Point st = upper(fitStart, fitCurr);
		Point cu = lower(fitStart, fitCurr);
		if(st.y == cu.y && st.x > cu.x) {
			Point tmp = st; st = cu; cu = tmp;
		}
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;


		Point s = new Point(st.x + INT_LEFT, st.y + INT_TOP_ALL);
		Point c = new Point(cu.x + INT_LEFT, cu.y + INT_TOP_ALL);

		int x0 = INT_LEFT;
		int x1 = INT_LEFT + processor.processor.getWidth();
		int sh = processor.signalHeight;

		// draw start marker
		g.setFillColor(255, 0, 0, 255);
		g.moveTo(s.x - 1, s.y - sh);
		g.fillRectangle(2, sh);

		// draw end marker
		g.moveTo(c.x - 1, c.y - sh);
		g.fillRectangle(2, sh);

		g.setFillColor(255, 0, 0, 150);

		// on the same line
		if(s.y == c.y) {
			g.moveTo(s.x, s.y - sh);
			g.fillRectangle(c.x - s.x, sh);
			return;
		}

		// on different lines
		g.moveTo(s.x, s.y - sh);
		g.fillRectangle(x1 - s.x, sh);
		g.moveTo(x0, c.y - sh);
		g.fillRectangle(c.x - x0, sh);
		int cury = s.y + processor.baselineDist;
		while(cury < c.y) {
			g.moveTo(x0, cury - sh);
			g.fillRectangle(x1 - x0, sh);
			cury += processor.baselineDist;
		}
	}

	private void drawFPTriangle(DrawingBackend g) {
		if(fpStart == null || fpCurr == null)
			return;

		Point st = upper(fpStart, fpCurr);
		Point cu = lower(fpStart, fpCurr);
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;

		Point s = new Point(st.x + INT_LEFT, st.y + INT_TOP_ALL);
		Point c = new Point(cu.x + INT_LEFT, cu.y + INT_TOP_ALL);

		g.setLineColor(color.getRGB());
		g.setLineWidth(stroke);
		g.moveTo(c.x, c.y);
		g.lineTo(s.x, s.y);
		g.lineTo(s.x, c.y);
		g.lineTo(c.x, c.y);
		int dy = (cu.y - st.y) / processor.baselineDist; // in periods
		int dx = Math.abs(cu.x - st.x);
		dx *= processor.downsampled.interval.millis;
		String v = dy + " cycles";
		String h = new TimeInterval(dx).toString();
		g.setFont(font);
		FontMetrics fm = getFontMetrics(font);
		int fh = fm.getHeight();

		// v string
		Rectangle vr = new Rectangle(
			s.x - fm.stringWidth(v) - 2,
			(s.y + c.y + fh) / 2,
			fm.stringWidth(v), fh);
		g.setFillColor(color.getRGB());
		g.moveTo(vr.x, vr.y - fh + 2);
		g.fillRectangle(vr.width, vr.height);
		g.setLineColor(text.getRGB());
		g.setFillColor(text.getRGB());
		g.moveTo(vr.x, vr.y);
		g.drawText(v);

		// h string
		Rectangle hr = new Rectangle(
			(s.x + c.x - fm.stringWidth(h)) / 2,
			c.y + fh + 2,
			fm.stringWidth(h), fh);
		g.setFillColor(color.getRGB());
		g.moveTo(hr.x, hr.y - fh + 2);
		g.fillRectangle(hr.width, hr.height);
		g.setLineColor(text.getRGB());
		g.setFillColor(text.getRGB());
		g.moveTo(hr.x, hr.y);
		g.drawText(h);
	}

	private void drawCalibration(DrawingBackend g) {
		int w = width - INT_LEFT - INT_RIGHT;

		g.setLineColor(0, 0, 0, 255);
		g.setLineWidth(1f);
		g.moveTo(INT_LEFT, 10);
		g.lineTo(width - INT_RIGHT, 10);
		for(int i = 0; i <= nSubdivisions; i++) {
			int x = INT_LEFT + (int)Math.round((float)i * w / nSubdivisions);
			g.moveTo(x, 10 - 5);
			g.lineTo(x, 10 + 5);
		}
	}

	public Point snap(Point in) {
		if(in.x < INT_LEFT || in.y < INT_TOP_ALL ||
			in.x >= width - INT_RIGHT || in.y >= height - INT_BOTTOM)
			return null;
		int bd = processor.baselineDist;
		int idx = (in.y - INT_TOP_ALL)/ bd;
		return new Point(in.x - INT_LEFT, (idx + 1) * bd);
	}

	private static Point upper(Point p1, Point p2) {
		if(p1 == null || p2 == null)
			return null;
		return p1.y <= p2.y ? p1 : p2;
	}

	private static Point lower(Point p1, Point p2) {
		if(p1 == null || p2 == null)
			return null;
		return p1.y <= p2.y ? p2 : p1;
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		if(mode == Mode.FITTING) {
			if(fitStart == null || fitCurr == null ||
				fitStart.equals(fitCurr))
				return;
			boolean doit = IJ.showMessageWithCancel(
				"Fit", "Automatically fit sine curve?");
			if(!doit)
				return;
			fitSine();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if(mode == Mode.FREERUNNING_PERIOD) {
			fpStart = null;
			fpCurr = null;
			if(feedback != null)
				feedback.periodChanged(getPeriodString());
			repaint();
		} else if(mode == Mode.FITTING) {
			fitStart = null;
			fitCurr = null;
			repaint();
		}
	}

	public void mousePressed(MouseEvent e) {
		if(mode == Mode.FREERUNNING_PERIOD) {
			fpStart = snap(e.getPoint());
			fpCurr = snap(e.getPoint());
			repaint();
		} else if(mode == Mode.FITTING) {
			fitStart = snap(e.getPoint());
			fitCurr = snap(e.getPoint());
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
		if(feedback != null)
			feedback.positionChanged(
				getPositionString(e.getX(), e.getY()));
	}

	public void mouseDragged(MouseEvent e) {
		if(feedback != null)
			feedback.positionChanged(
				getPositionString(e.getX(), e.getY()));

		if(mode == Mode.FREERUNNING_PERIOD) {
			Point tmp = snap(e.getPoint());

			fpCurr = tmp;
			if(feedback != null)
				feedback.periodChanged(getPeriodString());
			repaint();
		} else if(mode == Mode.FITTING) {
			fitCurr = snap(e.getPoint());
			repaint();
		}
	}

	public static interface Feedback {
		public void positionChanged(String pos);
		public void periodChanged(String per);
	}
}

