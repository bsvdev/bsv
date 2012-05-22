package controller;

import gui.settings.Settings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.Failure;
import db.Database;
import db.DatabaseAccessException;

/**
 * The class {@code Subspace} represents a defined set of {@link Feature}s.
 */
public class Subspace {

	/**
	 * A unique identifier for this {@code Subspace}, set by the {@link Database}.
	 */
	private final int id;

	/**
	 * The name of this {@code Subspace}.
	 */
	private final String name;

	/**
	 * The list of feature ids belonging to this {@code Subspace}.
	 */
	private final Integer[] featureIds;

	/**
	 * The {@link Database}, where the features are stored.
	 */
	private final Database database;

	/**
	 * The {@link SubspaceController} this {@code Subspace} is managed.
	 */
	private final SubspaceController subspaceController;

	/**
	 * Constructs a new {@code Subspace}.
	 *
	 * @param subspaceController
	 *            the {@link SubspaceController} this {@code Subspace} is managed.
	 * @param database
	 *            the {@link Database}, where the {@link Feature}s are stored.
	 * @param id
	 *            a not negative unique identifier.
	 * @param name
	 *            the name of this {@code Subspace}.
	 * @param featureIds
	 *            the feature ids belonging to this {@code Subspace}, it may not be {@code null}.
	 */
	public Subspace(SubspaceController subspaceController, Database database, int id, String name,
			Integer[] featureIds) {

		if (subspaceController == null || database == null || id < 0 || featureIds == null || name == null) {
			throw new IllegalArgumentException("id is negative, features or name is null");
		}

		this.subspaceController = subspaceController;
		this.database = database;
		this.id = id;
		this.name = name;
		this.featureIds = featureIds;
	}

	/**
	 * Returns the unique identifier of this {@code Subspace}.
	 *
	 * @return the identifier.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the name of this {@code Subspace}.
	 *
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns a list of all {@link Feature}s, belonging to this {@code Subspace} and adds a virtual feature for the
	 * effective outlierness at first position.
	 *
	 * @return the features.
	 * @throws DatabaseAccessException
	 *             if read operation failed in {@link Database}.
	 */
	public Feature[] getFeatures() throws DatabaseAccessException {
		ResultSet rs = null;
		Feature[] features = new Feature[0];

		if (featureIds.length > 0) {
			try {
				Statement stmt = database.getConnection().createStatement();

				rs = stmt.executeQuery("SELECT Name, OutlierFlag, Id, Min, Max FROM Features WHERE Id IN "
						+ requiredFeatures(this.featureIds) + ";");

				features = new Feature[featureIds.length + 1];

				// add a feature for the effective outlierness
				features[0] = new Feature(this.subspaceController, this.database, -1, Settings.getInstance()
						.getResourceBundle().getString("effectOutlierness"), false, true, 0, 0);

				for (int i = 1; i <= features.length && rs.next(); ++i) {
					features[i] = new Feature(this.subspaceController, this.database, rs.getInt(3), rs.getString(1),
							rs.getBoolean(2), false, rs.getFloat("Min"), rs.getFloat("Max"));
				}

				stmt.close();
			} catch (SQLException e) {
				throw new DatabaseAccessException(Failure.READ);
			}
		}

		return features;
	}

	/**
	 * This method builds a String with all required features, to insert into the sql query.
	 *
	 * @param features
	 *            a list of features.
	 * @return the build string.
	 */
	private String requiredFeatures(Integer[] features) {
		// SELECT * FROM Objects WHERE Id IN;
		// requiredFeatures has to be: "1", "3", "6", ..

		StringBuilder strB = new StringBuilder();
		strB.append("(");

		for (int currentFeature : features) {
			strB.append(currentFeature);
			strB.append(',');
		}

		strB.deleteCharAt(strB.lastIndexOf(","));
		strB.append(")");

		String requiredFeatures = strB.toString();

		return requiredFeatures;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public boolean equals(Object o) {
		return ((o instanceof Subspace) ? (((Subspace) o).getId() == this.id) : false);
	}
}
