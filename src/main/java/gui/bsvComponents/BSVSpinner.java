package gui.bsvComponents;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 * Special JSpinner with more features.
 */
public class BSVSpinner extends JSpinner {
	private static final long serialVersionUID = -7533339446697563441L;

	/**
	 * Creates a new BSVSpinner.
	 */
	public BSVSpinner() {
		super();
		this.init();
	}

	/**
	 * Creates a new BSVSpinner.
	 * 
	 * @param model
	 *            the spinner model
	 */
	public BSVSpinner(SpinnerModel model) {
		super(model);
		this.init();
	}

	/**
	 * Set up some features.
	 */
	private void init() {
		this.addMouseWheelListener(new BSVSpinnerMouseWheelListener());
	}

	/**
	 * Mouse wheel listener that changes the value.
	 */
	private class BSVSpinnerMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			SpinnerModel model = getModel();
			final int steps = e.getWheelRotation();

			try {
				if (steps < 0) {
					for (int i = 0; i > steps; i--) {
						model.setValue(model.getNextValue());
					}
				} else {
					for (int i = 0; i < steps; i++) {
						model.setValue(model.getPreviousValue());
					}
				}
			} catch (IllegalArgumentException ex) {
				// ignore
			}
		}
	}
}
