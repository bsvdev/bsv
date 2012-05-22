package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * The class {@code ElementDataTest} offers tests for the elementdata object.
 */
public class ElementDataTest {
	// environment
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_controller_tests";
	private final String dbFile = this.path + "/database-junit-element.bsv";
	private Database database = null;
	private SubspaceController subspaceController;

	// valid data
	private final int id = 42;
	private final int[] featureIds = { 1, 2, 3 };
	private final float[] values = { 1.1f, 1.2f, 1.3f };
	private final Group[] groups = new Group[0];

	// common valid elementdata object
	private ElementData element = null;

	/**
	 * Initialize a valid elementdata for the majority of our tests.
	 */
	@Before
	public void setup() {
		// create working directory
		(new File(path)).mkdirs();

		// make sure the old file is deleted
		(new File(dbFile)).delete();

		try {
			// create a new database with two features
			database = new Database(dbFile);
			String[] features = { "Feature 1", "Feature 2" };
			boolean[] outlier = { false, false };
			database.initFeatures(features, outlier);

			subspaceController = new SubspaceController(database);

		} catch (InvalidDriverException e) {
			Assert.fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			Assert.fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// valid data
		try {
			this.element = new ElementData(id, featureIds, values, groups);
		} catch (IllegalArgumentException e) {
			fail("Initializing the valid elementdata failed.");
		}
	}

	/**
	 * Clean up, after testing.
	 */
	@After
	public void tearDown() {
		// shutdown
		try {
			database.shutdown();
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
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
		// invalid id
		try {
			this.element = new ElementData(-1, featureIds, values, groups);
		} catch (IllegalArgumentException e) {
		}

		// invalid feature ids
		try {
			this.element = new ElementData(id, null, values, groups);
		} catch (IllegalArgumentException e) {
		}

		// invalid values
		try {
			this.element = new ElementData(id, featureIds, null, groups);
		} catch (IllegalArgumentException e) {
		}

		// value array does not match feature array
		try {
			this.element = new ElementData(id, featureIds, new float[] { 2.1f, 2.2f }, groups);
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Tests the id getter.
	 */
	@Test
	public void getIdTest() {
		assertEquals(id, element.getId());
	}

	/**
	 * Tests the value getter.
	 */
	@Test
	public void getValueTest() {
		try {
			element.getValue(null);
			fail("Expected exception.");
		} catch (IllegalArgumentException e) {
		}

		try {
			element.getValue(new Feature(subspaceController, database, 42, "dimX", false, false, 1f, 100f));
			fail("Expected exception.");
		} catch (NullPointerException e) {
		}

		assertEquals(values[1], element.getValue(new Feature(subspaceController, database, featureIds[1], "dimX",
				false, false, 1f, 100f)), 0.01f);
	}

	/**
	 * Tests the groups getter.
	 */
	@Test
	public void getGroupsTest() {
		if (element.getGroups() != groups) {
			fail("Unexpected return value.");
		}
	}

	/**
	 * Tests the insertion of new values.
	 */
	@Test
	public void addValueTest() {
		Feature feature = new Feature(subspaceController, database, 42, "dimX", false, false, 1f, 100f);
		float value = 98.87f;

		element.addValue(feature.getId(), value);

		assertEquals(value, element.getValue(feature), 0.01f);
	}
}
