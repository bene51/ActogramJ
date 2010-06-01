package actoj.io;

import actoj.core.*;
import actoj.gui.*;

import java.util.ArrayList;

import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class PDFExporter {

	private final ImageCanvas ic;
	private final float w;
	private final float h;
	private final String file;

	public PDFExporter(ImageCanvas ic, String file, float w, float h) {
		this.ic = ic;
		this.w = w;
		this.h = h;
		this.file = file;
	}

	public PDFExporter(ImageCanvas ic, String file) {
		this.ic = ic;
		Rectangle r = PageSize.A4;
		this.w = r.width();
		this.h = r.height();
		this.file = file;
	}

	public void export() throws IOException, DocumentException {

		ArrayList<ActogramCanvas> canvasses = ic.getActograms();
		int n = canvasses.size();
		if(n == 0)
			return;

		int cols = ic.getCols();
		int rows = ic.getRows();

		// assuming all processors have the same size
		// TODO use max size instead?
		ActogramCanvas ac = canvasses.get(0);
		ActogramProcessor ap = ac.processor;

		int aWidthInPixel = ac.width;
		int aHeightInPixels = ac.height;
		int gapInPixels = (int)Math.ceil(aHeightInPixels / 10f); // ImageCanvas.PADDING;
		int widthInPixel = cols * aWidthInPixel + (cols + 1) * gapInPixels;

		float factor = w / widthInPixel;

		// calculate dimensions in mm
		float aWidthInMM = aWidthInPixel * factor;
		float aHeightInMM = aHeightInPixels * factor;
		float gapInMM = gapInPixels * factor;

		// calculate how many rows fit on a page:
		// r * aHeightInMM + r * gapInMM <= h
		// --> r <= h / (aHeightInMM + gapInMM);
		int rowsOnPage = (int)Math.floor(h / (aHeightInMM + gapInMM));
System.out.println("rowsOnPage = " + rowsOnPage);


		Document document = new Document(new Rectangle(0, 0, w, h));
		PdfWriter writer = PdfWriter.getInstance(
				document, new FileOutputStream(file));
		document.open();
		PdfContentByte content = writer.getDirectContent();

		// lower left, relative to the lower left of the page
		int gridx = 0;
		int gridy = 0;

		for(ActogramCanvas canvas : canvasses) {
			float ax = gridx * aWidthInMM + (gridx + 1) * gapInMM;
			float ay = h - ((gridy + 1) * (aHeightInMM + gapInMM));
			float aw = aWidthInMM;
			float ah = aHeightInMM;

			drawActogram(content, canvas.processor, ax, ay, aw, ah);
			gridx++;
			if(gridx >= ic.getMaxColumns()) {
				gridx = 0;
				gridy++;
				if(gridy >= rowsOnPage) {
					document.newPage();
					gridy = 0;
				}
			}
		}

		document.close();
	}

	private static final float LINE_WIDTH = 0.3f;

	public void drawActogram(PdfContentByte content, ActogramProcessor ap, float ax, float ay, float aw, float ah) {
		Actogram acto = ap.downsampled;
		int mpd = acto.SAMPLES_PER_PERIOD;
		content.setRGBColorStroke(100, 100, 100);
		content.rectangle(ax, ay, aw, ah);
		content.setLineWidth(LINE_WIDTH);
		content.stroke();

		content.setRGBColorStroke(0, 0, 0);
		content.setRGBColorFill(0, 0, 0);

		float baselineDist = ah / (ap.periods + 1);
		float signalWidth = aw / (ap.ppl * mpd);
		float signalHeight = 0.75f * baselineDist;


		int offs = 0;
		for(int d = 0; d < ap.periods; d++) {
			offs = mpd * d;
			int length = offs + mpd < acto.size() ?
					mpd : acto.size() - offs;

			// first half
			float y = ay + (ap.periods - d) * baselineDist;
			float x = ax + (ap.ppl - 1) * mpd * signalWidth;
			// draw baseline
			content.moveTo(x, y);
			content.lineTo(x + length * signalWidth, y);
			content.setLineWidth(LINE_WIDTH);
			content.stroke();
			// draw signal
			for(int i = offs; i < offs + length; i++, x += signalWidth) {
				float v = acto.get(i);
				float sh = signalHeight * Math.min(ap.uLimit, v) / ap.uLimit;
				content.rectangle(x, y, signalWidth, sh);
				content.fill();
			}

			// rest
			y -= baselineDist;
			for(int re = 1; re < ap.ppl; re++) {
				x = ax + (re - 1) * mpd * signalWidth;
				// draw baseline
				content.moveTo(x, y);
				content.lineTo(x + length * signalWidth, y);
				content.setLineWidth(LINE_WIDTH);
				content.stroke();
				// draw signal
				for(int i = offs; i < offs + length; i++, x += signalWidth) {
					float v = acto.get(i);
					float sh = signalHeight * Math.min(ap.uLimit, v) / ap.uLimit;
					content.rectangle(x, y, signalWidth, sh);
					content.fill();
				}
			}
		}
	}
}

