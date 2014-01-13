package actoj.gui;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import actoj.core.MarkerList;
import actoj.core.TimeInterval;

@SuppressWarnings("serial")
public class MarkerTable extends JTable {

	public MarkerTable(MarkerList markers, TimeInterval T) {
		super(new MarkerTableModel(markers, T));
		// setAutoResizeMode(AUTO_RESIZE_OFF);
	}

	public void setMarkers(MarkerList markers) {
		((MarkerTableModel)getModel()).setMarkers(markers);
	}

	public void updateTable() {
		((AbstractTableModel)getModel()).fireTableDataChanged();
	}

	private static final class MarkerTableModel extends AbstractTableModel {

		private MarkerList markers;
		private TimeInterval T;

		public MarkerTableModel(MarkerList markers, TimeInterval T) {
			this.markers = markers;
			this.T = T;
		}

		public void setMarkers(MarkerList markers) {
			this.markers = markers;
			fireTableDataChanged();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			// one additional column for the row number
			return 3;
		}

		@Override
		public String getColumnName(int col) {
			switch(col) {
			case 0: return "Row";
			case 1: return "Period";
			case 2: return "Time within period";
			default: return null;
			}
		}

		@Override
		public int getRowCount() {
			return markers.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if(col == 0)
				return Integer.toString(row + 1);
			double pos = markers.getCalibration() * markers.getPosition(row);
			double period = Math.floor(pos / T.millis);
			double millisWithinPeriod = pos - period * T.millis;
			switch(col) {
			case 1: return new Double(period);
			case 2: return new TimeInterval(millisWithinPeriod).toString();
			default: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int row, int col) {}
	}
}
