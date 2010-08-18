package actoj.gui;

import ij.gui.GenericDialog;
import ij.IJ;

import actoj.core.Actogram;
import actoj.util.Filters;

import java.util.ArrayList;
import java.util.List;

import java.text.DecimalFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CalculateDialog {

	public static final float[] GAUSSIAN_KERNEL =
			Filters.makeGaussianKernel(2);
	public static final float[] UNIFORM_KERNEL =
			Filters.makeUniformKernel(GAUSSIAN_KERNEL.length);

	public static final void run(CustomWindow win) {
		new CalcDialog(win);
	}

	private static float[] makeKernel() {
		GenericDialog gd = new GenericDialog("Make kernel");
		gd.addChoice("Type", new String[] {"Gaussian"}, "Gaussian");
		gd.addNumericField("sigma", 2.0, 3);
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
		int idx = gd.getNextChoiceIndex();
		if(idx != 0)
			return null;
		float sigma = (float)gd.getNextNumber();
		return Filters.makeGaussianKernel(sigma);
	}

	private static class CalcDialog extends JDialog {

		private JComboBox methodBox, operationBox;
		private JTextField kernelField;
		private JButton kernelButton;

		private ArrayList<JComponent> smoothComponents =
				new ArrayList<JComponent>();

		private CustomWindow win;

		public CalcDialog(CustomWindow win) {
			super(win, "Calculate");
			this.win = win;

			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			getContentPane().setLayout(gridbag);

			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(5, 5, 5, 5);


			c.gridx = c.gridy = 0;
			JLabel l = new JLabel("Operation:");
			getContentPane().add(l, c);

			c.gridx++;
			operationBox = new JComboBox(new String[] {
				"Average", "Sum", "Smooth" });
			getContentPane().add(operationBox, c);


			c.gridy++;
			c.gridx = 0;
			l = new JLabel("Method:");
			smoothComponents.add(l);
			getContentPane().add(l, c);

			c.gridx++;
			methodBox = new JComboBox(new String[] {
				"Gaussian", "Uniform", "Custom" });
			smoothComponents.add(methodBox);
			getContentPane().add(methodBox, c);


			c.gridy++;
			c.gridx = 0;
			l = new JLabel("Kernel:");
			smoothComponents.add(l);
			getContentPane().add(l, c);

			c.gridx++;
			kernelField = new JTextField(20);
			smoothComponents.add(kernelField);
			getContentPane().add(kernelField, c);

			c.gridx++;
			kernelButton = new JButton("Make kernel");
			kernelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					float[] k = makeKernel();
					if(k != null)
						kernelField.setText(
							arrayToString(k));
				}
			});
			smoothComponents.add(kernelButton);
			getContentPane().add(kernelButton, c);

			JPanel p = new JPanel(new FlowLayout());
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
					try {
						calculate();
						dispose();
					} catch(Exception ex) {
						IJ.error(ex.getClass() + ": " +
							ex.getMessage());
					}
				}
			});
			p.add(b);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			getContentPane().add(p, c);



			operationBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					adjustEnabled();
				}
			});

			methodBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					adjustEnabled();
				}
			});

			pack();
			adjustEnabled();
			setVisible(true);
		}

		private void adjustEnabled() {
			int idx = operationBox.getSelectedIndex();
			for(JComponent c : smoothComponents)
				c.setEnabled(idx == 2);
			if(idx == 2) {
				int midx = methodBox.getSelectedIndex();
				float[] kernel = null;
				switch(midx) {
					case 0: kernel = GAUSSIAN_KERNEL; break;
					case 1: kernel = UNIFORM_KERNEL; break;
					case 2: kernel = new float[0]; break;
				}
				kernelField.setEnabled(true);
				kernelField.setText(arrayToString(kernel));
				kernelField.setEnabled(midx == 2);
				kernelButton.setEnabled(midx == 2);
			}
		}

		private String arrayToString(float[] arr) {
			StringBuffer buff = new StringBuffer();
			for(float v : arr)
				buff.append(v).append(" ");
			return buff.toString().trim();
		}

		private void calculate() {
			int opIndex = operationBox.getSelectedIndex();
			int meIndex = methodBox.getSelectedIndex();

			ArrayList<ActogramCanvas> acs;
			List<Actogram> actograms;
			Actogram result;
			switch(opIndex) {
				case 0: // average
					acs = win.canvas.getActograms();
					actograms = new ArrayList<Actogram>();
					for(ActogramCanvas ac : acs)
						actograms.add(
							ac.processor.original);
					result = Actogram.average(actograms);
					win.tree.addCalculated(result);
					break;
				case 1: // sum
					acs = win.canvas.getActograms();
					actograms = new ArrayList<Actogram>();
					for(ActogramCanvas ac : acs)
						actograms.add(
							ac.processor.original);
					result = Actogram.sum(actograms);
					win.tree.addCalculated(result);
					break;
				case 2: // smooth
					acs = win.canvas.getActograms();
					actograms = new ArrayList<Actogram>();
					float[] kernel = null;
					switch(meIndex) {
						case 0:
						kernel = GAUSSIAN_KERNEL;
						break;
						case 1:
						kernel = UNIFORM_KERNEL;
						break;
						case 2:
						kernel = getKernel(
							kernelField.getText());
						break;
					}
					for(ActogramCanvas ac : acs) {
						Actogram ag =
							ac.processor.original;
						result = ag.convolve(kernel);
						win.tree.addCalculated(result);
					}
					break;
			}
		}

		private float[] getKernel(String text) {
			String[] s = text.split(" ");
			float[] k = new float[s.length];
			for(int i = 0; i < s.length; i++)
				k[i] = Float.parseFloat(s[i]);
			return k;
		}
	}
}

