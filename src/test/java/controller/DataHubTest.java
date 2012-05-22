package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * The class {@code DataHubTest} offers tests for the {@code DataHub} subsystem.
 */
public class DataHubTest {
	// environment
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_controller_tests";
	private final String dbFile = this.path + "/database-junit-datahub.bsv";
	private Database database = null;
	private SubspaceController subspaceController;
	private GroupController groupController;

	// valid datahub, used for the majority of our tests
	private DataHub datahub = null;

	// dummy values
	private final float[][] objects = { { 1.1f, 1.2f }, { 2.1f, 2.2f }, { 3.1f, 3.2f } };

	/**
	 * Initialize a valid environment for the majority of our tests.
	 */
	@Before
	public void setup() {
		// create working directory
		(new File(path)).mkdirs();

		// make sure the old file is deleted
		(new File(dbFile)).delete();

		try {
			// create a new database
			database = new Database(dbFile);

			// features
			String[] features = { "Feature 1", "Feature 2" };
			boolean[] outlier = { false, false };

			// subspaces
			int sid = 1;
			int[] featureReference = { 1, 2 };
			String sname = "feature 1 and feature 2";

			this.database.initFeatures(features, outlier);
			this.database.pushObject(objects);
			this.database.pushSubspace(sid, featureReference, sname);

			// controller
			subspaceController = new SubspaceController(database);
			groupController = new GroupController(database, subspaceController);

		} catch (InvalidDriverException e) {
			fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Clean up the environment.
	 */
	@After
	public void tearDown() {
		// shutdown
		try {
			database.shutdown();
		} catch (DatabaseAccessException e) {
			fail(e.getMessage());
		}

		// clean up database
		if (database != null) {
			assertEquals(true, (new File(dbFile)).delete());
		}
	}

	/**
	 * Tests the constructor, by passing invalid data.
	 */
	@Test
	public void invalidCtorTest() {
		try {
			datahub = new DataHub(null, groupController, subspaceController);
			fail("Expected exception.");
		} catch (IllegalArgumentException e) {
		}

		try {
			datahub = new DataHub(database, null, subspaceController);
			fail("Expected exception.");
		} catch (IllegalArgumentException e) {
		}

		try {
			datahub = new DataHub(database, groupController, null);
			fail("Expected exception.");
		} catch (IllegalArgumentException e) {
		}

		// valid data
		try {
			datahub = new DataHub(database, groupController, subspaceController);
		} catch (IllegalArgumentException e) {
			fail("Initializing the valid datahub failed.");
		}
	}

	/**
	 * Tests the get data functionality.
	 */
	@Test
	public void getDataTest() {
		// set up the datahub for this test
		this.datahub = new DataHub(database, groupController, subspaceController);

		Group group;
		int[] selection = { 1, 2 };

		try {
			// we have no constraints, so we expect all ids
			assertEquals(objects.length, datahub.getData().length);

			// now add a group with our selection as constraint
			group = groupController.createGroup("Testgroup");
			group.createStaticConstraint(selection);

			// group with constraint active, so we want specific objects
			assertEquals(selection.length, datahub.getData().length);

			// check if the ids are equal
			for (int i = 0; i < selection.length; ++i) {
				assertEquals(selection[i], datahub.getData()[i].getId());
			}

			// check all subspaces
			for (Subspace subspace : subspaceController.getSubspaces()) {

				// and check all features in those subspaces
				for (Feature feature : subspace.getFeatures()) {

					// but only actual values
					if (!feature.isOutlier() && !feature.isVirtual()) {

						// and now verify each value from the getData call, by comparing it with our given objects
						for (ElementData element : datahub.getData()) {

							assertEquals(element.getValue(feature), objects[element.getId() - 1][feature.getId() - 1],
									0.0001f);
						}
					}
				}
			}

		} catch (DatabaseAccessException e) {
			fail("Unexpected exception.");
		}
	}
}
