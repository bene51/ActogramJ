package actoj.gui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import java.awt.*;

import ij.process.ImageProcessor;

public class ImageCanvas extends JPanel {

	public static final int PADDING = 20;

	private ArrayList<ActogramCanvas> actograms =
			new ArrayList<ActogramCanvas>();

	private int maxColumns = 4;

	private int nRows = 0;
	private int nCols = 0;

	private int nSubdivisions = 8;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();

	public ImageCanvas() {
		setLayout(gridbag);
		c.gridx = maxColumns - 1; c.gridy = -1;
		c.insets = new Insets(PADDING, PADDING, PADDING, PADDING);
	}

	public ArrayList<ActogramCanvas> getActograms() {
		return actograms;
	}

	public int getRows() {
		return nRows;
	}

	public int getCols() {
		return nCols;
	}

	public int getMaxColumns() {
		return maxColumns;
	}

	public void setMaxColumns(int n) {
		this.maxColumns = n;
	}

	public void setCalibrationSubdivisions(int n) {
		this.nSubdivisions = n;
		for(ActogramCanvas c : actograms)
			c.setNSubdivisions(n);
	}

	public int getCalibrationSubdivisions() {
		return nSubdivisions;
	}

	public void addActogram(ActogramCanvas a) {
		actograms.add(a);
		c.gridx++;
		if(nCols < maxColumns)
			nCols++;

		if(c.gridx == maxColumns) {
			c.gridx = 0;
			c.gridy++;
			nRows++;
		}
		gridbag.setConstraints(a, c);
		add(a);
	}

	public void clear() {
		actograms.clear();
		removeAll();
		c.gridx = maxColumns - 1;
		c.gridy = -1;
		nRows = nCols = 0;
	}

	public void addAll(Collection<ActogramCanvas> a) {
		for(ActogramCanvas ac : a)
			addActogram(ac);
	}
}

