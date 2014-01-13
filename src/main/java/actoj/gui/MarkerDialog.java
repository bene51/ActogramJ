package actoj.gui;

import ij.IJ;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import actoj.core.Actogram;
import actoj.core.MarkerList;
import actoj.core.TimeInterval;

@SuppressWarnings("serial")
public class MarkerDialog extends JDialog {

	public static final void run(Actogram ag, int i) {
		new MarkerDialog(ag, i);
	}

	private Actogram a;

	private JComboBox variableBox;
	private ColorButton colorButton;
	private JTextField linewidthTF;
	private JCheckBox showRegressionCB;
	private JSpinner regressionInPlotSpinner;
	private MarkerTable mTable;

	private MarkerList curr;

	private MarkerList[] old;

	public MarkerDialog(Actogram ag, int idx) {
		super(IJ.getInstance(), true);
		setTitle("Edit Marker");
		this.a = ag;

		int nMarkers = ag.nMarkers();
		if(idx < 0) idx = 0;
		if(idx >= nMarkers) idx = nMarkers - 1;

		// Make a deep copy
		old = new MarkerList[nMarkers];
		for(int i = 0; i < nMarkers; i++)
			old[i] = new MarkerList(ag.getMarker(i));
		curr = ag.getMarker(idx);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(gridbag);

		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);


		c.gridx = c.gridy = 0;
		variableBox = new JComboBox();
		for(int i = 0; i < nMarkers; i++)
			variableBox.addItem(ag.getMarker(i));

		variableBox.setSelectedIndex(idx);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(variableBox, c);

		ColorChangeListener cl = new ColorChangeListener() {
			@Override
			public void colorChanged(Color c) {
				curr.setColor(colorButton.getTheColor());
			}
		};

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		JLabel l = new JLabel("Marker color");
		getContentPane().add(l, c);

		c.gridx++;
		colorButton = new ColorButton(curr.getColor(), 30, 10, cl);
		getContentPane().add(colorButton, c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("Line width"), c);

		c.gridx++;
		linewidthTF = new JTextField(Float.toString(curr.getLinewidth()));
		linewidthTF.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				curr.setLinewidth((float)Double.parseDouble(linewidthTF.getText()));
			}
		});
		getContentPane().add(linewidthTF, c);

		mTable = new MarkerTable(curr, new TimeInterval(ag.SAMPLES_PER_PERIOD * a.interval.millis));
		JScrollPane scroll = new JScrollPane(mTable);
		scroll.setPreferredSize(new Dimension(300, 100));
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.5;
		c.weightx = 0.5;
		getContentPane().add(scroll, c);
		mTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					int indices[] = mTable.getSelectedRows();
					if(indices == null || indices.length == 0)
						return;
					boolean hasRegression = curr.getRegression() != null;
					for(int i = indices.length - 1; i >= 0; i--) {
						int idx = indices[i];
						curr.removePosition(idx);
					}
					if(hasRegression)
						curr.calculateRegression(new TimeInterval(a.interval.millis * a.SAMPLES_PER_PERIOD));
					mTable.updateTable();
				}
			}
		});

		final JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem("Remove marker(s)");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int indices[] = mTable.getSelectedRows();
				if(indices == null || indices.length == 0)
					return;
				boolean hasRegression = curr.getRegression() != null;
				for(int i = indices.length - 1; i >= 0; i--) {
					int idx = indices[i];
					curr.removePosition(idx);
				}
				if(hasRegression)
					curr.calculateRegression(new TimeInterval(a.interval.millis * a.SAMPLES_PER_PERIOD));
				mTable.updateTable();
			}
		});
		popup.add(item);

		mTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				int row = mTable.rowAtPoint(e.getPoint());
				if(row < 0)
					return;
				if(!mTable.isRowSelected(row))
					mTable.getSelectionModel().setSelectionInterval(row, row);
				if(e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});


		c.gridy++;
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = c.weighty = 0;
		c.anchor = GridBagConstraints.WEST;
		showRegressionCB = new JCheckBox("Show regression");
		showRegressionCB.setSelected(curr.getRegression() != null);
		showRegressionCB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(showRegressionCB.isSelected())
					curr.calculateRegression(new TimeInterval(a.interval.millis * a.SAMPLES_PER_PERIOD));
				else
					curr.invalidateRegression();
			}
		});
		getContentPane().add(showRegressionCB, c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		l = new JLabel("Show regression in plot no.");
		getContentPane().add(l, c);

		c.gridx++;
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				curr.getIndexInPlotPerLine(),
                0,  //min
                10, //max
                1); //step
		regressionInPlotSpinner = new JSpinner(spinnerModel);
		regressionInPlotSpinner.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = (Integer)regressionInPlotSpinner.getValue();
				curr.setIndexInPlotPerLine(value);
			}
		});

		getContentPane().add(regressionInPlotSpinner, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton b = new JButton("Cancel");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
				dispose();
			}
		});
		p.add(b);
		b = new JButton("Ok");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		p.add(b);
		getContentPane().add(p, c);


		variableBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				variableChanged((MarkerList)variableBox.getSelectedItem());
			}
		});

		pack();
		variableChanged(curr);
		setVisible(true);
	}

	private void cancel() {
		for(int i = 0; i < a.nMarkers(); i++) {
			MarkerList m = a.getMarker(i);
			MarkerList o = old[i];
			m.setColor(o.getColor());
			m.setIndexInPlotPerLine(o.getIndexInPlotPerLine());
			m.setLinewidth(o.getLinewidth());
			m.setPositions(o.getPositions());
			if(o.getRegression() != null && m.getRegression() == null)
				m.calculateRegression(new TimeInterval(a.interval.millis * a.SAMPLES_PER_PERIOD));
			else if(o.getRegression() == null && m.getRegression() != null)
				m.invalidateRegression();
		}
	}

	private void variableChanged(MarkerList m) {
		this.curr = m;
		colorButton.setTheColor(curr.getColor());
		showRegressionCB.setSelected(curr.getRegression() != null);
		linewidthTF.setText(Float.toString(curr.getLinewidth()));
		regressionInPlotSpinner.setValue(curr.getIndexInPlotPerLine());
		mTable.setMarkers(m);
	}

	public interface ColorChangeListener {
		public void colorChanged(Color c);
	}

	public static class ColorButton extends JButton implements Icon, ActionListener {
		private Color color;
		private int iconw, iconh;
		private ColorChangeListener listener;

		public ColorButton(Color color, int iconw, int iconh, ColorChangeListener l) {
			this.color = color;
			this.iconw = iconw;
			this.iconh = iconh;
			setIcon(this);
			addActionListener(this);
			this.listener = l;
		}

		public Color getTheColor() { return color; }
		public void setTheColor(Color color) {
			this.color = color;
			repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Color res = JColorChooser.showDialog(this, "Change color", color);
			if (res != null) {
				color = res;
				repaint();
				if(this.listener != null)
					listener.colorChanged(color);
			}
		}

		// Icon methods
		@Override
		public int getIconWidth() { return iconw; }
		@Override
		public int getIconHeight() { return iconh; }
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color old = g.getColor();
			g.setColor(color);
			g.fillRect(x, y, iconw, iconh);
			g.setColor(old);
		}
	}
}

