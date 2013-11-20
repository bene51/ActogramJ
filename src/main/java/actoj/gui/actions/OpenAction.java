package actoj.gui.actions;

import ij.IJ;
import ij.io.OpenDialog;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import actoj.ActogramJ_;
import actoj.Settings;
import actoj.core.TimeInterval;
import actoj.gui.PreviewTable;
import actoj.gui.TreeView;
import actoj.io.ActogramReader;

@SuppressWarnings("serial")
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
		putValue(SMALL_ICON, new ImageIcon(ActogramJ_.class.getResource("icons/fileopen.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String dir = OpenDialog.getLastDirectory();

		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Open");
		fc.setMultiSelectionEnabled(true);
		if (dir != null)
			fc.setCurrentDirectory(new File(dir));
		int ret = fc.showOpenDialog(IJ.getInstance());
		if(ret != JFileChooser.APPROVE_OPTION)
			return;
		File[] files = fc.getSelectedFiles();
		if(files == null || files.length == 0)
			return;

                dir = fc.getCurrentDirectory().getPath()
			+ File.separator;
		OpenDialog.setLastDirectory(dir);

		PreviewDialog pd = new PreviewDialog(files, treeview);
		pd.pack();
		pd.setVisible(true);
	}

// 	public void actionPerformed(ActionEvent e) {
// 		OpenDialog od = new OpenDialog("Open...", "");
// 		String dir = od.getDirectory();
// 		String file = od.getFileName();
// 		if(dir == null || file == null)
// 			return;
// 		String path = new File(dir, file).getAbsolutePath();
// 		PreviewDialog pd = new PreviewDialog(path, treeview);
// 		pd.pack();
// 		pd.setVisible(true);
// 	}

	private static final class PreviewDialog extends JDialog
					implements ActionListener {

		private final File[] files;
		private final TreeView treeview;

		private int endRow, endCol, startCol, startRow, spp, calValue;

		private TimeInterval.Units calUnit;

		private PreviewTable preview;
		private JTextField startRowField, endRowField,
			startColField, endColField, sppField, calValueField;
		private JComboBox calUnitBox, delimBox;

		public PreviewDialog(File[] files, TreeView treeview) {
			super();
			this.files = files;
			this.treeview = treeview;
			try {
				createGUI();
			} catch(IOException e) {
				IJ.error("Can't read " + files[0]);
				e.printStackTrace();
			}
		}

		void createGUI() throws IOException {
			startCol = i(Settings.get(Settings.START_COL));
			startRow = i(Settings.get(Settings.START_ROW));
			endCol   = i(Settings.get(Settings.END_COL));
			endRow   = i(Settings.get(Settings.END_ROW));
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

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.weighty = 0.0;
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Column delimiter:"));
			delimBox = new JComboBox(new String[] {"TAB", ",", ":", ";"});
			delimBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					try {
						preview.update(files[0].getAbsolutePath(), getDelimiter());
					} catch(Exception ex) {
						IJ.error("Error reading " + files[0] + " for preview");
						ex.printStackTrace();
					}
				}
			});
			p.add(delimBox);
			gridbag.setConstraints(p, c);
			add(p);

			c.gridy++;
			c.weightx = c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;

			preview = new PreviewTable(files[0].getAbsolutePath());
			JScrollPane scroll = new JScrollPane(preview);
			scroll.setPreferredSize(new Dimension(400, 200));
			scroll.setBorder(BorderFactory.
				createTitledBorder("Preview"));
			gridbag.setConstraints(scroll, c);
			add(scroll);

			if(endCol == -1)
				endCol = preview.getColumnCount();
			if(endRow == -1)
				endRow = preview.getRowCount();

			c.weighty = 0;
			c.weightx = 0.5;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.BOTH;

			c.gridy++;
			JPanel colp = new JPanel(new GridLayout(2, 2, 5, 2));
			colp.add(new JLabel("Start column"));
			startColField = createNumberField(startCol);
			colp.add(startColField);
			colp.add(new JLabel("End column"));
			endColField = createNumberField(endCol);
			colp.add(endColField);
			colp.setBorder(BorderFactory.createTitledBorder(
				"Columns"));
			gridbag.setConstraints(colp, c);
			add(colp);

			c.gridx++;
			JPanel rowp = new JPanel(new GridLayout(2, 2, 2, 2));
			rowp.add(new JLabel("Start row"));
			startRowField = createNumberField(startRow);
			rowp.add(startRowField);
			rowp.add(new JLabel("End row"));
			endRowField = createNumberField(endRow);
			rowp.add(endRowField);
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

		private char getDelimiter() {
			switch(delimBox.getSelectedIndex()) {
			case 0: return '\t';
			case 1: return ',';
			case 2: return ':';
			case 3: return ';';
			default: throw new RuntimeException("Unknown delimiter: " + delimBox.getSelectedItem());
			}
		}

		public void readFields() {
			startCol = i(startColField.getText());
			startRow = i(startRowField.getText());
			endCol   = i(endColField.getText());
			endRow   = i(endRowField.getText());
			spp      = i(sppField.getText());
			calValue = i(calValueField.getText());
			calUnit  = (TimeInterval.Units)calUnitBox.getSelectedItem();
		}

		private static final int i(String s) {
			return Integer.parseInt(s);
		}

		private static final JTextField createNumberField(int def) {
			final JTextField ret = new JTextField(Integer.toString(def));
			ret.addKeyListener(new KeyAdapter() {
				@Override
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
				int rts = endRow == preview.getRowCount()
					? -1 : endRow;
				int cts = endCol == preview.getColumnCount()
					? -1 : endCol;
				Settings.set(Settings.START_COL, startCol);
				Settings.set(Settings.END_COL, cts);
				Settings.set(Settings.START_ROW, startRow);
				Settings.set(Settings.END_ROW, rts);
				Settings.set(Settings.SPP, spp);
				Settings.set(Settings.CAL_UNIT, calUnit.ordinal());
				Settings.set(Settings.CAL_VALUE, calValue);
			} catch(IOException e) {
				IJ.error("Error writing defaults:\n" +
					e.getMessage());
			}
		}

		public void readFile(String file, char delimiter) throws IOException {
			treeview.add(ActogramReader.readActograms(
				file, delimiter, startCol - 1, endCol - startCol + 1,
				startRow - 1, endRow - startRow + 1, spp,
				new TimeInterval(calValue, calUnit),
				calUnit));
		}

		@Override
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
				for(File f : files) {
					try {
						readFile(f.getAbsolutePath(), getDelimiter());
					} catch(Exception ex) {
						IJ.error("Error reading " + f + "\n"
								+ ex.getClass() + ": " + ex.getMessage());
						ex.printStackTrace();
					}
				}
				dispose();
			} else if(e.getActionCommand().equals("Cancel")) {
				dispose();
			}
		}
	}
}

