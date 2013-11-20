package actoj.gui;

import ij.IJ;
import ij.gui.GenericDialog;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import actoj.core.Actogram;
import actoj.util.Filters;

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
		gd.addChoice("Type", new String[] {"Gaussian", "Uniform"}, "Gaussian");
		gd.addNumericField("sigma/length", 2.0, 3);
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
		int idx = gd.getNextChoiceIndex();
		float sigma = (float)gd.getNextNumber();
		switch(idx) {
			case 0: return Filters.makeGaussianKernel(sigma);
			case 1: return Filters.makeUniformKernel((int)sigma);
		}
		return null;
	}

	@SuppressWarnings("serial")
	private static class CalcDialog extends JDialog {

		private JComboBox operationBox;
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
				@Override
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
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			p.add(b);
			b = new JButton("Ok");
			b.addActionListener(new ActionListener() {
				@Override
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
				@Override
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
		}

		private String arrayToString(float[] arr) {
			StringBuffer buff = new StringBuffer();
			for(float v : arr)
				buff.append(v).append(" ");
			return buff.toString().trim();
		}

		private void calculate() {
			int opIndex = operationBox.getSelectedIndex();

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
					float[] kernel = getKernel(kernelField.getText());
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

