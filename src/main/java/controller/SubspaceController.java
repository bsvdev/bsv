package controller;

import controller.effectiveoutlierness.Max;
import controller.effectiveoutlierness.Average;
import controller.effectiveoutlierness.Min;
import controller.effectiveoutlierness.Calculation;
import gui.settings.Settings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;

import util.Failure;
import db.Database;
import db.DatabaseAccessException;

/**
 * The class {@code SubspaceController} holds the currently active {@link Subspace} and manages the list of all existing
 * {@link Subspace}s.
 */
public class SubspaceController extends Observable {

	/**
	 * The currently active {@link Subspace} in the UI}.
	 */
	private Subspace currentActiveSubspace;

	/**
	 * The method to calculate the effective outlierness by.
	 */
	private Calculation calculateEffectiveOutliernessBy;

	/**
	 * The {@link Database}, where the {@code Subspace}s are stored.
	 */
	private final Database database;

	/**
	 * Constructs a new {@code SubspaceController}.
	 *
	 * @param database
	 *            the {@link Database}, where the {@link Subspace}s are stored, it may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if the read operation failed in {@link Database}.
	 */
	public SubspaceController(Database database) throws DatabaseAccessException {
		if (database == null) {
			throw new IllegalArgumentException("database is null");
		}

		this.database = database;
		this.currentActiveSubspace = getAllFeatureSubspace();
		this.calculateEffectiveOutliernessBy = this.getAllCalculations()[0];
	}

	/**
	 * Returns a list of all existing {@link Subspace}s in the {@link Database}. The first item is a generated
	 * {@link Subspace} with all existing {@link Feature}s.
	 *
	 * @return The list of {@link Subspace}s.
	 * @throws DatabaseAccessException
	 *             if read operation failed in {@link Database}.
	 */
	public Subspace[] getSubspaces() throws DatabaseAccessException {
		Subspace[] subspaces = new Subspace[0];

		try {
			Statement stmt = this.database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Count(DISTINCT Id) FROM Subspaces;");
			int count = rs.getInt(1);

			subspaces = new Subspace[count + 1];

			// Add first Subspace, with all existing features
			subspaces[0] = getAllFeatureSubspace();

			for (int i = 1; i <= count; i++) {
				ArrayList<Integer> currentFeatures = new ArrayList<Integer>();
				int subspaceId = i;

				rs = stmt.executeQuery("SELECT Name, FeatureReference FROM Subspaces WHERE Id=" + i + ";");

				// set the fields id and name and add the first feature reference to the list
				rs.next();
				String name = rs.getString(1);
				currentFeatures.add(rs.getInt(2));

				// add all remaining features to the list
				while (rs.next()) {
					currentFeatures.add(rs.getInt(2));
				}

				Integer[] featureIds = new Integer[currentFeatures.size()];
				currentFeatures.toArray(featureIds);

				subspaces[i] = new Subspace(this, this.database, subspaceId,
						buildSubspaceName(name, featureIds), featureIds);
			}

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}

		return subspaces;
	}

	/**
	 * Returns the currently active {@link Subspace} in the UI.
	 *
	 * @return the active subspace.
	 */
	public Subspace getActiveSubspace() {
		return this.currentActiveSubspace;
	}

	/**
	 * Changes the currently active {@link Subspace} in the UI.
	 *
	 * @param subspace
	 *            The new {@link Subspace}.
	 * @return The newly activated {@link Subspace}.
	 */
	public Subspace setActiveSubspace(Subspace subspace) {
		if (subspace == null) {
			throw new IllegalArgumentException("subspace is null");
		}

		this.currentActiveSubspace = subspace;
		this.calculateEffectiveOutliernessBy.resetMinMax();

		informObservers();

		return this.currentActiveSubspace;
	}

	/**
	 * Returns a list with all possible calculations for the effective outlierness.
	 *
	 * @return the list.
	 */
	public Calculation[] getAllCalculations() {
		Calculation[] calcs = { new Average(), new Max(),
				new Min() };

		return calcs;
	}

	/**
	 * Returns the currently active strategy to calculate the effective outlierness by.
	 *
	 * @return the strategy.
	 */
	public Calculation getCalculateEffectiveOutliernessBy() {
		return this.calculateEffectiveOutliernessBy;
	}

	/**
	 * Sets the strategy to calculate the effective outlierness by.
	 *
	 * @param strategy
	 *            the new strategy.
	 */
	public void setCalculateEffectiveOutliernessBy(Calculation strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("the new strategy may not be null");
		}

		strategy.resetMinMax();
		this.calculateEffectiveOutliernessBy = strategy;

		informObservers();
	}

	/**
	 * This method builds a String which represents the subspace.
	 *
	 * The query is build by the features within the subspace and looks like: "(feature1,feature2,...)".
	 *
	 * @param features
	 *            a list of features.
	 * @return the built string.
	 * @throws DatabaseAccessException
	 *             if the read operation failed in {@link Database}.
	 */
	private String buildSubspaceName(String subSpaceName, Integer[] features) throws DatabaseAccessException {
		// features has to be: "1", "3", "6", ..

		StringBuilder strB = new StringBuilder();

		try {
			Statement stmt = this.database.getConnection().createStatement();

			// Get all required names for the features
			ResultSet rs = stmt.executeQuery("SELECT Name FROM Features WHERE Id IN " + requiredFeatures(features)
					+ ";");

			// iterate all names and build the name for the subspace
			strB.append(subSpaceName);
			strB.append(" - (");

			while (rs.next()) {
				strB.append(rs.getString(1));
				strB.append(',');
			}

			strB.deleteCharAt(strB.lastIndexOf(","));
			strB.append(")");

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}

		return strB.toString();
	}

	/**
	 * This method builds a String with all required features, to insert into the sql query.
	 *
	 * @param features
	 *            a list of features.
	 * @return the built string.
	 */
	private String requiredFeatures(Integer[] features) {
		// SELECT Name FROM Features WHERE Id IN;
		// requiredFeatures has to be: "1", "3", "6", ..

		StringBuilder strB = new StringBuilder();
		strB.append("(");

		for (int currentFeature : features) {
			strB.append(currentFeature);
			strB.append(',');
		}

		strB.deleteCharAt(strB.lastIndexOf(","));
		strB.append(")");

		return strB.toString();
	}

	/**
	 * The method returns a new {@link Subspace} with all existing features.
	 *
	 * @return the newly created {@link Subspace}.
	 * @throws DatabaseAccessException
	 *             if the read operation failed in {@link Database}.
	 */
	private Subspace getAllFeatureSubspace() throws DatabaseAccessException {
		Subspace subspace = new Subspace(this, this.database, 0, "No-Features", new Integer[0]);

		try {
			Statement stmt = this.database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Id FROM Features;");

			ArrayList<Integer> allExistingFeatures = new ArrayList<Integer>();

			while (rs.next()) {
				allExistingFeatures.add(rs.getInt(1));
			}

			Integer[] featureIds = new Integer[allExistingFeatures.size()];
			allExistingFeatures.toArray(featureIds);

			subspace = new Subspace(this, this.database, 0, Settings.getInstance().getResourceBundle()
					.getString("allFeatures"), featureIds);

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}

		return subspace;
	}

	/**
	 * Returns the minimum value in effective outlierness.
	 *
	 * @return the min value.
	 */
	public float getEOMinValue() {
		return this.calculateEffectiveOutliernessBy.getMinValue();
	}

	/**
	 * Returns the maximum value in effective outlierness.
	 *
	 * @return the max value.
	 */
	public float getEOMaxValue() {
		return this.calculateEffectiveOutliernessBy.getMaxValue();
	}

	/**
	 * This method updates all registered Observer.
	 */
	public void informObservers() {
		// update observers
		setChanged();
		notifyObservers();
	}
}
