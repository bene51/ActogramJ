package actoj.gui;

import actoj.core.*;

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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ij.process.ImageProcessor;
import ij.IJ;

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

	/** Distance between bottom border and the actogram. */
	private int INT_BOTTOM = 5;

	/** Stroke for the freerunning period triangle */
	private final BasicStroke stroke = new BasicStroke(3f);

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
				Feedback f) {
		super();
		this.processor = new ActogramProcessor(actogram, zoom, uLimit, ppl);
		this.feedback = f;
		this.nSubdivisions = subd;

		ImageProcessor ip = processor.processor;
		width = ip.getWidth() + INT_LEFT + INT_RIGHT;
		height = ip.getHeight() + INT_TOP + INT_BOTTOM;
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
		if(x < INT_LEFT || y < INT_TOP || x >= width - INT_RIGHT || y >= height - INT_BOTTOM)
			return "outside";
		int idx = processor.getIndex(x - INT_LEFT, y - INT_TOP);
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

		System.out.println("Fit sinusoidal curve between " +
			sIdx + " and " + cIdx);
	}

	public void paintComponent(Graphics g) {
		ImageProcessor ip = processor.processor;
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.drawImage(ip.createImage(), INT_LEFT, INT_TOP, null);


		drawFPTriangle(g);

		drawFittingInterval(g);

		drawCalibration(g, nSubdivisions);
	}

	private void drawFittingInterval(Graphics g) {
		if(fitStart == null || fitCurr == null)
			return;

		Graphics2D g2d = (Graphics2D) g;
		Point st = upper(fitStart, fitCurr);
		Point cu = lower(fitStart, fitCurr);
		if(st.y == cu.y && st.x > cu.x) {
			Point tmp = st; st = cu; cu = tmp;
		}
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;


		Point s = new Point(st.x + INT_LEFT, st.y + INT_TOP);
		Point c = new Point(cu.x + INT_LEFT, cu.y + INT_TOP);

		int x0 = INT_LEFT;
		int x1 = INT_LEFT + processor.processor.getWidth() - 1;
		int sh = processor.signalHeight;

		// draw start marker
		g2d.setColor(Color.RED);
		g2d.fillRect(s.x-1, s.y - sh, 2, sh);

		// draw end marker
		g2d.fillRect(c.x-1, c.y - sh, 2, sh);

		g2d.setColor(new Color(1f, 0f, 0f, 0.5f));

		// on the same line
		if(s.y == c.y) {
			g2d.fillRect(s.x, s.y - sh, c.x - s.x, sh);
			return;
		}

		// on different lines
		g2d.fillRect(s.x, s.y - sh, x1 - s.x, sh);
		g2d.fillRect(x0,  c.y - sh, c.x - x0, sh);
		int cury = s.y + processor.baselineDist;
		while(cury < c.y) {
			g2d.fillRect(x0, cury - sh, x1 - x0, sh);
			cury += processor.baselineDist;
		}
	}

	private void drawFPTriangle(Graphics g) {
		if(fpStart == null || fpCurr == null)
			return;

		Point st = upper(fpStart, fpCurr);
		Point cu = lower(fpStart, fpCurr);
		int sIdx = processor.getIndex(st.x, st.y);
		if(sIdx < 0) return;
		int cIdx = processor.getIndex(cu.x, cu.y);
		if(cIdx < 0) return;

		Point s = new Point(st.x + INT_LEFT, st.y + INT_TOP);
		Point c = new Point(cu.x + INT_LEFT, cu.y + INT_TOP);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(color);
		g2d.setStroke(stroke);
		g2d.drawLine(s.x, s.y, c.x, c.y);
		g2d.drawLine(s.x, s.y, s.x, c.y);
		g2d.drawLine(s.x, c.y, c.x, c.y);
		int dy = (cu.y - st.y) / processor.baselineDist; // in periods
		int dx = Math.abs(cu.x - st.x);
		dx *= processor.downsampled.interval.millis;
		String v = dy + "periods";
		String h = new TimeInterval(dx).toString();
		g2d.setFont(font);
		FontMetrics fm = getFontMetrics(font);
		int fh = fm.getHeight();

		// v string
		Rectangle vr = new Rectangle(
			s.x - fm.stringWidth(v) - 2,
			(s.y + c.y + fh) / 2,
			fm.stringWidth(v), fh);
		g2d.setColor(color);
		g2d.fillRect(vr.x, vr.y - fh + 2, vr.width, vr.height);
		g2d.setColor(text);
		g2d.drawString(v, vr.x, vr.y);

		// h string
		Rectangle hr = new Rectangle(
			(s.x + c.x - fm.stringWidth(h)) / 2,
			c.y + fh + 2,
			fm.stringWidth(h), fh);
		g2d.setColor(color);
		g2d.fillRect(hr.x, hr.y - fh + 2, hr.width, hr.height);
		g2d.setColor(text);
		g2d.drawString(h, hr.x, hr.y);
	}

	private void drawCalibration(Graphics gr, int subd) {
		int w = width - INT_LEFT - INT_RIGHT;

		Graphics2D g = (Graphics2D)gr;
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1f));
		g.drawLine(INT_LEFT, 10, width - INT_RIGHT, 10);
		for(int i = 0; i <= subd; i++) {
			int x = INT_LEFT + (int)Math.round((float)i * w / subd);
			g.drawLine(x, 10 - 5, x, 10 + 5);
		}
	}

	public Point snap(Point in) {
		if(in.x < INT_LEFT || in.y < INT_TOP ||
			in.x >= width - INT_RIGHT || in.y >= height - INT_BOTTOM)
			return null;
		int bd = processor.baselineDist;
		int idx = (in.y - INT_TOP)/ bd;
		return new Point(in.x - INT_LEFT, (idx + 1) * bd);
	}

	private static Point upper(Point p1, Point p2) {
		if(p1 == null || p2 == null)
			return null;
		return p1.y <= p2.y ? p1 : p2;
	}

	private Point lower(Point p1, Point p2) {
		if(p1 == null || p2 == null)
			return null;
		return p1.y <= p2.y ? p2 : p1;
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		if(mode == Mode.FITTING) {
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
		} else if(mode == Mode.FITTING) {
			fitStart = snap(e.getPoint());
			fitCurr = snap(e.getPoint());
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

