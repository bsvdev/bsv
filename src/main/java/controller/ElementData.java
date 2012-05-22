package controller;

import java.util.HashMap;

/**
 * The class {@code ElementData} represents one element with several {@link Feature}s and their values. The number of
 * {@link Feature}s differs, according to the used {@link Feature}s in the UI.
 */
public class ElementData {

	/**
	 * A unique identifier for this element.
	 */
	private final int id;

	/**
	 * A list of group ids, containing this element.
	 */
	private final Group[] groups;

	/**
	 * Stores the values, matching to the features.
	 */
	private final HashMap<Integer, Float> values;

	/**
	 * Constructs a new {@code ElementData}.
	 * 
	 * @param id
	 *            not negative unique identifier of this element.
	 * @param featureIds
	 *            a list of feature ids, this element contains.
	 * @param values
	 *            the values matching to the feature ids.
	 * @param groups
	 *            a list of groups, which this element belongs to.
	 */
	public ElementData(int id, int[] featureIds, float[] values, Group[] groups) {
		if (id < 1 || featureIds == null || values == null || groups == null) {
			throw new IllegalArgumentException("one parameter is null or id is negative");
		} else if (featureIds.length != values.length) {
			throw new IllegalArgumentException("featureIds and values have not same length");
		}

		this.id = id;
		this.values = new HashMap<Integer, Float>((int) ((featureIds.length + 1) * 1.2f) + 1, 1.f);

		// Add all values matching to the features
		for (int i = 0; i < featureIds.length; i++) {
			this.values.put(featureIds[i], values[i]);
		}

		this.groups = groups;
	}

	/**
	 * Returns the unique identifier of this element.
	 * 
	 * @return the unique identifier.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the value, matching to the given {@code feature}.
	 * 
	 * @param feature
	 *            the {@link Feature} to request the value, it may not be {@code null}.
	 * @return the value
	 */
	public float getValue(Feature feature) {
		if (feature == null) {
			throw new IllegalArgumentException("feature is null");
		}

		return this.values.get(feature.getId());
	}

	/**
	 * Returns a list of {@link Group}s, containing this element.
	 * 
	 * @return the list of {@link Group}s.
	 */
	public Group[] getGroups() {
		return this.groups;
	}

	/**
	 * Adds a new value to this element.
	 * 
	 * @param id
	 *            the identifier to match the value.
	 * @param value
	 *            the value to add to this element.
	 */
	public void addValue(int id, float value) {
		this.values.put(id, value);
	}
}
