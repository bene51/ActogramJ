package actoj.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import actoj.core.Actogram;
import actoj.core.ActogramGroup;
import actoj.core.ExternalVariable;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;

@SuppressWarnings("serial")
public class TreeView extends JPanel implements TreeSelectionListener {

	private final JTree tree;

	private final ActoTreeModel model;

	private ArrayList<ActogramGroup> actogroups;

	private ArrayList<TreeViewListener> selectionListeners =
		new ArrayList<TreeViewListener>();

	private final ArrayList<Actogram> selected = new ArrayList<Actogram>();

	private boolean hasCalculated = false;

	public TreeView() {
		super();
		setBackground(Color.WHITE);

		actogroups = new ArrayList<ActogramGroup>();
		model = new ActoTreeModel();

		tree = new JTree(model);
		tree.addTreeSelectionListener(this);

		setLayout(new BorderLayout());
		add(tree, BorderLayout.WEST);

		createActogramGroupPopoup();
		createSingleActogramPopoup();
	}

	private ActogramGroup actogramGroupClicked = null;
	private Actogram actogramClicked = null;

	private void createActogramGroupPopoup() {
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem("Remove actograms");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramGroupClicked != null) {
					remove(actogramGroupClicked);
					actogramGroupClicked = null;
				}
			}
		});
		popup.add(item);

		popup.addSeparator();

		item = new JMenuItem("Add environmental bar");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramGroupClicked != null) {
					addExternalVariable(actogramGroupClicked);
					actogramGroupClicked = null;
				}
			}
		});
		popup.add(item);

		item = new JMenuItem("Remove environmental bar");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramGroupClicked != null) {
					removeExternalVariable(actogramGroupClicked);
					actogramGroupClicked = null;
				}
			}
		});
		popup.add(item);

		item = new JMenuItem("Edit environmental bars");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramGroupClicked != null) {
					editExternalVariables(actogramGroupClicked, 0);
					actogramGroupClicked = null;
				}
			}
		});
		popup.add(item);


		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				TreePath p = tree.getPathForLocation(e.getX(), e.getY());
				if(p == null)
					return;
				Object o = p.getLastPathComponent();
				if(o != null && o instanceof ActogramGroup && e.isPopupTrigger()) {
					actogramGroupClicked = (ActogramGroup)o;
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
	}

	private void createSingleActogramPopoup() {
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem("Edit marker");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramClicked != null) {
					changeMarkerColor(actogramClicked);
					actogramClicked = null;
				}
			}
		});
		popup.add(item);
		item = new JMenuItem("Export markers");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramClicked != null) {
					exportMarkers(actogramClicked, actogramGroupClicked);
					actogramClicked = null;
					actogramGroupClicked = null;
				}
			}
		});
		popup.add(item);
		item = new JMenuItem("Remove marker");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(actogramClicked != null) {
					removeMarker(actogramClicked);
					actogramClicked = null;
				}
			}
		});
		popup.add(item);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				TreePath p = tree.getPathForLocation(e.getX(), e.getY());
				if(p == null)
					return;
				Object o = p.getLastPathComponent();
				if(o != null && o instanceof Actogram && e.isPopupTrigger()) {
					actogramClicked = (Actogram)o;
					actogramGroupClicked = (ActogramGroup)p.getParentPath().getLastPathComponent();
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
	}

	public void editExternalVariables(ActogramGroup ag, int i) {
		ExternalVariablesDialog.run(ag, i);
		fireExternalVariablesChanged();
	}

	public void addExternalVariable(ActogramGroup ag) {
		if(ag.size() == 0)
			throw new IllegalArgumentException("Non-empty actogram group required");

		String name = IJ.getString("Name", "Unnamed");
		if(name.length() == 0)
			name = "Unnamed";
		ExternalVariable ev = new ExternalVariable(name, ag.get(0).SAMPLES_PER_PERIOD);
		for(int i = 0; i < ag.size(); i++)
			ag.get(i).addExternalVariable(ev);

		editExternalVariables(ag, ag.get(0).getExternalVariables().length - 1);
		fireExternalVariablesChanged();
	}

	public void removeExternalVariable(ActogramGroup ag) {
		if(ag.size() == 0)
			throw new IllegalArgumentException("Non-empty actogram group required");

		ExternalVariable[] evs = ag.get(0).getExternalVariables();
		String[] names = new String[evs.length];
		for(int i = 0; i < evs.length; i++)
			names[i] = evs[i].toString();
		GenericDialog gd = new GenericDialog("Remove environmental bar");
		gd.addChoice("Environmental bar", names, names[0]);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		int idx = gd.getNextChoiceIndex();
		for(int i = 0; i < ag.size(); i++)
			ag.get(i).removeExternalVariable(idx);

		fireExternalVariablesChanged();
	}

	public void removeMarker(Actogram ag) {
		int nMarkers = ag.nMarkers();
		if(nMarkers == 0) {
			IJ.showMessage("No marker");
			return;
		}
		String[] names = new String[nMarkers];
		for(int i = 0; i < nMarkers; i++)
			names[i] = ag.getMarker(i).getName();
		GenericDialog gd = new GenericDialog("Remove marker");
		gd.addChoice("Marker", names, names[0]);
		gd.showDialog();
		if(gd.wasCanceled())
			return;

		int idx = gd.getNextChoiceIndex();
		ag.removeMarker(idx);

		fireMarkersChanged();
	}

	private static class Line {

	}

	public void exportMarkers(Actogram ag, ActogramGroup parent) {
		int nMarkers = ag.nMarkers();
		if(nMarkers == 0) {
			IJ.showMessage("No marker");
			return;
		}
		String defaultName = String.valueOf(parent.name) + ag.name + "_markers.csv";
		SaveDialog sd = new SaveDialog("Export markers", defaultName, ".csv");
		try {
			ag.exportMarkers(String.valueOf(sd.getDirectory()) + sd.getFileName());
		} catch(Exception ex) {
			IJ.error(ex.getMessage());
			ex.printStackTrace();
			return;
		}
	}

	public void changeMarkerColor(Actogram ag) {
		int nMarkers = ag.nMarkers();
		if(nMarkers == 0) {
			IJ.showMessage("No marker");
			return;
		}
		MarkerDialog.run(ag, 0);
		fireMarkersChanged();
	}

	public void clearSelection() {
		tree.clearSelection();
	}

	public void addToSelection(Actogram[] actograms) {
		for(Actogram a : actograms) {
			TreePath tp = model.findLeaf(new TreePath(model.getRoot()), a);
			if(tp != null)
				tree.addSelectionPath(tp);
		}
	}

	public void setSelection(Actogram[] actograms) {
		clearSelection();
		addToSelection(actograms);
	}

	public void addCalculated(Actogram a) {
		if(!hasCalculated) {
			ActogramGroup ag = new ActogramGroup("Calculated");
			add(ag);
			hasCalculated = true;
		}
		ActogramGroup par = actogroups.get(actogroups.size() - 1);
		par.add(a);

		Object src = this;
		Object[] path = new Object[] {model.root, par};
		int[] indices = new int[] {par.size() - 1};
		Object[] children = new Object[] {a};
		model.fireInserted(src, path, indices, children);
	}

	public void add(ActogramGroup group) {
		if(!hasCalculated)
			actogroups.add(group);
		else
			actogroups.add(actogroups.size() - 1, group);
		Object src = this;
		Object[] path = new Object[] {model.root};
		int[] indices = new int[] {hasCalculated ? actogroups.size() - 2 : actogroups.size() - 1};
		Object[] children = new Object[] {group};
		model.fireInserted(src, path, indices, children);
	}

	public void remove(ActogramGroup group) {
		int idx = actogroups.indexOf(group);
		if(idx == -1)
			return;
		actogroups.remove(idx);
		Object[] path = new Object[] {model.root};
		int[] indices = new int[] {idx};
		Object[] children = new Object[] {group};
		model.fireDeleted(this, path, indices, children);
		fireSelectionChanged();
		if(hasCalculated && idx == actogroups.size())
			hasCalculated = false;
	}

	public List<Actogram> getSelected() {
		return selected;
	}

	public void addTreeSelectionListener(TreeViewListener l) {
		selectionListeners.add(l);
	}

	public void removeTreeSelectionListener(TreeViewListener l) {
		selectionListeners.remove(l);
	}

	private void fireSelectionChanged() {
		for(TreeViewListener l : selectionListeners)
			l.selectionChanged();
	}

	private void fireExternalVariablesChanged() {
		for(TreeViewListener l : selectionListeners)
			l.externalVariablesChanged();
	}

	private void fireMarkersChanged() {
		for(TreeViewListener l : selectionListeners)
			l.markersChanged();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] paths = tree.getSelectionPaths();
		selected.clear();
		if(paths == null) {
			fireSelectionChanged();
			return;
		}

		TreeMap<Integer, Actogram> rowToActogram =
			new TreeMap<Integer, Actogram>();

		for(int i = 0; i < paths.length; i++) {
			TreePath p = paths[i];
			Object l = p.getLastPathComponent();
			if(l instanceof Actogram) {
				Actogram a = (Actogram) l;
				int row = tree.getRowForPath(p);
				rowToActogram.put(row, a);
			}
		}

		for(int row : rowToActogram.keySet())
			selected.add(rowToActogram.get(row));

		fireSelectionChanged();
	}

	private class ActoTreeModel implements TreeModel {

		public final String root = "Actograms";

		private final ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

		@Override
		public Object getChild(Object parent, int index) {
			if(parent == root)
				return actogroups.get(index);
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).get(index);
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if(parent == root)
				return actogroups.size();
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).size();
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if(parent == root)
				return actogroups.indexOf(child);
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).indexOf((Actogram)child);
			return -1;
		}

		public TreePath findLeaf(TreePath path, Object a) {
			Object parent = path.getLastPathComponent();
			// check if one of the children is the object
			for(int i = 0; i < getChildCount(parent); i++)
				if(getChild(parent, i) == a)
					return path.pathByAddingChild(a);

			// check if one of the children's subtree has the
			// object
			for(int i = 0; i < getChildCount(parent); i++) {
				TreePath np = path.pathByAddingChild(getChild(parent, i));
				TreePath res = findLeaf(np, a);
				if(res != null)
					return res;
			}

			return null;
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public boolean isLeaf(Object node) {
			return (node instanceof Actogram);
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {}

		void fireInserted(Object src, Object[] path, int[] indices, Object[] children) {
			TreeModelEvent e = new TreeModelEvent(src, path, indices, children);
			for(TreeModelListener l : listeners)
				l.treeNodesInserted(e);
		}

		void fireDeleted(Object src, Object[] path, int[] indices, Object[] children) {
			TreeModelEvent e = new TreeModelEvent(src, path, indices, children);
			for(TreeModelListener l : listeners)
				l.treeNodesRemoved(e);
		}
	}
}

