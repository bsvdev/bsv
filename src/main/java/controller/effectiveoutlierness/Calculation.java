package controller.effectiveoutlierness;

import controller.ElementData;
import controller.Feature;

/**
 * The interface is used to calculate the effective outlierness by various methods.
 */
public abstract class Calculation {

	/**
	 * The minimum value calculated.
	 */
	private float minimum;

	/**
	 * The maximum value calculated.
	 */
	private float maximum;

	/**
	 * Constructs a new {@code EffectiveOutliernessCalculation} and resets min and max.
	 */
	public Calculation() {
		resetMinMax();
	}

	/**
	 * Calculates the effective outlierness and write the determined value in the element. Moreover the min and max
	 * values are checked and reset if needed.
	 *
	 * @param features
	 *            a list with all features in the currently active subspace.
	 * @param element
	 *            the {@link ElementData} to calculate the outlierness.
	 */
	public abstract void calculate(Feature[] features, ElementData element);

	/**
	 * Returns the minimum value calculated over all elements.
	 *
	 * @return the minimum value.
	 */
	public float getMinValue() {
		return this.minimum;
	}

	/**
	 * Returns the maximum value calculated over all elements.
	 *
	 * @return the maximum value.
	 */
	public float getMaxValue() {
		return this.maximum;
	}

	/**
	 * Sets the min value to the new minimum.
	 *
	 * @param min
	 *            the new minimum.
	 */
	public void setMinValue(float min) {
		this.minimum = min;
	}

	/**
	 * Sets the max value to the new maximum.
	 *
	 * @param max
	 *            the new maximum.
	 */
	public void setMaxValue(float max) {
		this.maximum = max;
	}

	/**
	 * Resets the minimum and maximum values, sets the minimum to the max possible float value and the maximum to the
	 * min possible float value.
	 */
	public void resetMinMax() {
		this.minimum = Float.MAX_VALUE;
		this.maximum = Float.MIN_VALUE;
	}

	/**
	 * Returns the Name of this calculation method.
	 *
	 * @return the name.
	 */
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}
}
