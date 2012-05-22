package gui.bsvComponents;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

/**
 * List, in which elements can be resorted by drag and drop.
 */
public class DragDropList extends JList {
	private static final long serialVersionUID = 647149035420106625L;

	/**
	 * Model used by the list.
	 */
	private final DefaultListModel model;

	/**
	 * The drag listener. It is not used locally, but it avoids the garbage collector to clean up.
	 */
	private final DragListener dl;

	/**
	 * Stores last selected item.
	 */
	private int lastSelected = -1;

	/**
	 * Constructs a new list using the given elements.
	 * 
	 * @param elements
	 *            elements shown in the list
	 */
	public DragDropList(Object[] elements) {
		super(new DefaultListModel());

		this.model = (DefaultListModel) this.getModel();

		this.setDragEnabled(true);
		this.setDropMode(DropMode.INSERT);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Change the standard selection model and do nothing in setSelectionInterval
		this.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = -4669197776774227317L;

			@Override
			public void setSelectionInterval(int a, int b) {
				// ignore
			}
		});

		this.setTransferHandler(new DropHandler(this));
		this.dl = new DragListener(this);
		this.setCellRenderer(new CellRenderer());

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				lastSelected = index;

				if (index != -1) {
					if (isSelectedIndex(index)) {
						removeSelectionInterval(index, index);
					} else {
						addSelectionInterval(index, index);
					}

					repaint();
				}
			}
		});

		for (Object o : elements) {
			this.model.addElement(o);
		}
	}

	/**
	 * Gets sorted Elements stored in the list.
	 * 
	 * @return sorted elements
	 */
	public Object[] getElements() {
		return this.model.toArray();
	}

	/**
	 * Implements a cell renderer with check boxes.
	 */
	private class CellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -1346558652020551278L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JCheckBox checkbox = new JCheckBox(value.toString());
			checkbox.setSelected(isSelected);

			return checkbox;
		}
	}

	/**
	 * Listener for dragging.
	 */
	private class DragListener extends DragSourceAdapter implements DragGestureListener {
		/**
		 * The list.
		 */
		private final DragDropList list;

		/**
		 * Our drag source.
		 */
		private final DragSource ds = new DragSource();

		/**
		 * Constructs a new listener.
		 * 
		 * @param list
		 *            list that the listener should manage
		 */
		public DragListener(DragDropList list) {
			this.list = list;
			DragGestureRecognizer dgr = ds
					.createDefaultDragGestureRecognizer(this.list, DnDConstants.ACTION_MOVE, this);
		}

		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			StringSelection transferable = new StringSelection(Integer.toString(lastSelected));
			this.ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
		}
	}

	/**
	 * Transfer handler for list index transfer (resorting).
	 */
	private class DropHandler extends TransferHandler {
		private static final long serialVersionUID = 647149035023101337L;

		/**
		 * The list.
		 */
		private final DragDropList list;

		/**
		 * Constructs a new DropHandler.
		 * 
		 * @param list
		 *            list that the handler should manage
		 */
		public DropHandler(DragDropList list) {
			this.list = list;
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}

			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();

			return dl.getIndex() != -1;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!this.canImport(support)) {
				return false;
			}

			Transferable transferable = support.getTransferable();
			String indexString;

			try {
				indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}

			int index = Integer.parseInt(indexString);
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			int dropTargetIndex = dl.getIndex();

			// resort list
			DefaultListModel model = (DefaultListModel) this.list.getModel();
			Object o = model.remove(index);

			if (dropTargetIndex > index) {
				dropTargetIndex--;
			}

			model.add(dropTargetIndex, o);

			return true;
		}
	}
}
