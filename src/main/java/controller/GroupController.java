package controller;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import util.Failure;
import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;

/**
 * The class {@code GroupController} manages all existing {@link Group}s. You can add and remove a {@link Group} and
 * request a list with all existing {@link Group}s.
 */
public class GroupController extends Observable {

	/**
	 * The {@link Database}, where these {@link Group}s are stored.
	 */
	private final Database database;

	/**
	 * The {@link SubspaceController} to rebuild the {@link Feature}s.
	 */
	private final SubspaceController subspaceController;

	/**
	 * Constructs a new {@code GroupController} and sets the static {@code idCountStaticConstraint} in {@link Group}.
	 *
	 * @param database
	 *            the {@link Database} where the {@link Group}s are stored.
	 * @param subspaceController
	 *            the {@link SubspaceController} the {@link Feature}s refer to.
	 * @throws DatabaseAccessException
	 *             if the read operation failed in {@link Database}.
	 */
	public GroupController(Database database, SubspaceController subspaceController) throws DatabaseAccessException {
		if (database == null || subspaceController == null) {
			throw new IllegalArgumentException("database or subspaceController is null");
		}

		this.database = database;
		this.subspaceController = subspaceController;

		try {
			Statement stmt = database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(Id) FROM StaticConstraints;");

			Group.setIdCountStaticConstraint(rs.getInt(1));

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}
	}

	/**
	 * Create a new {@link Group}, and store it in the {@link Database}.
	 *
	 * @param name
	 *            The name of the new {@link Group}, may not be {@code null}.
	 * @return The new created {@link Group}.
	 * @throws DatabaseAccessException
	 *             if the write operation in the {@link Database} failed.
	 */
	public Group createGroup(String name) throws DatabaseAccessException {
		if (name == null || DatabaseConfiguration.VARCHARLENGTH < name.length()) {
			throw new IllegalArgumentException("name is null or to long");
		}

		int groupId = 0;

		Color groupColor = Color.getHSBColor((float) Math.random(), 1f, 1f);

		try {
			PreparedStatement prepStmt = this.database.getConnection().prepareStatement(
					"INSERT INTO Groups VALUES (NULL,?,?,?,?,?)");
			prepStmt.setString(1, name);
			prepStmt.setBoolean(2, true);
			prepStmt.setInt(3, groupColor.getRGB());
			prepStmt.setInt(4, 0);
			prepStmt.setString(5, "");
			prepStmt.execute();
			prepStmt.close();

			Statement stmt = this.database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(Id) FROM Groups;");

			groupId = rs.getInt(1);

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}

		informOberserver();

		return new Group(this, database, groupId, name, true, groupColor.getRGB(), null, "");
	}

	/**
	 * Deletes the {@link Group} from the {@link Database}.
	 *
	 * @param group
	 *            The {@link Group} to remove.
	 * @throws DatabaseAccessException
	 *             if write operation failed in {@link Database}.
	 */
	public void removeGroup(Group group) throws DatabaseAccessException {
		group.remove();
		informOberserver();
	}

	/**
	 * Returns a list of all existing {@link Group}s, stored in the {@link Database}.
	 *
	 * @return The list of {@link Group}s.
	 * @throws DatabaseAccessException
	 *             if the read operation in the {@link Database} failed.
	 */
	public Group[] getGroups() throws DatabaseAccessException {
		ArrayList<Group> groups = new ArrayList<Group>();

		try {
			HashMap<Integer, Feature> colorFeatures = getRequiredColorFeaturesToRebuildGroups();

			Statement stmt = this.database.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT"
					+ " Id, Name, Visibility, Color, ColorCalculatedByFeature, Description"
					+ " FROM Groups;");

			while (rs.next()) {
				Group group = new Group(this, this.database, rs.getInt(1), rs.getString(2), rs
						.getBoolean(3), rs.getInt(4), colorFeatures.get(rs
						.getInt(5)), rs.getString(6));

				group.rebuildConstraintsFromDatabase();
				groups.add(group);
			}

			stmt.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.READ);
		}

		Group[] groupsArray = new Group[groups.size()];
		groups.toArray(groupsArray);

		return groupsArray;
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
	 * The method gets all needed features to rebuild the list of groups and creates the matching features.
	 *
	 * @return the created list of {@link Feature}s.
	 * @throws SQLException
	 *             error in database access.
	 */
	private HashMap<Integer, Feature> getRequiredColorFeaturesToRebuildGroups() throws SQLException {
		Statement stmt = this.database.getConnection().createStatement();
		ArrayList<Integer> neededFeatures = new ArrayList<Integer>();

		// get all needed Features form the database
		ResultSet rs = stmt.executeQuery("SELECT ColorCalculatedByFeature FROM Groups;");

		while (rs.next()) {
			neededFeatures.add(rs.getInt(1));
		}

		HashMap<Integer, Feature> features = new HashMap<Integer, Feature>();

		for (int i = 0; i < neededFeatures.size(); i++) {
			if (neededFeatures.get(i) < 0) {
				// this is legal because we don't need this values when using this function
				features.put(neededFeatures.get(i), new Feature(this.subspaceController, this.database, -1,
						"Effect. Outlierness", false, true, 0, 1));
			} else if (neededFeatures.get(i) == 0) {
				features.put(0, null);
			} else {
				int featureId = neededFeatures.get(i);
				rs = stmt.executeQuery("SELECT Name, OutlierFlag, Min, Max"
						+ " FROM Features WHERE Id=" + featureId + ";");

				features.put(neededFeatures.get(i), new Feature(this.subspaceController, this.database,
						featureId, rs.getString(1), rs.getBoolean(2), false, rs.getFloat(3),
						rs.getFloat(4)));
			}
		}

		stmt.close();

		return features;
	}

	/**
	 * Returns the {@link SubspaceController} the {@link Feature}s belong to.
	 *
	 * @return the subspace controller.
	 */
	public SubspaceController getSubspaceController() {
		return this.subspaceController;
	}

	/**
	 * This method is called to update all Observer, registered to this {@code GroupController}.
	 */
	public void informOberserver() {
		// update observers
		this.setChanged();
		this.notifyObservers();
	}
}
