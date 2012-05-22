package controller.effectiveoutlierness;

import controller.ElementData;
import controller.Feature;
import gui.settings.Settings;

/**
 * The class is used to calculate the effective outlierness for a specific subspace. The value is set to the minimum
 * value.
 */
public class Min extends Calculation {

	/**
	 * The name of this calculation method
	 */
	private String name = null;

	/**
	 * Constructor of a new effective outlierness calculation by minimum
	 */
	public Min() {
		super();
		this.name = Settings.getInstance().getResourceBundle().getString("effectiveMin");
	}

	@Override
	public void calculate(Feature[] features, ElementData element) {
		float min = Integer.MAX_VALUE;

		for (Feature current : features) {
			if (current.isOutlier()) {
				min = Math.min(min, element.getValue(current));
			}
		}

		// reset the min if needed
		if (min < this.getMinValue()) {
			this.setMinValue(min);
		}

		// resest the max if needed
		if (min > this.getMaxValue()) {
			this.setMaxValue(min);
		}

		element.addValue(features[0].getId(), min);
	}

	@Override
	public String getName() {
		return this.name;
	}
}
