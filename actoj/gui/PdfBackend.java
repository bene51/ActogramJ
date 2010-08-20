package actoj.gui;

import java.awt.Color;

import com.lowagie.text.Font;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;

public class PdfBackend extends DrawingBackend {

	private PdfContentByte g;

	private float x = 0;
	private float y = 0;

	private float paperHeight;

	public PdfBackend(PdfContentByte g, float paperHeight) {
		super();
		this.g = g;
		setLineColor(linecolor);
		setFillColor(fillcolor);

		this.paperHeight = paperHeight;
	}

	@Override
	public void moveTo(float toX, float toY) {
		this.x = getX(toX);
		this.y = getY(toY);
		g.moveTo(x, y);
	}

	@Override
	public void lineTo(float toX, float toY) {
		g.lineTo(getX(toX), getY(toY));
		g.stroke();
		moveTo(toX, toY);
	}

	@Override
	public void drawRectangle(float w, float h) {
		w *= factorX;
		h *= factorY;
		g.rectangle(x, y - h, w, h);
		g.stroke();
	}

	@Override
	public void fillRectangle(float w, float h) {
		w *= factorX;
		h *= factorY;
		g.rectangle(x, y - h, w, h);
		g.fill();
	}

	@Override
	public void drawText(String text) {
		g.showText(text);
	}

	private float getX(float x) {
		return offsX + factorX * x;
	}

	private float getY(float y) {
		return paperHeight - offsY - factorY * y;
	}

	@Override
	public void setLineWidth(float linewidth) {
		super.setLineWidth(linewidth);
		g.setLineWidth(linewidth);
	}

	@Override
	public void setLineColor(int c) {
		super.setLineColor(c);
		int a = (int)((c & (255L << 24)) >> 24);
		int r = (c & 0xff0000) >> 16;
		int gg = (c & 0xff00) >> 8;
		int b = (c & 0xff);
		g.setColorStroke(new Color(r, gg, b, a));
	}

	@Override
	public void setFillColor(int c) {
		super.setFillColor(c);
		int a = (int)((c & (255L << 24)) >> 24);
		int r = (c & 0xff0000) >> 16;
		int gg = (c & 0xff00) >> 8;
		int b = (c & 0xff);
		g.setColorFill(new Color(r, gg, b, a));
	}

	@Override
	public void setFont(java.awt.Font f) {
		super.setFont(f);
		System.out.println("f.getFamily() = " + f.getFamily());
		BaseFont bf = null;
		try {
			bf = BaseFont.createFont(f.getFamily(), "", false);
		} catch(Exception e) {
			// falling back to Helvetica
			try {
				bf = BaseFont.createFont(BaseFont.HELVETICA, "", false);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		g.setFontAndSize(bf, f.getSize());
	}
}

