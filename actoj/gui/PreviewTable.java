package actoj.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
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
			for(int i = 0; i < lines.length; i++) {
				lines[i] = lines[i] + "\t";
				int c = occurences(lines[i], '\t');
				if(c > cols)
					cols = c;
			}
			this.columnCount = cols;
		}

		@Override
		public void addTableModelListener(TableModelListener l) {}
		@Override
		public void removeTableModelListener(TableModelListener l) {}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			// one additional column for the row number
			return columnCount + 1;
		}

		@Override
		public String getColumnName(int col) {
			return col == 0 ? "Row" : "Column " + col;
		}

		@Override
		public int getRowCount() {
			return lines.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if(col == 0)
				return Integer.toString(row + 1);
			String line = lines[row];
			int start = col == 1 ? 0 : nthIndexOf(line, '\t', col - 1);
			int end   = nthIndexOf(line, '\t', col);
			return start == -1 || end == -1 ? "" :
				line.substring(start, end);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
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

