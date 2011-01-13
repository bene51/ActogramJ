package actoj.io;

import actoj.util.FloatArray;
import actoj.core.TimeInterval;
import actoj.core.Actogram;
import actoj.core.ActogramGroup;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class to read an actogram file.
 */
public class ActogramReader {

	/**
	 * Reads and returns the ActogramGroup from the specified file.
	 * Actograms are expected in columns.
	 * @param file     The file to read.
	 * @param fromCol  The first data column.
	 * @param numCols  The number of actograms/data columns.
	 * @param fromLine The first line to read.
	 * @param numLines The number of lines to read.
	 * @param spp      Samples per period in the file.
	 * @param cal      The interval between measurements.
	 * @param unit     The unit of the interval.
	 */
	public static ActogramGroup readActograms(String file,
		int fromCol, int numCols, int fromLine, int numLines,
		int spp, TimeInterval cal, TimeInterval.Units unit) throws FileNotFoundException, IOException {

		BufferedReader bf = new BufferedReader(new FileReader(file));
		String line = null;
		for(int i = 0; i < fromLine; i++)
			bf.readLine();

		FloatArray[] arr = new FloatArray[numCols];
		for(int i = 0; i < arr.length; i++)
			arr[i] = new FloatArray();

		int l = 0;
		while((line = bf.readLine()) != null && l < numLines) {
			String[] sp = line.split("\t");
			for(int i = 0; i < numCols && i + fromCol < sp.length; i++) {
				if(i + fromCol >= sp.length)
					throw new IllegalArgumentException("Line " + (l + 1) + ": Cannot read column " + (i + fromCol));
				try {
					arr[i].add(Float.parseFloat(sp[i + fromCol]));
				} catch(NumberFormatException e) {
					throw new IllegalArgumentException(
						"Line " + (l + 1) + ": Cannot convert "
						+ sp[i + fromCol] + " to a number");
				}
			}
			l++;
		}
		bf.close();

		Actogram[] ret = new Actogram[numCols];
		for(int i = 0; i < ret.length; i++)
			ret[i] = new Actogram("#" + i, arr[i].toArray(), spp, cal, unit);

		return new ActogramGroup(new File(file).getName(), ret);
	}
}

