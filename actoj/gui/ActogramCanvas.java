package actoj.gui;

import actoj.core.*;

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
public class ActogramCanvas extends JComponent
			implements MouseMotionListener, MouseListener {
	
	public final ActogramProcessor processor;

	public final int width;
	public final int height;

	private final Feedback feedback;

	public ActogramCanvas(Actogram actogram, int zoom, float uLimit, int ppl, Feedback f) {
		super();
		this.processor = new ActogramProcessor(actogram, zoom, uLimit, ppl);
		this.feedback = f;

		ImageProcessor ip = processor.processor;
		width = ip.getWidth();
		height = ip.getHeight();
		this.setPreferredSize(new Dimension(width, height));

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public String getPositionString(int x, int y) {
		int idx = processor.getIndex(x, y);
		if(idx < 0)
			return "outside";
		Actogram a = processor.downsampled;
		return a.name + ": " + a.getTimeStringForIndex(idx);
	}

	public TimeInterval getFreerunningPeriod() {
		if(start == null || curr == null)
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

	private final BasicStroke stroke = new BasicStroke(3f);
	private final Font font = new Font("Helvetica", Font.BOLD, 14);
	private final Color text = new Color(149, 255, 139);
	private final Color color = new Color(26, 93, 0);

	public void paintComponent(Graphics g) {
		ImageProcessor ip = processor.processor;
		g.drawImage(ip.createImage(), 0, 0, null);


		if(start != null) {
			Point st = upper();
			Point cu = lower();
			int sIdx = processor.getIndex(st.x, st.y);
			if(sIdx < 0) return;
			int cIdx = processor.getIndex(cu.x, cu.y);
			if(cIdx < 0) return;

			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(color);
			g2d.setStroke(stroke);
			g2d.drawLine(st.x, st.y, cu.x,  cu.y);
			g2d.drawLine(st.x, st.y, st.x, cu.y);
			g2d.drawLine(st.x, cu.y,  cu.x,  cu.y);
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
				st.x - fm.stringWidth(v) - 2,
				(st.y + cu.y + fh) / 2,
				fm.stringWidth(v), fh);
			g2d.setColor(color);
			g2d.fillRect(vr.x, vr.y - fh + 2, vr.width, vr.height);
			g2d.setColor(text);
			g2d.drawString(v, vr.x, vr.y);

			// h string
			Rectangle hr = new Rectangle(
				(st.x + cu.x - fm.stringWidth(h)) / 2,
				cu.y + fh + 2,
				fm.stringWidth(h), fh);
			g2d.setColor(color);
			g2d.fillRect(hr.x, hr.y - fh + 2, hr.width, hr.height);
			g2d.setColor(text);
			g2d.drawString(h, hr.x, hr.y);
		}
	}

	public Point snap(Point in) {
		int bd = processor.baselineDist;
		int idx = in.y / bd;
		return new Point(in.x, (idx + 1) * bd);
	}

	private Point upper() {
		if(start == null || curr == null)
			return null;
		return start.y <= curr.y ? start : curr;
	}

	private Point lower() {
		if(start == null || curr == null)
			return null;
		return start.y <= curr.y ? curr : start;
	}

	private Point start = null;
	private Point curr = null;

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		start = null;
		curr = null;
		if(feedback != null)
			feedback.periodChanged(getPeriodString());
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		start = snap(e.getPoint());
		curr = snap(e.getPoint());
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
// 		if(e.getButton() != MouseEvent.BUTTON1)
// 			return;
		Point tmp = snap(e.getPoint());
		if(!tmp.equals(curr)) {
			curr = tmp;
			if(feedback != null)
				feedback.periodChanged(getPeriodString());
			repaint();
		}
	}

	public static interface Feedback {
		public void positionChanged(String pos);
		public void periodChanged(String per);
	}
}

