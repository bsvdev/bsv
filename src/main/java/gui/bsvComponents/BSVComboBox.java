package gui.bsvComponents;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * Special JComboBox with some extra features.
 */
public class BSVComboBox extends JComboBox {
	private static final long serialVersionUID = 4626462963342365660L;

	/**
	 * Create a new BSVComboBox.
	 */
	public BSVComboBox() {
		super();
		this.init();
	}

	/**
	 * Create a new BSVComboBox.
	 * 
	 * @param aModel
	 *            ComboBoxModel that should be used
	 */
	public BSVComboBox(ComboBoxModel aModel) {
		super(aModel);
		this.init();
	}

	/**
	 * Create a new BSVComboBox.
	 * 
	 * @param items
	 *            items to show in combo box
	 */
	public BSVComboBox(Object[] items) {
		super(items);
		this.init();
	}

	/**
	 * Create a new BSVComboBox.
	 * 
	 * @param items
	 *            items to show in combo box
	 */
	public BSVComboBox(Vector<?> items) {
		super(items);
		this.init();
	}

	/**
	 * Set up some basic stuff for special features.
	 */
	private void init() {
		this.addMouseWheelListener(new BSVComboBoxMouseWheelListener());
	}

	/**
	 * A mouse wheel listener that changes the selection index.
	 */
	private class BSVComboBoxMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int index = getSelectedIndex() + e.getWheelRotation();

			if (index < 0) {
				index = 0;
			} else if (index >= getItemCount()) {
				index = getItemCount() - 1;
			}

			setSelectedIndex(index);
		}
	}
}
