package actoj.io;

import actoj.core.*;
import actoj.gui.*;

import java.util.ArrayList;

import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Class to export the actograms of an image canvas to pdf.
 */
public class PDFExporter {

	/**
	 * Line width.
	 */
	private static final float LINE_WIDTH = 0.3f;

	/** The ImageCanvas whose actograms are to be exported. */
	private final ImageCanvas ic;

	/** The width of the final pdf. */
	private final float w;

	/** The height of the final pdf. */
	private final float h;

	/** The output file. */
	private final String file;

	/**
	 * Constructor.
	 */
	public PDFExporter(ImageCanvas ic, String file, float w, float h) {
		this.ic = ic;
		this.w = w;
		this.h = h;
		this.file = file;
	}

	/**
	 * Constructor;
	 * Output dimensions are set to A4.
	 */
	public PDFExporter(ImageCanvas ic, String file) {
		this.ic = ic;
		Rectangle r = PageSize.A4;
		this.w = r.getWidth();
		this.h = r.getHeight();
		this.file = file;
	}

	/**
	 * Export function.
	 */
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


		Document document = new Document(new Rectangle(0, 0, w, h));
		PdfWriter writer = PdfWriter.getInstance(
				document, new FileOutputStream(file));
		document.open();
		PdfContentByte content = writer.getDirectContent();

		// lower left, relative to the lower left of the page
		int gridx = 0;
		int gridy = 0;

		for(ActogramCanvas canvas : canvasses) {
			// left coord in mm
			float ax = gridx * aWidthInMM + (gridx + 1) * gapInMM;
			// upper coord in mm, w.r.t. the top of the page
			float ay = gridy * aHeightInMM + (gridy + 1) * gapInMM;

			float aw = aWidthInMM;
			float ah = aHeightInMM;

			drawActogram(content, canvas, ax, ay, aw, ah);
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

		content.restoreState();
		document.close();
	}

	/**
	 * Method to draw a single actogram.
	 */
	public void drawActogram(PdfContentByte content, ActogramCanvas ac, float ax, float ay, float aw, float ah) {

		PdfBackend ba = new PdfBackend(content, h);
		ba.setFactorX(aw / ac.width);
		ba.setFactorY(ah / ac.height);
		ba.setOffsX(ax);
		ba.setOffsY(ay);

		ac.paint(ba);
	}
}

