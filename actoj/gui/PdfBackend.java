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

import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;

public class PdfBackend extends DrawingBackend {

	private PdfContentByte g;

	private float x = 0;
	private float y = 0;

	private float paperHeight;
	private int alphaStroke = 255;
	private int alphaFill = 255;

	private BaseFont baseFont;
	private float fontSize;

	public PdfBackend(PdfContentByte g, float paperHeight) {
		super();
		this.g = g;
		setFont(this.font);
		setLineWidth(linewidth);
		g.saveState();
		setOpacities();
		this.paperHeight = paperHeight;
	}

	private void setOpacities() {
		g.restoreState();
		g.setColorStroke(new Color(linecolor));
		g.setColorFill(new Color(fillcolor));
		g.setFontAndSize(baseFont, fontSize);
		PdfGState gstate = new PdfGState();
		gstate.setStrokeOpacity(alphaStroke / 255f);
		gstate.setFillOpacity(alphaFill / 255f);
		g.saveState();
		g.setGState(gstate);
	}

	private void resetOpacities() {
		g.restoreState();
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
		g.beginText();

		g.setTextMatrix(x, y);
		g.setFontAndSize(baseFont, fontSize);
		g.showText(text);

		g.endText();
	}

	private float getX(float x) {
		return offsX + factorX * x;
	}

	private float getY(float y) {
		return paperHeight - offsY - factorY * y;
	}

	@Override
	public void setLineWidth(float linewidth) {
		linewidth *= factorX;
		super.setLineWidth(linewidth);
		g.setLineWidth(linewidth);
	}

	@Override
	public void setLineColor(int c) {
		super.setLineColor(c);
		this.alphaStroke = (int)((c & (255L << 24)) >> 24);
		setOpacities();
	}

	@Override
	public void setFillColor(int c) {
		super.setFillColor(c);
		this.alphaFill = (int)((c & (255L << 24)) >> 24);
		setOpacities();
	}

	@Override
	public void setFont(java.awt.Font f) {
		super.setFont(f);
		this.baseFont = null;
		this.fontSize = f.getSize() * factorX;
		try {
			baseFont = BaseFont.createFont(f.getFamily(), BaseFont.CP1252, true);
		} catch(Exception e) {
			// falling back to Helvetica
			try {
				baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, true);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		g.setFontAndSize(baseFont, fontSize);
	}
}

