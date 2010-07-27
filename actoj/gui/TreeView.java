package actoj.gui;

import actoj.core.Actogram;
import actoj.core.ActogramGroup;

import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.*;

public class TreeView extends JPanel implements TreeSelectionListener {

	private final JTree tree;

	private final ActoTreeModel model;

	private ArrayList<ActogramGroup> actogroups;

	private ArrayList<ATreeSelectionListener> selectionListeners =
		new ArrayList<ATreeSelectionListener>();
	
	private final ArrayList<Actogram> selected = new ArrayList<Actogram>();

	public TreeView() {
		super();
		setBackground(Color.WHITE);

		actogroups = new ArrayList<ActogramGroup>();
		model = new ActoTreeModel();

		tree = new JTree(model);
		tree.addTreeSelectionListener(this);

		setLayout(new BorderLayout());
		add(tree, BorderLayout.WEST);

		createPopup();
	}

	private ActogramGroup popupClicked = null;

	private void createPopup() {
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem("Remove");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(popupClicked != null) {
					remove(popupClicked);
					popupClicked = null;
				}
			}
		});
		popup.add(item);


		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				TreePath p = tree.getPathForLocation(e.getX(), e.getY());
				if(p == null)
					return;
				Object o = p.getLastPathComponent();
				if(o != null && o instanceof ActogramGroup && e.isPopupTrigger()) {
					popupClicked = (ActogramGroup)o;
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
	}

	public void add(ActogramGroup group) {
		actogroups.add(group);
		Object src = this;
		Object[] path = new Object[] {model.root};
		int[] indices = new int[] {actogroups.size() - 1};
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
	}

	public List<Actogram> getSelected() {
		return selected;
	}

	public void addTreeSelectionListener(ATreeSelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeTreeSelectionListener(ATreeSelectionListener l) {
		selectionListeners.remove(l);
	}

	private void fireSelectionChanged() {
		for(ATreeSelectionListener l : selectionListeners)
			l.selectionChanged();
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] paths = tree.getSelectionPaths();
		selected.clear();
		if(paths != null) {
			for(int i = 0; i < paths.length; i++) {
				Object l = paths[i].getLastPathComponent();
				if(l instanceof Actogram)
					selected.add((Actogram)l);
			}
		}
		fireSelectionChanged();
	}

	private class ActoTreeModel implements TreeModel {

		public final String root = "Actograms";

		private final ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

		public Object getChild(Object parent, int index) {
			if(parent == root)
				return actogroups.get(index);
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).get(index);
			return null;
		}

		public int getChildCount(Object parent) {
			if(parent == root)
				return actogroups.size();
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).size();
			return 0;
		}

		public int getIndexOfChild(Object parent, Object child) {
			if(parent == root)
				return actogroups.indexOf((ActogramGroup)child);
			if(parent instanceof ActogramGroup)
				return ((ActogramGroup)parent).indexOf((Actogram)child);
			return -1;
		}

		public Object getRoot() {
			return root;
		}

		public boolean isLeaf(Object node) {
			return (node instanceof Actogram);
		}

		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

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

