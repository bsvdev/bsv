package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import util.Failure;
import db.Database;
import db.DatabaseAccessException;

/**
 * A {@code StaticConstraint} is a {@link Constraint}, which defines a set of points via a selection in the UI.
 */
public class StaticConstraint implements Constraint {

	/**
	 * A unique identifier, set by the {@link Database}.
	 */
	private final int id;

	/**
	 * This Constraint refers to the {@link Group} with this id.
	 */
	private final int groupId;

	/**
	 * A list of all ids from points belonging to this {@code StaticConstraint}.
	 */
	private int[] ids;

	/**
	 * Determines, whether this {@code Constraint} is active.
	 */
	private boolean active;

	/**
	 * The {@link Database}, where this {@code Group} is stored.
	 */
	private final Database database;

	/**
	 * The {@link GroupController} the {@link Group} was created.
	 */
	private final GroupController groupController;

	/**
	 * Constructs a new {@code StaticConstraint}. The attribute {@code valid} is set true.
	 * 
	 * @param groupController
	 *            the {@link GroupController} where the {@link Group} , this constraint belongs to, is managed.
	 * @param database
	 *            The {@link Database}, where this {@code Group} is stored. The parameter may not be {@code null}.
	 * @param id
	 *            A not negative unique identifier.
	 * @param groupId
	 *            The {@link Group} this {@code Constraint} belongs to, it may not be negative.
	 * @param ids
	 *            A list of all ids from points. The parameter may not be {@code null} and must have at least one item.
	 * @param active
	 *            The state of this {@code Constraint}.
	 */
	public StaticConstraint(GroupController groupController, Database database, int id, int groupId, int[] ids,
			boolean active) {
		if (groupController == null || database == null || id < 1 || groupId < 1 || ids == null || ids.length == 0) {
			throw new IllegalArgumentException("database is null, id or groupId is negative or ids no array");
		}

		this.groupController = groupController;
		this.database = database;
		this.id = id;
		this.groupId = groupId;
		this.ids = ids;
		this.active = active;
	}

	@Override
	public int getId() {
		return this.id;
	}

	/**
	 * Returns a list of all ids from points, belonging to this {@code StaticConstraint}.
	 * 
	 * @return A list of all ids.
	 */
	public int[] getSelection() {
		return this.ids;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(boolean active) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE StaticConstraints SET Active=? WHERE Id=?;");
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
	 * Sets the list of ids from points, belonging to this {@code StaticConstraint} and updates it in the
	 * {@link Database}.
	 * 
	 * @param ids
	 *            A list of ids.
	 * @throws IllegalArgumentException
	 *             if {@code ids} is {@code null} or if the array has length zero.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public void setSelection(int[] ids) throws IllegalArgumentException, DatabaseAccessException {
		if (ids == null || ids.length == 0) {
			throw new IllegalArgumentException("ids may not be null and the list must have one item");
		}

		try {
			// Remove the old list in the database, and build a completely new one
			// b/c deleting and adding items in the database is complex
			Connection connection = this.database.getConnection();

			this.remove();

			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"INSERT INTO StaticConstraints Values(?,?,?,?);");
			prepStmt.setInt(1, this.id);
			prepStmt.setInt(2, this.groupId);
			prepStmt.setBoolean(4, this.active);

			// Insert all elements to the batch and execute them in one block
			for (int current : ids) {
				prepStmt.setInt(3, current);
				prepStmt.addBatch();
			}

			connection.setAutoCommit(false);
			prepStmt.executeBatch();
			connection.setAutoCommit(true);
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.ids = ids;
	}

	@Override
	public void remove() throws DatabaseAccessException {
		try {
			Statement stmt = this.database.getConnection().createStatement();
			stmt.execute("DELETE FROM StaticConstraints WHERE Id=" + this.id + ";");
			stmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}
}
