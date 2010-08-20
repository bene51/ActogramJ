package actoj.gui;

public abstract class DrawingBackend {

	protected float linewidth = 1f;
	protected int linecolor = 0;
	protected int fillcolor = 0;
	protected float offsX = 0;
	protected float offsY = 0;

	public void setLineWidth(float linewidth) {
		this.linewidth = linewidth;
	}

	public float getLineWidth() {
		return linewidth;
	}

	public void setLineColor(int linecolor) {
		this.linecolor = linecolor;
	}

	public void setLineColor(int r, int g, int b, int a) {
		setLineColor(new java.awt.Color(r, g, b, a).getRGB());
	}

	public float getLineColor() {
		return linecolor;
	}

	public void setFillColor(int fillcolor) {
		this.fillcolor = fillcolor;
	}

	public void setFillColor(int r, int g, int b, int a) {
		setFillColor(new java.awt.Color(r, g, b, a).getRGB());
	}

	public float getFillColor() {
		return fillcolor;
	}

	public void setOffsX(float offsX) {
		this.offsX = offsX;
	}

	public float getOffsX() {
		return offsX;
	}

	public void setOffsY(float offsY) {
		this.offsY = offsY;
	}

	public float getOffsY() {
		return offsY;
	}

	public abstract void moveTo(float x, float y);

	public abstract void lineTo(float x, float y);

	public abstract void drawRectangle(float w, float h);

	public abstract void fillRectangle(float w, float h);
}
