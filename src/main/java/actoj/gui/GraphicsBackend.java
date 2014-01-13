package actoj.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class GraphicsBackend extends DrawingBackend {

	private Graphics2D g;

	private Color lColor, fColor;

	private int x = 0;
	private int y = 0;

	public GraphicsBackend(Graphics g) {
		super();
		this.g = (Graphics2D)g;
		setLineColor(linecolor);
		setFillColor(fillcolor);
	}

	@Override
	public void moveTo(float x, float y) {
		this.x = getX(x);
		this.y = getY(y);
	}

	@Override
	public void lineTo(float toX, float toY) {
		g.setColor(lColor);
		g.drawLine(x, y, getX(toX), getY(toY));
		moveTo(toX, toY);
	}

	@Override
	public void fillTriangle(float x0, float y0, float x1, float y1, float x2, float y2) {
		Polygon p = new Polygon(new int[] {
				(int)x0, (int)x1, (int)x2}, new int[] {(int)y0, (int)y1, (int)y2}, 3);
		g.setColor(fColor);
		g.fill(p);
	}

	@Override
	public void drawTriangle(float x0, float y0, float x1, float y1, float x2, float y2) {
		Polygon p = new Polygon(new int[] {
				(int)x0, (int)x1, (int)x2}, new int[] {(int)y0, (int)y1, (int)y2}, 3);
		g.setColor(lColor);
		g.draw(p);
	}

	@Override
	public void drawRectangle(float w, float h) {
		g.setColor(lColor);
		g.drawRect(x, y, (int)w - 1, (int) h - 1);
	}

	@Override
	public void fillRectangle(float w, float h) {
		g.setColor(fColor);
		g.fillRect(x, y, (int)w, (int) h);
	}

	@Override
	public void drawOval(float w, float h) {
		g.setColor(lColor);
		g.drawOval(x, y, (int)w - 1, (int) h - 1);
	}

	@Override
	public void fillOval(float w, float h) {
		g.setColor(fColor);
		g.fillOval(x, y, (int)w, (int) h);
	}

	@Override
	public void drawText(String text) {
		g.setColor(lColor);
		g.drawString(text, x, y);
	}

	private int getX(float x) {
		return (int)(offsX + x);
	}

	private int getY(float y) {
		return (int)(offsY + y);
	}

	@Override
	public void setLineWidth(float linewidth) {
		super.setLineWidth(linewidth);
		g.setStroke(new BasicStroke(
				linewidth,
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_MITER,
				10.0f,
				linedashpattern,
				0));
	}

	@Override
	public void setLineDashPattern(float[] pattern) {
		super.setLineDashPattern(pattern);
		g.setStroke(new BasicStroke(
				linewidth,
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_MITER,
				10.0f,
				linedashpattern,
				0));
	}

	@Override
	public void setLineColor(int c) {
		super.setLineColor(c);
		int a = (int)((c & (255L << 24)) >> 24);
		int r = (c & 0xff0000) >> 16;
		int g = (c & 0xff00) >> 8;
		int b = (c & 0xff);
		this.lColor = new Color(r, g, b, a);
	}

	@Override
	public void setFillColor(int c) {
		super.setFillColor(c);
		int a = (int)((c & (255L << 24)) >> 24);
		int r = (c & 0xff0000) >> 16;
		int g = (c & 0xff00) >> 8;
		int b = (c & 0xff);
		this.fColor = new Color(r, g, b, a);
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		g.setFont(f);
	}

	@Override
	public void clip(float x, float y, float w, float h) {
		g.clipRect(getX(x), getY(y), (int)w, (int)h);
	}

	@Override
	public void resetClip() {
		g.setClip(null);
	}
}

