package actoj.gui;

import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.event.TableModelListener;

import java.io.*;
import java.util.ArrayList;

public class PreviewTable extends JTable {

	public PreviewTable(String filename) throws IOException {
		super(new PreviewTableModel(filename));
		setAutoResizeMode(AUTO_RESIZE_OFF);
	}

	private static final class PreviewTableModel implements TableModel {

		final String[] lines;
		final int columnCount;

		public PreviewTableModel(String file) throws IOException {
			this(readLines(file));
		}

		public PreviewTableModel(String[] lines) {
			this.lines = lines;
			int cols = 0;
			for(String st : lines) {
				int c = occurences(st, '\t') + 1;
				if(c > cols)
					cols = c;
			}
			this.columnCount = cols;
		}

		public void addTableModelListener(TableModelListener l) {}
		public void removeTableModelListener(TableModelListener l) {}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public int getColumnCount() {
			// one additional column for the row number
			return columnCount + 1;
		}

		public String getColumnName(int col) {
			return col == 0 ? "Row" : "Column " + col;
		}

		public int getRowCount() {
			return lines.length;
		}

		public Object getValueAt(int row, int col) {
			if(col == 0)
				return Integer.toString(row + 1);
			String line = lines[row];
			int start = col == 1 ? 0 : nthIndexOf(line, '\t', col - 1);
			int end   = col == columnCount ? line.length() : nthIndexOf(line, '\t', col);
			return line.substring(start, end);
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {}

		private static final int occurences(String st, char c) {
			int ret = 0;
			for(int i = 0; i < st.length(); i++)
				if(st.charAt(i) == c)
					ret++;
			return ret;
		}

		private static final int nthIndexOf(String st, char c, int n) {
			int nth = 0;
			for(int i = 0; i < st.length(); i++) {
				if(st.charAt(i) == c)
					nth++;
				if(nth == n)
					return i;
			}
			return -1;
		}

		public static String[] readLines(String filename) throws IOException {

			ArrayList<String> data = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(
				new FileReader(filename));
			String line = null;
			while((line = reader.readLine()) != null)
				data.add(line);
			reader.close();
			String[] ret = new String[data.size()];
			data.toArray(ret);
			return ret;
		}
	}
}

