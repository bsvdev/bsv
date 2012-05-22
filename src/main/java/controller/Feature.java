package controller;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import util.Failure;
import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;

/**
 * The class {@code Feature} represents one measured feature within the data record.
 */
public class Feature {

	/**
	 * A unique identifier, set by the {@link Database}.
	 */
	private final int id;

	/**
	 * The name of the {@code Feature}, shown in the UI.
	 */
	private String name;

	/**
	 * The outlier flag of the {@code Feature}, shown in the UI.
	 */
	private final boolean outlier;

	/**
	 * The flag indicates, that this {@code Feature} doesn't exist in database.
	 */
	private final boolean virtual;

	/**
	 * Stores minimum value of all elements data for this feature.
	 */
	private final float min;

	/**
	 * Stores maximum value of all elements data for this feature.
	 */
	private final float max;

	/**
	 * The {@link Database}, where this {@code Feature} is stored.
	 */
	private final Database database;

	/**
	 * The {@link SubspaceController} the {@link Subspace}, this {@code Feature} belongs to, is managed.
	 */
	private final SubspaceController subspaceController;

	/**
	 * Constructs a new {@code Feature} with identifier and name.
	 * 
	 * @param subspaceController
	 *            the {@link SubspaceController} the {@link Subspace} ,this {@code Feature} belongs to, is managed. The
	 *            information is used to trigger the right observer.
	 * @param database
	 *            the {@link Database}, where this {@code Feature} is stored.
	 * @param id
	 *            a not negative unique identifier.
	 * @param name
	 *            the name of the {@code Feature}, may not be {@code null}.
	 * @param outlier
	 *            the outlier flag of the {@code Feature}.
	 * @param virtual
	 *            true, if the {@code Feature} doesn't exist in database.
	 * @param min
	 *            minimum value of all elements data for this feature.
	 * @param max
	 *            maximum value of all elements data for this feature.
	 */
	public Feature(SubspaceController subspaceController, Database database, int id, String name, boolean outlier,
			boolean virtual, float min, float max) {
		if (subspaceController == null || database == null || name == null
				|| DatabaseConfiguration.VARCHARLENGTH < name.length()) {
			throw new IllegalArgumentException("subspaceController, database, name is null or to long");
		}

		this.subspaceController = subspaceController;
		this.database = database;
		this.id = id;
		this.name = name;
		this.outlier = outlier;
		this.virtual = virtual;
		this.min = min;
		this.max = max;
	}

	/**
	 * Returns the identifier of this {@code Feature}.
	 * 
	 * @return the identifier.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the name of this {@code Feature}.
	 * 
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns true if this {@code Feature} is a outlier indicator.
	 * 
	 * @return the outlier flag.
	 */
	public boolean isOutlier() {
		return this.outlier;
	}

	/**
	 * Indicates if the {@code Feature} is virtual and doesn't exist in database.
	 * 
	 * @return true, if it is virtual.
	 */
	public boolean isVirtual() {
		return this.virtual;
	}

	/**
	 * Change the name of this {@code Feature}.
	 * 
	 * @param name
	 *            the new name, it may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if write operation failed in {@link Database}.
	 */
	public void setName(String name) throws DatabaseAccessException {
		if (name == null || DatabaseConfiguration.VARCHARLENGTH < name.length()) {
			throw new IllegalArgumentException("name is null or to long");
		}

		// do not act on a virtual feature
		if (this.id > 0) {
			try {
				PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
						"UPDATE Features SET Name=? WHERE Id=?;");
				prepStmt.setString(1, name);
				prepStmt.setInt(2, id);
				prepStmt.execute();
				prepStmt.close();

				this.subspaceController.informObservers();
			} catch (SQLException e) {
				throw new DatabaseAccessException(Failure.WRITE);
			}
		}

		this.name = name;
	}

	/**
	 * Returns the max length for a {@code String} handled by the {@code Database}.
	 * 
	 * @return the max length.
	 */
	public int maxStringLength() {
		return DatabaseConfiguration.VARCHARLENGTH;
	}

	/**
	 * Returns the max value emerging in this {@code Feature}. If the id of this {@code Feature} is negative, the value
	 * is get from {@link SubspaceController} {@code getEOMaxValue}.
	 * 
	 * @return the max value.
	 */
	public float getMaxValue() {
		if (this.id < 0) {
			return this.subspaceController.getEOMaxValue();
		} else {
			return this.max;
		}
	}

	/**
	 * Returns the min value emerging in this {@code Feature}. If the id of this {@code Feature} is negative, the value
	 * is get from {@link SubspaceController} {@code getEOMinValue}.
	 * 
	 * @return the min value.
	 */
	public float getMinValue() {
		if (this.id < 0) {
			return this.subspaceController.getEOMinValue();
		} else {
			return this.min;
		}
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
