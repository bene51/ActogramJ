package actoj.gui.actions;

import actoj.Settings;
import actoj.ActogramJ_;
import actoj.core.TimeInterval;
import actoj.core.ActogramGroup;
import actoj.io.ActogramReader;
import actoj.gui.TreeView;
import actoj.gui.PreviewTable;

import ij.IJ;
import ij.io.OpenDialog;
import ij.gui.GenericDialog;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.io.IOException;
import java.io.File;


public class OpenAction extends AbstractAction {
	// 	ACCELERATOR_KEY
	// 	ACTION_COMMAND_KEY
	// 	DEFAULT
	// 	LONG_DESCRIPTION
	// 	MNEMONIC_KEY
	// 	NAME
	// 	SHORT_DESCRIPTION
	// 	SMALL_ICON 

	private final TreeView treeview;

	public OpenAction(TreeView treeview) {
		this.treeview = treeview;
		putValue(SHORT_DESCRIPTION, "Open Actograms");
		putValue(LONG_DESCRIPTION, "Open a group of actograms from file");
		putValue(NAME, "Open");
		putValue(SMALL_ICON, new ImageIcon(
			ActogramJ_.class.getResource("icons/fileopen.png")));
	}

	public void actionPerformed(ActionEvent e) {
		OpenDialog od = new OpenDialog("Open...", "");
		String dir = od.getDirectory();
		String file = od.getFileName();
		if(dir == null || file == null)
			return;
		String path = new File(dir, file).getAbsolutePath();
		PreviewDialog pd = new PreviewDialog(path, treeview);
		pd.pack();
		pd.setVisible(true);
	}

	private static final class PreviewDialog extends JDialog
					implements ActionListener {

		private final String file;
		private final TreeView treeview;

		private int numRows, numCols, startCol, startRow, spp, calValue;

		private TimeInterval.Units calUnit;

		private PreviewTable preview;
		private JTextField startRowField, numRowField,
			startColField, numColField, sppField, calValueField;
		private JComboBox calUnitBox;

		public PreviewDialog(String file, TreeView treeview) {
			super();
			this.file = file;
			this.treeview = treeview;
			try {
				createGUI();
			} catch(IOException e) {
				IJ.error("Can't read " + file);
				e.printStackTrace();
			}
		}

		void createGUI() throws IOException {
			startCol = i(Settings.get(Settings.START_COL));
			startRow = i(Settings.get(Settings.START_ROW));
			numCols  = i(Settings.get(Settings.COL_COUNT));
			numRows  = i(Settings.get(Settings.ROW_COUNT));
			spp      = i(Settings.get(Settings.SPP));
			calValue = i(Settings.get(Settings.CAL_VALUE));

			calUnit  = TimeInterval.Units.values()[
				i(Settings.get(Settings.CAL_UNIT))];

			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gridbag);

			c.gridx = c.gridy = 0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(10, 5, 5, 5);

			c.weightx = c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;

			preview = new PreviewTable(file);
			JScrollPane scroll = new JScrollPane(preview);
			scroll.setPreferredSize(new Dimension(400, 200));
			scroll.setBorder(BorderFactory.
				createTitledBorder("Preview"));
			gridbag.setConstraints(scroll, c);
			add(scroll);

			if(numCols == -1)
				numCols = preview.getColumnCount();
			if(numRows == -1)
				numRows = preview.getRowCount();

			c.weighty = 0;
			c.weightx = 0.5;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.BOTH;

			c.gridy++;
			JPanel colp = new JPanel(new GridLayout(2, 2, 5, 2));
			colp.add(new JLabel("Start column"));
			startColField = createNumberField(startCol);
			colp.add(startColField);
			colp.add(new JLabel("Column count"));
			numColField = createNumberField(numCols);
			colp.add(numColField);
			colp.setBorder(BorderFactory.createTitledBorder(
				"Columns"));
			gridbag.setConstraints(colp, c);
			add(colp);

			c.gridx++;
			JPanel rowp = new JPanel(new GridLayout(2, 2, 2, 2));
			rowp.add(new JLabel("Start row"));
			startRowField = createNumberField(startRow);
			rowp.add(startRowField);
			rowp.add(new JLabel("Row count"));
			numRowField = createNumberField(numRows);
			rowp.add(numRowField);
			rowp.setBorder(BorderFactory.createTitledBorder(
				"Rows"));
			gridbag.setConstraints(rowp, c);
			add(rowp);

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = GridBagConstraints.REMAINDER;
			JPanel calp = new JPanel(new GridLayout(2, 3, 2, 2));
			calp.add(new JLabel("Samples per period"));
			sppField = createNumberField(spp);
			calp.add(sppField);
			calp.add(new JPanel());
			calp.add(new JLabel("Interval duration"));
			calValueField = createNumberField(calValue);
			calp.add(calValueField);
			calUnitBox = new JComboBox(
				TimeInterval.Units.values());
			calUnitBox.setSelectedItem(calUnit);
			calp.add(calUnitBox);
			calp.setBorder(BorderFactory.createTitledBorder(
				"Calibration"));
			gridbag.setConstraints(calp, c);
			add(calp);

			JPanel buttons = new JPanel(new FlowLayout());
			JButton button = new JButton("Cancel");
			button.addActionListener(this);
			buttons.add(button);
			button = new JButton("Ok");
			button.addActionListener(this);
			buttons.add(button);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(buttons, c);
			add(buttons);
		}

		public void readFields() {
			startCol = i(startColField.getText());
			startRow = i(startRowField.getText());
			numCols = i(numColField.getText());
			numRows = i(numRowField.getText());
			spp = i(sppField.getText());
			calValue = i(calValueField.getText());
			calUnit = (TimeInterval.Units)calUnitBox.getSelectedItem();
		}

		private static final int i(String s) {
			return Integer.parseInt(s);
		}

		private static final JTextField createNumberField(int def) {
			final JTextField ret = new JTextField(Integer.toString(def));
			ret.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					String t = ret.getText();
					char c = t.charAt(t.length() - 1);
					if(!Character.isDigit(c))
						ret.setText(t.substring(0, t.length() - 1));
				}
			});
			return ret;
		}

		public void saveDefaults() {
			try {
				int rts = numRows == preview.getRowCount()
					? -1 : numRows;
				Settings.set(Settings.START_COL, startCol);
				Settings.set(Settings.COL_COUNT, numCols);
				Settings.set(Settings.START_ROW, startRow);
				Settings.set(Settings.ROW_COUNT, rts);
				Settings.set(Settings.SPP, spp);
				Settings.set(Settings.CAL_UNIT, calUnit.ordinal());
				Settings.set(Settings.CAL_VALUE, calValue);
			} catch(IOException e) {
				IJ.error("Error writing defaults:\n" +
					e.getMessage());
			}
		}

		public void readFile() throws IOException {
			treeview.add(ActogramReader.readActograms(
				file, startCol - 1, numCols,
				startRow - 1, numRows, spp,
				new TimeInterval(calValue, calUnit),
				calUnit));
		}

		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Ok")) {
				try {
					readFields();
				} catch(NumberFormatException ex) {
					IJ.error("Cannot read input fields\n" +
						"Only integer numbers are allowed:\n" +
						ex.getMessage());
				}
				saveDefaults();
				try {
					readFile();
				} catch(IOException ex) {
					IJ.error("Error reading " + file + "\n"
						+ ex.getMessage());
					ex.printStackTrace();
				}
				dispose();
			} else if(e.getActionCommand().equals("Cancel")) {
				dispose();
			}
		}
	}
}

