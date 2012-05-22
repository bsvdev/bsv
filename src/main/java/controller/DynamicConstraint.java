package controller;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import util.Failure;
import util.Operator;
import db.Database;
import db.DatabaseAccessException;

/**
 * The class {@code DynamicConstraint} describes a set of points using an {@link Operator}, limit and one
 * {@link Feature}.
 */
public class DynamicConstraint implements Constraint {

	/**
	 * A unique identifier, set by the {@link Database}.
	 */
	private final int id;

	/**
	 * The {@code Constraint} operates on this {@code Feature}.
	 */
	private Feature feature;

	/**
	 * The values are compared by this {@code Operator}.
	 */
	private Operator operator;

	/**
	 * Determines the {@code value} to compare on.
	 */
	private float value;

	/**
	 * Defines whether this {@code Constraint} is active.
	 */
	private boolean active;

	/**
	 * The {@link Database}, where this {@code DynamicConstraint} is stored.
	 */
	private final Database database;

	/**
	 * The {@link GroupController} the {@link Group} was created.
	 */
	private final GroupController groupController;

	/**
	 * Constructs a new DynamicConstraint
	 * 
	 * @param groupController
	 *            The {@link GroupController} the {@link Group} was created.
	 * @param database
	 *            The {@link Database} where this {@code DynamicConstraint} is stored. The parameter may not be {@code
	 *            null}.
	 * @param id
	 *            The identifier for this {@code DynamicConstraint}. The id may not be negative.
	 * @param feature
	 *            The {@link Feature} used to compare, it may not be {@code null}.
	 * @param operator
	 *            The {@link Operator} used to compare.
	 * @param value
	 *            The value used to compare.
	 * @param active
	 *            The state of the {@code Constraint}.
	 */
	public DynamicConstraint(GroupController groupController, Database database, int id, Feature feature,
			Operator operator, float value, boolean active) {
		if (groupController == null || database == null || id < 1 || feature == null || operator == null) {
			throw new IllegalArgumentException("database, feature and operator may not be null, id must be positive");
		}

		this.groupController = groupController;
		this.database = database;
		this.id = id;
		this.feature = feature;
		this.operator = operator;
		this.value = value;
		this.active = active;
	}

	@Override
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the {@link Feature} used in this {@code Constraint}.
	 * 
	 * @return The {@link Feature} used to compare.
	 */
	public Feature getFeature() {
		return this.feature;
	}

	/**
	 * Returns the {@link Operator} used in this {@code Constraint}.
	 * 
	 * @return The {@link Operator} used to compare.
	 */
	public Operator getOperator() {
		return this.operator;
	}

	/**
	 * Returns the limit used in this {@code Constraint}.
	 * 
	 * @return The limit used to compare.
	 */
	public float getValue() {
		return this.value;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Sets the {@link Feature} used in this {@code Constraint} and updates it in the {@link Database}.
	 * 
	 * @param feature
	 *            The new {@link Feature}, may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if write operation failed in {@link Database}.
	 */
	public void setFeature(Feature feature) throws DatabaseAccessException {
		if (feature == null) {
			throw new IllegalArgumentException("feature may not be null");
		}

		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE DynamicConstraints SET FeatureReference=? WHERE Id=?;");
			prepStmt.setInt(1, feature.getId());
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.feature = feature;
	}

	/**
	 * Defines the {@link Operator} used in this {@code Constraint} and updates it in the {@link Database}.
	 * 
	 * @param operator
	 *            The new {@link Operator}, may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public void setOperator(Operator operator) throws DatabaseAccessException {
		if (operator == null) {
			throw new IllegalArgumentException("operator may not be null");
		}

		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE DynamicConstraints SET Operator=? WHERE Id=?;");
			prepStmt.setInt(1, operator.ordinal());
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.operator = operator;
	}

	@Override
	public void setActive(boolean active) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE DynamicConstraints SET Active=? WHERE Id=?;");
			prepStmt.setBoolean(1, active);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.active = active;
	}

	/**
	 * Sets the limit used in this {@code Constraint} and updates the value in the {@link Database}.
	 * 
	 * @param value
	 *            The new limit.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public void setValue(float value) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE DynamicConstraints SET Value=? WHERE Id=?;");
			prepStmt.setFloat(1, value);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.value = value;
	}

	@Override
	public void remove() throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"DELETE FROM DynamicConstraints WHERE Id=?;");
			prepStmt.setInt(1, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}
}
