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

/**
 * A JComponent representing one actogram plus accompanying data, like
 * calibration. It uses a ActogramProcessor to draw the actogram itself.
 */
public class ActogramCanvas extends JPanel
			implements MouseMotionListener, MouseListener {
	
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
		Point st = upper();
		Point cu = lower();
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

	public void paintComponent(Graphics g) {
		ImageProcessor ip = processor.processor;
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.drawImage(ip.createImage(), INT_LEFT, INT_TOP, null);


		if(fpStart != null && fpCurr != null)
			drawFPTriangle(g);

		drawCalibration(g, nSubdivisions);
	}

	private void drawFPTriangle(Graphics g) {
		Point st = upper();
		Point cu = lower();
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

	private Point upper() {
		if(fpStart == null || fpCurr == null)
			return null;
		return fpStart.y <= fpCurr.y ? fpStart : fpCurr;
	}

	private Point lower() {
		if(fpStart == null || fpCurr == null)
			return null;
		return fpStart.y <= fpCurr.y ? fpCurr : fpStart;
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		fpStart = null;
		fpCurr = null;
		if(feedback != null)
			feedback.periodChanged(getPeriodString());
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		fpStart = snap(e.getPoint());
		fpCurr = snap(e.getPoint());
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
		Point tmp = snap(e.getPoint());

		fpCurr = tmp;
		if(feedback != null)
			feedback.periodChanged(getPeriodString());
		repaint();
	}

	public static interface Feedback {
		public void positionChanged(String pos);
		public void periodChanged(String per);
	}
}

