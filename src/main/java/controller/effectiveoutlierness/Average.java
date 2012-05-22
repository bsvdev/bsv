package controller.effectiveoutlierness;

import controller.ElementData;
import controller.Feature;
import gui.settings.Settings;

/**
 * This class calculates the effective outlierness according to the average.
 */
public class Average extends Calculation {

	/**
	 * The name of this calculation method
	 */
	private String name = null;

	/**
	 * Constructor of a new effective outlierness calculation by average
	 */
	public Average() {
		super();
		this.name = Settings.getInstance().getResourceBundle().getString("effectiveAverage");
	}

	@Override
	public void calculate(Feature[] features, ElementData element) {
		float sum = 0;
		int count = 0;

		for (Feature current : features) {
			if (current.isOutlier()) {
				sum += element.getValue(current);
				count++;
			}
		}

		// divide by zero is not possible, b/c the import checks that there is at least one outlier feature
		float result = sum / count;

		// reset the min if needed
		if (result < this.getMinValue()) {
			this.setMinValue(result);
		}

		// reset the max if needed
		if (result > this.getMaxValue()) {
			this.setMaxValue(result);
		}

		element.addValue(features[0].getId(), result);
	}

	@Override
	public String getName() {
		return this.name;
	}
}
