package actoj.gui;

import ij.gui.GenericDialog;
import ij.IJ;

import actoj.core.ExternalVariable;
import actoj.core.ActogramGroup;
import actoj.core.Actogram;
import actoj.util.Filters;

import java.util.ArrayList;
import java.util.List;

import java.text.DecimalFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ExternalVariablesDialog extends JDialog {

	public static final void run(ActogramGroup ag, int i) {
		new ExternalVariablesDialog(ag, i);
	}

	private ActogramGroup ag;

	private VarPanel varPanel;
	private JComboBox variableBox;
	private ColorButton onColor;
	private ColorButton offColor;
	private JButton addInterval;
	private ExternalVariable curr;

	private ExternalVariable[] exts;

	public ExternalVariablesDialog(ActogramGroup ag, int idx) {
		super(IJ.getInstance(), true);
		setTitle("External variables");
		this.ag = ag;

		if(ag.size() == 0)
			throw new IllegalArgumentException("Empty ActogramGroup");

		ExternalVariable[] old = ag.get(0).getExternalVariables();
		if(idx < 0) idx = 0;
		if(idx >= old.length) idx = old.length - 1;

		// Make a deep copy
		exts = new ExternalVariable[old.length];
		for(int i = 0; i < old.length; i++)
			exts[i] = new ExternalVariable(old[i]);
		curr = exts[idx];

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(gridbag);

		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 5, 5, 5);


		c.gridx = c.gridy = 0;
		variableBox = new JComboBox(exts);
		variableBox.setSelectedIndex(idx);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(variableBox, c);

		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		varPanel = new VarPanel(curr, 300);
		getContentPane().add(varPanel, c);


		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		addInterval = new JButton("Add a new interval");
		addInterval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewInterval();
			}
		});
		getContentPane().add(addInterval, c);

		ColorChangeListener cl = new ColorChangeListener() {
			public void colorChanged(Color c) {
				curr.onColor = onColor.getTheColor().getRGB();
				curr.offColor = offColor.getTheColor().getRGB();
				varPanel.repaint();
			}
		};

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		JLabel l = new JLabel("On color");
		getContentPane().add(l, c);

		c.gridx++;
		onColor = new ColorButton(new Color(curr.onColor), 30, 10, cl);
		getContentPane().add(onColor, c);

		c.gridx++;
		l = new JLabel("Off color");
		getContentPane().add(l, c);

		c.gridx++;
		offColor = new ColorButton(new Color(curr.offColor), 30, 10, cl);
		getContentPane().add(offColor, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton b = new JButton("Cancel");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		p.add(b);
		b = new JButton("Ok");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accept();
				dispose();
			}
		});
		p.add(b);
		getContentPane().add(p, c);


		variableBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				variableChanged((ExternalVariable)variableBox.getSelectedItem());
			}
		});

		pack();
		variableChanged(curr);
		setVisible(true);
	}

	private void accept() {
		for(int i = 0; i < ag.size(); i++)
			ag.get(i).setExternalVariables(exts);
	}

	private void variableChanged(ExternalVariable ev) {
		this.curr = ev;
		varPanel.setVariable(curr);
		onColor.setTheColor(new Color(curr.onColor));
		offColor.setTheColor(new Color(curr.offColor));
	}

	public void addNewInterval() {
		GenericDialog gd = new GenericDialog("New Interval");
		int lower = 1;
		int upper = ag.get(0).SAMPLES_PER_PERIOD;
		String[] values = new String[] {"On", "Off"};
		String value = values[0];

		gd.addNumericField("Lower bound", lower, 0);
		gd.addNumericField("Upper bound", upper, 0);
		gd.addChoice("Value", values, value);

		gd.showDialog();
		if(gd.wasCanceled())
			return;
		lower = Math.min(upper, Math.max(lower, (int)gd.getNextNumber()));
		upper = Math.min(upper, Math.max(lower, (int)gd.getNextNumber()));
		boolean v = gd.getNextChoiceIndex() == 0;

		for(int i = lower - 1; i <= upper - 1; i++)
			curr.values[i] = v;

		varPanel.repaint();
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
		public int getIconWidth() { return iconw; }
		public int getIconHeight() { return iconh; }
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color old = g.getColor();
			g.setColor(color);
			g.fillRect(x, y, iconw, iconh);
			g.setColor(old);
		}
	}

	public static class VarPanel extends JPanel {

		private ExternalVariable ext;
		private int fh;
		private int width;
		private float factor;

		public VarPanel(ExternalVariable ext, int width) {
			this.ext = ext;
			this.width = width;
			FontMetrics fm = getFontMetrics(getFont());
			this.fh = fm.getHeight();
			setPreferredSize(new Dimension(width, fh + 10 + 2));
			factor = ext.values.length / (float)width;
		}

		public void setVariable(ExternalVariable ext) {
			this.ext = ext;
			factor = ext.values.length / (float)width;
			repaint();
		}

		public void setWidth(int width) {
			this.width = width;
			factor = ext.values.length / (float)width;
			repaint();
		}

		public void paintComponent(Graphics g) {
			ext.paint(new GraphicsBackend(g), width, 1);
		}
	}
}

