package actoj.gui;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;

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
		g.setStroke(new BasicStroke(linewidth));
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
}

