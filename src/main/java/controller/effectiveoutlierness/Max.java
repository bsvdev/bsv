package controller.effectiveoutlierness;

import controller.ElementData;
import controller.Feature;
import gui.settings.Settings;

/**
 * The class is used to calculate the effective outlierness for a specific subspace. The value is set to the maximum
 * value.
 */
public class Max extends Calculation {

	/**
	 * The name of this calculation method
	 */
	private String name = null;

	/**
	 * Constructor of a new effective outlierness calculation by maximum
	 */
	public Max() {
		super();
		this.name = Settings.getInstance().getResourceBundle().getString("effectiveMax");
	}

	@Override
	public void calculate(Feature[] features, ElementData element) {
		float max = Integer.MIN_VALUE;

		for (Feature current : features) {
			if (current.isOutlier()) {
				max = Math.max(max, element.getValue(current));
			}
		}

		// reset the min if needed
		if (max < this.getMinValue()) {
			this.setMinValue(max);
		}

		// reset the max if needed
		if (max > this.getMaxValue()) {
			this.setMaxValue(max);
		}

		element.addValue(features[0].getId(), max);
	}

	@Override
	public String getName() {
		return this.name;
	}
}
