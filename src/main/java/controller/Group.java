package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import util.Failure;
import util.Operator;
import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;

/**
 * The class {@code Group} represents a subset of points. These points are specified by {@link Constraint}s.
 */
public class Group {

	/**
	 * A counter to set the id for a new created {@link StaticConstraint}. It should be set in front of first usage of
	 * {@code Group} and hold the last used id.
	 */
	private static int idCountStaticConstraint;

	/**
	 * A unique identifier, set by the {@link Database}.
	 */
	private final int id;

	/**
	 * The name of this {@code Group}, set by the user in the UI.
	 */
	private String name;

	/**
	 * Determines, whether the points belonging to this {@code Group} are visible in the UI.
	 */
	private boolean visible;

	/**
	 * The color visualizing this {@code Group} in the UI.
	 */
	private int color;

	/**
	 * The {@link Feature} used to calculate the color dynamically in the UI.
	 */
	private Feature colorFeature;

	/**
	 * The description to this {@code Group}.
	 */
	private String description;

	/**
	 * This list holds all {@link Constraint}s, referring to this group.
	 */
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();

	/**
	 * The {@link Database}, where this {@code Group} is stored.
	 */
	private final Database database;

	/**
	 * The {@link GroupController} this {@code Group} was created at.
	 */
	private final GroupController groupController;

	/**
	 * Constructs a new {@code Group}. The attribute {@code valid} is set true.
	 *
	 * @param groupController
	 *            the {@link GroupController} this {@code Group} was cerated at.
	 * @param database
	 *            The {@link Database}, where this {@code Group} is stored. The parameter may not be {@code null}.
	 * @param id
	 *            A unique identifier, it should not be negative.
	 * @param name
	 *            The name of this {@code Group}, may not be {@code null}.
	 * @param visible
	 *            Determines, whether this {@code Group} is visible in the UI.
	 * @param color
	 *            The color of this {@code Group} in the UI.
	 * @param colorFeature
	 *            {@link Feature} used to calculate the color dynamically. {@code null}, if there is no such
	 *            {@link Feature}.
	 * @param description
	 *            The description of this {@code Group}, may not be {@code null}.
	 */
	public Group(GroupController groupController, Database database, int id, String name, boolean visible, int color,
			Feature colorFeature, String description) {
		if (groupController == null || database == null || id < 1 || name == null
				|| DatabaseConfiguration.VARCHARLENGTH < name.length() || description == null) {
			throw new IllegalArgumentException(
					"database, name and description may not be null and the id has to be positive");
		}

		this.database = database;
		this.groupController = groupController;
		this.id = id;
		this.name = name;
		this.visible = visible;
		this.color = color;
		this.colorFeature = colorFeature;
		this.description = description;
		this.constraints = new ArrayList<Constraint>();
	}

	/**
	 * Return the unique identifier of this {@code Group}.
	 *
	 * @return the identifier.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Return the name of this {@code Group}, shown in the UI.
	 *
	 * @return the name of this {@code Group}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns whether this {@code Group} is currently visible in the UI.
	 *
	 * @return True, if it is visible.
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/**
	 * Returns the color visualizing this {@code Group} in the UI.
	 *
	 * @return The color of the {@code Group}.
	 */
	public int getColor() {
		return this.color;
	}

	/**
	 * Returns the {@link Feature} used to calculate the color in the UI dynamically. Returns {@code null} if there is
	 * no such {@link Feature}.
	 *
	 * @return The Feature.
	 */
	public Feature getColorFeature() {
		return this.colorFeature;
	}

	/**
	 * Returns the description of this {@code Group}.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Gives this {@code Group} a new name and updates it in the {@link Database}.
	 *
	 * @param name
	 *            The new name, may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public void setName(String name) throws DatabaseAccessException {
		if (name == null || DatabaseConfiguration.VARCHARLENGTH < name.length()) {
			throw new IllegalArgumentException("name may not be null or is to long");
		}

		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE Groups SET Name=? WHERE Id=?;");
			prepStmt.setString(1, name);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.name = name;
	}

	/**
	 * Determines whether the {@code Group} is currently visible in the UI and updates it in the {@link Database}.
	 *
	 * @param visible
	 *            True, if it is visible.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public void setVisible(boolean visible) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE Groups SET Visibility=? WHERE Id=?;");
			prepStmt.setBoolean(1, visible);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.visible = visible;
	}

	/**
	 * Gives this {@code Group} a new color, used to visualize it in the UI, and updates it in the {@link Database}.
	 *
	 * @param color
	 *            The new color.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public void setColor(int color) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE Groups SET Color=? WHERE Id=?;");
			prepStmt.setInt(1, color);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.color = color;
	}

	/**
	 * Gives this {@code Group} a new {@link Feature}, used to calculate the color in the UI dynamically, and updates it
	 * in the {@link Database}.
	 *
	 * @param colorFeature
	 *            The new Feature or {@code null} if there is no such {@link Feature}.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public void setColorFeature(Feature colorFeature) throws DatabaseAccessException {
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE Groups SET ColorCalculatedByFeature=? WHERE Id=?;");

			prepStmt.setInt(1, colorFeature == null ? 0 : colorFeature.getId());
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.colorFeature = colorFeature;
	}

	/**
	 * Changes the description of this {@code Group} and updates it in the {@link Database}.
	 *
	 * @param description
	 *            The new description, may not be {@code null}.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public void setDescription(String description) throws DatabaseAccessException {
		if (description == null) {
			throw new IllegalArgumentException("description may not be null");
		}

		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"UPDATE Groups SET Description=? WHERE Id=?;");
			prepStmt.setString(1, description);
			prepStmt.setInt(2, this.id);
			prepStmt.execute();
			prepStmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		this.description = description;
	}

	/**
	 * This method creates a new {@link DynamicConstraint}, stores it in the {@link database} and adds it to the list of
	 * all constraints.
	 *
	 * @param feature
	 *            The {@link Feature} of the new {@link DynamicConstraint}, may not be {@code null}.
	 * @param operator
	 *            The {@link Operator} of the new {@link DynamicConstraint}.
	 * @param value
	 *            The value of the new {@link DynamicConstraint}.
	 * @return The new constructed {@link DynamicConstraint}.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public DynamicConstraint createDynamicConstraint(Feature feature, Operator operator, float value)
			throws DatabaseAccessException {
		if (feature == null || operator == null) {
			throw new IllegalArgumentException("feature and operator may not be null");
		}

		// The newly created constraint
		DynamicConstraint newConstraint = null;

		// Create a new constraint, pass it to the database and get its new id.
		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"INSERT INTO DynamicConstraints VALUES(NULL,?,?,?,?,?);");
			prepStmt.setInt(1, operator.ordinal());
			prepStmt.setInt(2, feature.getId());
			prepStmt.setInt(3, this.id);
			prepStmt.setFloat(4, value);
			prepStmt.setBoolean(5, true);
			prepStmt.execute();
			prepStmt.close();

			Statement stmt = this.database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(Id) FROM DynamicConstraints;");

			newConstraint = new DynamicConstraint(this.groupController, this.database, rs.getInt(1), feature, operator,
					value, true);

			stmt.close();

			constraints.add(newConstraint);

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		return newConstraint;
	}

	/**
	 * This method creates a new {@link StaticConstraint}, stores it in the {@link database} and adds it to the list of
	 * all constraints.
	 *
	 * @param selection
	 *            A list of ids from points. The parameter may not be {@code null} and the list has at least one item.
	 * @return The new constructed {@link StaticConstraint}.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public StaticConstraint createStaticConstraint(int[] selection) throws DatabaseAccessException {
		if (selection == null || selection.length == 0) {
			throw new IllegalArgumentException("selection is null or the array has no element");
		}

		// The newly created constraint
		StaticConstraint newConstraint = null;

		// Create a new constraint, pass it to the database and get its new id.
		try {
			Connection connection = this.database.getConnection();

			// increment to determine the id for the next constraint
			idCountStaticConstraint++;

			PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO StaticConstraints VALUES(?,?,?,?);");
			prepStmt.setInt(1, idCountStaticConstraint);
			prepStmt.setInt(2, this.id);
			prepStmt.setBoolean(4, true);

			// add all single points to the batch
			for (int current : selection) {
				prepStmt.setInt(3, current);
				prepStmt.addBatch();
			}

			// execute the batch
			connection.setAutoCommit(false);
			prepStmt.executeBatch();
			connection.setAutoCommit(true);

			prepStmt.close();

			newConstraint = new StaticConstraint(this.groupController, this.database, idCountStaticConstraint, this.id,
					selection, true);

			constraints.add(newConstraint);

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		return newConstraint;
	}

	/**
	 * Returns a list of all {@link Constraint}s belonging to this {@code Group}.
	 *
	 * @return The list of constraints or an empty array if there are no constraints.
	 */
	public Constraint[] getConstraints() {
		Constraint[] constraintsArray = new Constraint[constraints.size()];
		this.constraints.toArray(constraintsArray);

		return constraintsArray;
	}

	/**
	 * Remove the {@link Constraint} from the list of constraints and from the {@link Database}.
	 *
	 * @param constraint
	 *            The constraint to delete.
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	public void removeConstraint(Constraint constraint) throws DatabaseAccessException {
		constraint.remove();
		this.constraints.remove(constraint);
	}

	/**
	 * Delete this {@code Group} from the {@link Database}.
	 *
	 * @throws DatabaseAccessException
	 *             if write operation failed in {@link Database}.
	 */
	public void remove() throws DatabaseAccessException {
		try {
			for (Constraint current : this.constraints) {
				current.remove();
			}

			Statement stmt = this.database.getConnection().createStatement();
			stmt.execute("DELETE FROM Groups WHERE Id=" + this.id + ";");
			stmt.close();

			this.groupController.informOberserver();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}

	/**
	 * Returns the max length for a {@code String} handled by the {@code Database}.
	 *
	 * @return the max length.
	 */
	public int maxStringLength() {
		return DatabaseConfiguration.VARCHARLENGTH;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * The method is used to set the the static parameter {@code idCountStaticConstraint}.
	 *
	 * @param idCountStaticConstraint
	 *            the start count.
	 */
	public static void setIdCountStaticConstraint(int idCountStaticConstraint) {
		Group.idCountStaticConstraint = idCountStaticConstraint;
	}

	/**
	 * This method dismisses the current intern list of {@link Constraint}s and rebuilds it with the {@link Constraint}s
	 * stored in the {@link Database}.
	 *
	 * @throws DatabaseAccessException
	 *             if read operation failed in {@link Database}.
	 */
	public void rebuildConstraintsFromDatabase() throws DatabaseAccessException {
		this.constraints = new ArrayList<Constraint>();

		try {
			// get all needed features and build the features from database
			Feature[] features = getRequiredFeaturesToRebuildDynamicConstraints();

			rebuildDynamicConstraints(features);
			rebuildStaticConstraints();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}
	}

	/**
	 * The method gets all needed features to rebuild the list of dynamic constraints and creates the matching features.
	 *
	 * @return the created list of {@link Feature}s.
	 * @throws SQLException
	 *             error in database access.
	 */
	private Feature[] getRequiredFeaturesToRebuildDynamicConstraints() throws SQLException {
		Statement stmt = this.database.getConnection().createStatement();
		ArrayList<Integer> neededFeatures = new ArrayList<Integer>();

		// get all needed Features form the database
		ResultSet rs = stmt.executeQuery("SELECT FeatureReference FROM DynamicConstraints WHERE GroupReference="
				+ this.id + ";");

		while (rs.next()) {
			neededFeatures.add(rs.getInt(1));
		}

		Feature[] features = new Feature[neededFeatures.size()];

		for (int i = 0; i < features.length; i++) {
			int featureId = neededFeatures.get(i);

			if (featureId < 0) {
				// this is legal because we don't need this values when using this function
				features[i] = new Feature(this.groupController.getSubspaceController(), this.database, -1,
						"Effect. Outlierness", false, true, 0, 1);
			} else {
				rs = stmt.executeQuery("SELECT Name, OutlierFlag, Min, Max"
						+ " FROM Features WHERE Id=" + featureId + ";");
				features[i] = new Feature(this.groupController.getSubspaceController(), this.database, featureId,
						rs.getString(1), rs.getBoolean(2), false, rs.getFloat(3), rs
								.getFloat(4));
			}
		}

		stmt.close();

		return features;
	}

	/**
	 * The method rebuilds all {@link DynamicConstraint}s and adds them to the list of {@code Constraint}s.
	 *
	 * @param features
	 *            the list of {@link Feature}s required in the {@link DynamicConstraints}.
	 * @throws SQLException
	 *             error in database access.
	 */
	private void rebuildDynamicConstraints(Feature[] features) throws SQLException {
		Statement stmt = this.database.getConnection().createStatement();

		// get all dynamic constraints and add them to the constraint list
		ResultSet rs = stmt.executeQuery("SELECT FeatureReference, Id, Operator, Value, Active"
				+ " FROM DynamicConstraints WHERE GroupReference=" + this.id + ";");

		while (rs.next()) {
			boolean found = false;
			Feature feature = null;

			int searchId = rs.getInt(1);

			// get the required feature
			for (int j = 0; j < features.length && !found; j++) {
				if (features[j].getId() == searchId) {
					feature = features[j];
					found = true;
				}
			}

			this.constraints.add(new DynamicConstraint(this.groupController, this.database, rs.getInt(2), feature,
					Operator.values()[rs.getInt(3)], rs.getFloat(4), rs.getBoolean(5)));
		}

		stmt.close();
	}

	/**
	 * The method rebuilds all {@link StaticConstraint}s and adds them to the list of constraints.
	 *
	 * @throws SQLException
	 *             error in database access.
	 */
	private void rebuildStaticConstraints() throws SQLException {
		Statement stmt = this.database.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT DISTINCT Id FROM StaticConstraints WHERE GroupReference=" + this.id
				+ ";");
		ArrayList<Integer> staticConstraintIds = new ArrayList<Integer>();

		while (rs.next()) {
			staticConstraintIds.add(rs.getInt(1));
		}

		for (int i = 0; i < staticConstraintIds.size(); i++) {
			int constraintID = staticConstraintIds.get(i);
			rs = stmt.executeQuery("SELECT Active, ObjectReference"
					+ " FROM StaticConstraints WHERE GroupReference=" + this.id + " AND Id="
					+ constraintID + ";");
			ArrayList<Integer> selection = new ArrayList<Integer>();

			boolean active = rs.getBoolean(1);
			selection.add(rs.getInt(2));

			while (rs.next()) {
				selection.add(rs.getInt(2));
			}

			int[] selectionArray = new int[selection.size()];

			for (int j = 0; j < selection.size(); j++) {
				selectionArray[j] = selection.get(j);
			}

			this.constraints.add(new StaticConstraint(this.groupController, this.database,
					constraintID, this.id, selectionArray, active));
		}

		stmt.close();
	}
}
