package controller;

import controller.effectiveoutlierness.Calculation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import db.Database;

/**
 * The class {@code DataArrayWorker} is used to concurrently filter and create {@code ElementData} objects and finally
 * fill the range of the array with it.
 */
public class DataArrayWorker implements Runnable {

	/**
	 * The {@link Database}, where all elements are stored.
	 */
	private Database database = null;

	/**
	 * The prepared part of the needed query, that is generated for the worker.
	 */
	private String sharedSql = null;

	/**
	 * The array we have to fill with our workload.
	 */
	private ElementData[] elements = null;

	/**
	 * The method to calculate the effective outlierness by
	 */
	private Calculation calculateEffectiveOutlierness = null;

	/**
	 * The groups, which currently exist.
	 */
	private Group[] groups = null;

	/**
	 * The active features, used to filter only wanted values.
	 */
	private Feature[] features = null;

	/**
	 * The start index of our array working range.
	 */
	private int start = 0;

	/**
	 * The end index of our array working range.
	 */
	private int end = 0;

	/**
	 * The array of ids, filter by constraints.
	 */
	private int[] uniqWorkerArray = null;

	/**
	 * The group ids with their set of ids, used to resolve elements to their groups.
	 */
	private HashMap<Integer, HashSet<Integer>> uniqGroupIds = null;

	/**
	 * Constructs a new {@code DataArrayWorker}.
	 *
	 * @param database
	 *            The {@link Database}, where all elements are stored.
	 * @param sql
	 *            The prepared part of the needed query, that is generated for the worker.
	 * @param elements
	 *            The array we have to fill with our workload.
	 * @param calculateEffectiveOutlierness
	 *            The method to calculate the effective outlierness.
	 * @param groups
	 *            The groups, which currently exist.
	 * @param features
	 *            The active features, used to filter only wanted values.
	 * @param start
	 *            The start index of our array working range.
	 * @param end
	 *            The end index of our array working range.
	 * @param uniqWorkerArray
	 *            The array of ids, filter by constraints.
	 * @param uniqGroupIds
	 *            The group ids with their set of ids, used to resolve elements to their groups.
	 */
	DataArrayWorker(Database database, String sql, ElementData[] elements,
			Calculation calculateEffectiveOutlierness, Group[] groups, Feature[] features,
			int start, int end, int[] uniqWorkerArray, HashMap<Integer, HashSet<Integer>> uniqGroupIds) {
		this.database = database;
		this.sharedSql = sql;
		this.elements = elements;
		this.calculateEffectiveOutlierness = calculateEffectiveOutlierness;
		this.groups = groups;
		this.features = features;
		this.start = start;
		this.end = end;
		this.uniqWorkerArray = uniqWorkerArray;
		this.uniqGroupIds = uniqGroupIds;
	}

	@Override
	public void run() {
		int[] featureIds = new int[this.features.length];
		float[] values = new float[this.features.length];

		// resolve feature ids
		for (int p = 1; p < this.features.length; ++p) {
			featureIds[p] = this.features[p].getId();
		}

		try {
			// we need to get all objects for our block [end-start], b/c we have no constraints
			if (uniqWorkerArray == null) {
				this.generateAllElements(featureIds, values);
			} else {
				this.generateElementsFromRange(featureIds, values);
			}
		} catch (SQLException e) {
			// we couldn't finish our block, so we re-fill it with NaN to gracefully handle this situation
			this.invalidateRange(featureIds, values);
		}

		// done here
		Thread.yield();
	}

	/**
	 * generates elements if we do not have any constraints.
	 *
	 * fast query, slow object creation
	 *
	 * @param featureIds
	 *            the list of features
	 * @param values
	 *            the list to store the values in
	 * @throws SQLException
	 *             if database access failed
	 */
	private void generateAllElements(int[] featureIds, float[] values) throws SQLException {
		int fetchSize = (end - start) < 1 ? 0 : (end - start);

		Statement stmt = this.database.getConnection().createStatement();
		stmt.setFetchSize(fetchSize);

		// query = shared + specific
		ResultSet rs = stmt.executeQuery(sharedSql + "WHERE Id >= " + (start + 1) + " AND Id < " + (end + 1) + ";");

		// fill elements from start to end
		for (int i = this.start; i < this.end && rs.next(); ++i) {
			// walk over each table column in order to get all values
			for (int j = 0; j < this.features.length; ++j) {
				// offset +1, b/c counting starts at 1
				values[j] = rs.getFloat(j + 1);

				// gracefully handle NaN, so we are always returning floats
				if (rs.wasNull()) {
					values[j] = Float.NaN;
				}
			}

			ElementData element = this.generateElementData(i + 1, featureIds, values, true);
			this.calculateEffectiveOutlierness.calculate(features, element);
			this.elements[i] = element;
		}

		stmt.close();
	}

	/**
	 * Generates elements based on constraints.
	 *
	 * slow query, fast object creation (assuming filtered ids << all ids)
	 *
	 * @param featureIds
	 *            the list of features
	 * @param values
	 *            the array to store the values in
	 * @throws SQLException
	 *             if database access failed
	 */
	private void generateElementsFromRange(int[] featureIds, float[] values) throws SQLException {
		int fetchSize = (end - start) < 1 ? 0 : (end - start);

		Statement stmt = this.database.getConnection().createStatement();
		stmt.setFetchSize(fetchSize);

		// only get ids from the set
		for (int k = this.start; k < this.end; ++k) {
			int id = this.uniqWorkerArray[k];

			ResultSet rs = stmt.executeQuery(sharedSql + "WHERE Id== " + id + ";");

			// walk over each table column in order to get all values
			for (int l = 0; l < this.features.length; ++l) {
				// offset +1, b/c counting starts at 1
				values[l] = rs.getFloat(l + 1);

				// gracefully handle NaN, so we are always returning floats
				if (rs.wasNull()) {
					values[l] = Float.NaN;
				}
			}

			ElementData element = this.generateElementData(id, featureIds, values, false);
			this.calculateEffectiveOutlierness.calculate(features, element);
			this.elements[k] = element;
		}
		stmt.close();
	}

	/**
	 * Invalidates the specific range of the worker array, by setting NaN as values.
	 *
	 * By doing so, we are able to handle interrupted worker threads gracefully.
	 *
	 * @param featureIds
	 *            the list of features
	 * @param values
	 *            the array to store the values in
	 */
	private void invalidateRange(int[] featureIds, float[] values) {
		// fill elements from start to end
		for (int i = this.start; i < this.end; ++i) {
			// fill each value of the element with NaN
			for (int j = 0; j < this.features.length; ++j) {
				values[j] = Float.NaN;
			}

			ElementData element = this.generateElementData(i + 1, featureIds, values, true);

			// calculate the effective outlierness for this element
			this.calculateEffectiveOutlierness.calculate(features, element);

			// add the element to the final list
			this.elements[i] = element;
		}
	}

	/**
	 * Generate ElementData and assign groups
	 *
	 * @param id element id
	 * @param featureIds feature ids of required features
	 * @param values feature values
	 * @param emptyGroup flags, if empty group should add to elements
	 * @return element data
	 */
	private ElementData generateElementData(int id, int[] featureIds, float[] values, boolean emptyGroup) {
		int[] groupIds;

		// get group ids from groups, which select all ids
		ArrayList<Integer> inGroup = new ArrayList<Integer>();

		if (emptyGroup) {
			// add "empty" group to elements
			for (Group group : this.groups) {
				if (group.isVisible() && group.getConstraints().length == 0) {
					inGroup.add(group.getId());
				}
			}
		}

		// check for occurrence of id and get the groupId from the mapping
		for (Map.Entry<Integer, HashSet<Integer>> entry : this.uniqGroupIds.entrySet()) {
			if (entry.getValue().contains(id) && !inGroup.contains(entry.getKey())) {
				inGroup.add(entry.getKey());
			}
		}

		if (inGroup.isEmpty()) {
			groupIds = new int[0];
		} else {
			// we have to convert the ArrayList to an array for the ElementData ctor
			groupIds = this.convertGroupMapping(inGroup);
			inGroup.clear();
		}

		// finally, fire up the constructor!
		return new ElementData(id, featureIds, values, resolvGroups(groupIds));
	}

	/**
	 * Converts an ArrayList of group ids to a native int array for ease of usage.
	 *
	 * @param inGroup
	 *            the ArrayList to convert
	 * @return the result in array
	 */
	private int[] convertGroupMapping(ArrayList<Integer> inGroup) {
		Integer[] integerArray = new Integer[inGroup.size()];
		inGroup.toArray(integerArray);

		return ArrayUtils.toPrimitive(integerArray);
	}

	/**
	 * Resolves group ids to group objects.
	 *
	 * @param groupIds
	 *            the list of group ids
	 * @return the list of groups
	 */
	private Group[] resolvGroups(int[] groupIds) {
		ArrayList<Group> inGroup = new ArrayList<Group>();

		// get specific group objects by their ids
		for (Group group : this.groups) {
			if (group.isVisible()) {
				for (int groupId : groupIds) {
					if (group.getId() == groupId) {
						inGroup.add(group);
					}
				}
			}
		}

		Group[] groups = new Group[inGroup.size()];

		for (int i = 0; i < inGroup.size(); ++i) {
			groups[i] = inGroup.get(i);
		}

		return groups;
	}
}
