package gui.views;

import java.awt.Color;
import java.util.LinkedList;

import org.apache.commons.lang3.ArrayUtils;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.Group;
import db.DatabaseAccessException;

/**
 * Different utility methods for views.
 */
public final class ViewUtils {

	/**
	 * Controls threshold of flips when sorting features.
	 */
	public static final float AUTOSORT_FLIP_THRESHOLD = 0.7f;

	/**
	 * Private constructor to avoid construction.
	 */
	private ViewUtils() {
		// utility class, do not construct
		throw new AssertionError();
	}

	/**
	 * Calculate color of an element depending on groups and feature values.
	 * 
	 * @param ed
	 *            element data.
	 * @return color of the element in rgba notation.
	 */
	public static Color calcColor(ElementData ed) {
		Color c = new Color(0.f, 0.f, 0.f, 1.f);

		Group[] groups = ed.getGroups();

		if (groups.length > 0) {
			int partsN = groups.length;

			float[] partsRed = new float[partsN];
			float[] partsGreen = new float[partsN];
			float[] partsBlue = new float[partsN];
			float[] partsAlpha = new float[partsN];

			for (int i = 0; i < partsN; i++) {
				int rgba = groups[i].getColor();

				partsBlue[i] = (rgba & 0xff) / 255.f;
				partsGreen[i] = ((rgba >> 8) & 0xff) / 255.f;
				partsRed[i] = ((rgba >> 16) & 0xff) / 255.f;
				partsAlpha[i] = ((rgba >> 24) & 0xff) / 255.f;

				Feature feature = groups[i].getColorFeature();

				if (feature != null) {
					float value = ed.getValue(feature);

					if (!Float.isNaN(value)) {
						float min = feature.getMinValue();
						float max = feature.getMaxValue();

						// scale alpha linear
						partsAlpha[i] *= (value - min) / (max - min);
					}
				}
			}

			float red = combineComponent(partsRed, partsAlpha, partsN);
			float green = combineComponent(partsGreen, partsAlpha, partsN);
			float blue = combineComponent(partsBlue, partsAlpha, partsN);
			float alpha = combineAlpha(partsAlpha, partsN);

			c = new Color(red, green, blue, alpha);
		}

		return c;
	}

	/**
	 * Combine multiple values of an color component (red, green or blue).
	 * 
	 * @param partsComponent
	 *            values of the component.
	 * @param partsAlpha
	 *            alpha values.
	 * @param partsN
	 *            number of values.
	 * @return combined value
	 */
	public static float combineComponent(float[] partsComponent, float[] partsAlpha, int partsN) {
		float sumComponent = 0.f;
		float sumAlpha = 0.f;
		int count = 0;

		for (int i = 0; i < partsN; i++) {
			if ((!Float.isNaN(partsComponent[i])) && (!Float.isNaN(partsAlpha[i]))) {
				sumComponent += partsComponent[i] * partsAlpha[i];
				sumAlpha += partsAlpha[i];
				count++;
			}
		}

		// fall back to the simple method without partsAlpha
		if ((count <= 0) || (sumAlpha <= 0.f)) {
			count = 0;

			for (int i = 0; i < partsN; i++) {
				if (!Float.isNaN(partsComponent[i])) {
					sumComponent += partsComponent[i];
					count++;
				}
			}

			sumAlpha = count;
		}

		if (count > 0) {
			return sumComponent / sumAlpha;
		} else {
			return 0.f;
		}
	}

	/**
	 * Combine multiple alpha values.
	 * 
	 * @param partsAlpha
	 *            alpha values.
	 * @param partsN
	 *            number of values.
	 * @return combined alpha
	 */
	public static float combineAlpha(float[] partsAlpha, int partsN) {
		float sumAlpha = 0.f;
		int count = 0;

		for (int i = 0; i < partsN; i++) {
			if (!Float.isNaN(partsAlpha[i])) {
				sumAlpha += partsAlpha[i];
				count++;
			}
		}

		if (count > 0) {
			return sumAlpha / count;
		} else {
			return 0.f;
		}
	}

	/**
	 * Sort all features by covariance.
	 * 
	 * @param features
	 *            features to sort.
	 * @param dataHub
	 *            DataHub for data access.
	 * @return sorted feature list
	 * 
	 * @throws DatabaseAccessException
	 *             thrown if there is an error on database access.
	 */
	public static Feature[] autoSort(Feature[] features, DataHub dataHub) throws DatabaseAccessException {
		if ((features == null) || (features.length < 3)) {
			return features;
		}

		int featureCount = features.length;
		ElementData[] data = dataHub.getData();
		int elementCount = data.length;

		if (elementCount <= 1) {
			return features;
		}

		// E[X] = 1/n * Sum(i=1,n,x_i)
		float[] e = new float[featureCount];
		for (int i = 0; i < featureCount; i++) {
			int count = 0;

			for (int j = 0; j < elementCount; j++) {
				float value = data[j].getValue(features[i]);

				if (!Float.isNaN(value)) {
					e[i] += value;
					count++;
				}
			}

			if (count > 0) {
				e[i] /= count;
			} else {
				e[i] = 0.f;
			}
		}

		// E[X*Y] = 1/n * Sum(i=1,n,x_i*y_i)
		float[][] eMatrix = new float[featureCount][featureCount];
		for (int i = 0; i < featureCount; i++) {
			for (int j = 0; j < featureCount; j++) {
				int count = 0;

				for (int k = 0; k < elementCount; k++) {
					float value1 = data[k].getValue(features[i]);
					float value2 = data[k].getValue(features[j]);

					if ((!Float.isNaN(value1)) && (!Float.isNaN(value2))) {
						eMatrix[i][j] += value1 * value2;
						count++;
					}
				}

				if (count > 0) {
					eMatrix[i][j] /= count;
				} else {
					eMatrix[i][j] = 0.f;
				}
			}
		}

		// Cov(X,Y) = E[X*Y] - E[X] * E[Y]
		float[][] c = new float[featureCount][featureCount];
		for (int i = 0; i < featureCount; i++) {
			for (int j = 0; j < featureCount; j++) {
				c[i][j] = eMatrix[i][j] - e[i] * e[j];
			}
		}

		// Var(X) = 1/(n-1) * Sum(i=1,n,(x_i - E[X])^2)
		float[] v = new float[featureCount];
		for (int i = 0; i < featureCount; i++) {
			int count = 0;

			for (int j = 0; j < elementCount; j++) {
				float value = data[j].getValue(features[i]);

				if (!Float.isNaN(value)) {
					float x = value - e[i];
					v[i] += x * x;
					count++;
				}
			}

			if (count > 1) {
				v[i] /= elementCount - 1;
			} else {
				v[i] = 0.f;
			}
		}

		// p(x,y) = C(X,Y) / sqrt(V(X)*V(Y))
		float[][] p = new float[featureCount][featureCount];
		for (int i = 0; i < featureCount; i++) {
			for (int j = 0; j < featureCount; j++) {
				p[i][j] = (float) (c[i][j] / Math.sqrt(v[i] * v[j]));
			}
		}

		// sort, so that sum of p is very high
		int[] best = new int[featureCount];
		float startMax = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < featureCount; i++) {
			for (int j = 0; j < featureCount; j++) {
				if ((i != j) && (p[i][j] > startMax)) {
					best[0] = i;
					best[1] = j;
					startMax = p[i][j];
				}
			}
		}

		for (int i = 2; i < featureCount; i++) {
			float max = Float.NEGATIVE_INFINITY;
			int last = best[i - 1];
			for (int j = 0; j < featureCount; j++) {
				float x = p[last][j];

				if (x < 0.f) {
					x *= -1.f * AUTOSORT_FLIP_THRESHOLD;
				}

				if (x > max) {
					boolean notInList = true;

					for (int k = 0; (k < i) && notInList; k++) {
						if (best[k] == j) {
							notInList = false;
						}
					}

					if (notInList) {
						max = x;
						best[i] = j;
					}
				}
			}
		}

		Feature[] sortedFeatures = new Feature[featureCount];

		for (int i = 0; i < featureCount; i++) {
			sortedFeatures[i] = features[best[i]];
		}

		return sortedFeatures;
	}

	/**
	 * Convert a number into a nice number.
	 * 
	 * @param x
	 *            the number to convert.
	 * @param round
	 *            controls if the number should rounded.
	 * @return the nice number
	 */
	public static float niceNum(float x, boolean round) {
		int exp = (int) Math.floor(Math.log10(x));
		float f = (float) (x / Math.pow(10, exp));

		float nf;

		if (round) {
			if (f < 1.5f) {
				nf = 1.f;
			} else if (f < 3.f) {
				nf = 2.f;
			} else if (f < 7.f) {
				nf = 5.f;
			} else {
				nf = 10.f;
			}
		} else {
			if (f < 1.f) {
				nf = 1.f;
			} else if (f < 2.f) {
				nf = 2.f;
			} else if (f < 5.f) {
				nf = 5.f;
			} else {
				nf = 10.f;
			}
		}

		return (float) (nf * Math.pow(10, exp));
	}

	/**
	 * Generate axis markers.
	 * 
	 * @param a
	 *            minimum axis value.
	 * @param b
	 *            maximum axis value.
	 * @param length
	 *            axis length.
	 * @param pixelsPerTick
	 *            markers per pixel.
	 * @return marker values, first value in array is nfrac
	 */
	public static float[] calcAxisMarkers(float a, float b, float length, float pixelsPerTick) {
		// parse input params
		boolean reverse = false;
		float minVar = a;
		float maxVar = b;

		if (minVar > maxVar) {
			reverse = true;
			minVar = b;
			maxVar = a;
		}

		// calc ticks, borders, steps
		int ntick = (int) (length / pixelsPerTick);
		float range = niceNum(maxVar - minVar, false);
		float d = niceNum(range / (ntick - 1), true);
		float graphMin = (float) (Math.floor(minVar / d) * d);
		float graphMax = (float) (Math.ceil(maxVar / d) * d);
		int nfrac = Math.max(-(int) Math.floor(Math.log10(d)), 0);

		// generate markers
		LinkedList<Float> markers = new LinkedList<Float>();
		markers.add((float) nfrac);

		for (float m = graphMin; m < graphMax + 0.5f * d; m += d) {
			float marker = m;

			if (marker < minVar) {
				marker = minVar;
			} else if (marker > maxVar) {
				marker = maxVar;
			}

			markers.add(marker);
		}

		// convert to result
		Float[] tmp = new Float[markers.size()];
		markers.toArray(tmp);
		float[] result = ArrayUtils.toPrimitive(tmp);

		// reverse?
		if (reverse) {
			float[] r = result;
			result = new float[r.length];
			result[0] = r[0];

			for (int i = 1; i < r.length; i++) {
				result[i] = r[r.length - i];
			}
		}

		return result;
	}
}
