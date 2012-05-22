package gui.bsvComponents;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;

/**
 * Special JSlider with more features.
 */
public class BSVSlider extends JSlider {
	private static final long serialVersionUID = -2488344410717310820L;

	/**
	 * Create a new BSVSlider.
	 */
	public BSVSlider() {
		super();
		this.init();
	}

	/**
	 * Create a new BSVSlider.
	 * 
	 * @param brm
	 *            slider model
	 */
	public BSVSlider(BoundedRangeModel brm) {
		super(brm);
		this.init();
	}

	/**
	 * Create a new BSVSlider.
	 * 
	 * @param orientation
	 *            the orientation of the slider
	 */
	public BSVSlider(int orientation) {
		super(orientation);
		this.init();
	}

	/**
	 * Create a new BSVSlider.
	 * 
	 * @param min
	 *            minimum value of the slider
	 * @param max
	 *            maximum value of the slider
	 */
	public BSVSlider(int min, int max) {
		super(min, max);
		this.init();
	}

	/**
	 * Create a new BSVSlider.
	 * 
	 * @param min
	 *            minimum value of the slider
	 * @param max
	 *            maximum value of the slider
	 * @param value
	 *            initial slider value
	 */
	public BSVSlider(int min, int max, int value) {
		super(min, max, value);
		this.init();
	}

	/**
	 * Create a new BSVSlider.
	 * 
	 * @param orientation
	 *            the orientation of the slider
	 * @param min
	 *            minimum value of the slider
	 * @param max
	 *            maximum value of the slider
	 * @param value
	 *            initial slider value
	 */
	public BSVSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		this.init();
	}

	/**
	 * Set up some features.
	 */
	private void init() {
		this.addMouseWheelListener(new BSVSliderMouseWheelListener());
	}

	/**
	 * Mouse wheel listener that changes the slider value.
	 */
	private class BSVSliderMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int value = getValue() - e.getWheelRotation();

			if (value < getMinimum()) {
				value = getMinimum();
			} else if (value > getMaximum()) {
				value = getMaximum();
			}

			setValue(value);
		}
	}
}
