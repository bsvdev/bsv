package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class {@code DatabaseTest} offers tests for the database storage system.
 */
public class DatabaseTest {
	// temporary file; for now we use the local resource folder to store the sqlite database
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_test";
	private final String dbFile = this.path + "/database-junit.bsv";
	private Database database = null;

	/**
	 * Set up a clean database before we do the testing on it.
	 */
	@Before
	public void setup() {
		// create working directory
		(new File(this.path)).mkdirs();

		// make sure the old file is deleted
		(new File(this.dbFile)).delete();

		try {
			this.database = new Database(this.dbFile);
		} catch (InvalidDriverException e) {
			fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Clean up, after testing.
	 */
	@After
	public void tearDown() {
		// shutdown
		try {
			this.database.shutdown();
		} catch (DatabaseAccessException e) {
			fail(e.getMessage());
		}

		// clean up database
		if (this.database != null) {
			assertEquals(true, (new File(this.dbFile)).delete());
		}
	}

	/**
	 * Test the connection to the database.
	 */
	@Test
	public void getConnectionTest() {
		if (this.database == null) {
			fail("Database not initialized.");
		} else if (this.database.getConnection() == null) {
			fail("Unable to get a database connection.");
		}
	}

	/**
	 * Test the emptiness of the database.
	 */
	@Test
	public void isEmptyTest() {
		if (this.database == null) {
			fail("Database not initialized.");
		} else if (this.database.isEmpty() == false) {
			fail("Database should be empty.");
		}
	}

	/**
	 * Test the insertion of objects.
	 */
	@Test
	public void pushObjectTest() {
		// no pushing of objects possible without features
		initFeaturesTest();

		float[][] objects = { { 1.1f, 1.2f, 1.3f }, { 2.1f, 2.2f, 2.3f }, { 3.1f, 3.2f, 3.3f } };

		try {
			this.database.pushObject(objects);
		} catch (DatabaseAccessException e) {
			fail("Could not push objects.");
		}
	}

	/**
	 * Test the initialization of features.
	 */
	@Test
	public void initFeaturesTest() {
		String[] features = { "dim0", "dim1", "dim2" };
		boolean[] outlierFlags = { false, false, true };

		try {
			this.database.initFeatures(features, outlierFlags);
		} catch (DatabaseAccessException e) {
			fail("Could not initialize the features.");
		}

		Assert.assertFalse(this.database.isEmpty());
	}

	/**
	 * Test the insertion of subspaces.
	 */
	@Test
	public void pushSubspaceTest() {
		int id = 1;
		int[] featureReference = { 0, 1, 2, 42 };
		String name = "dim0 dim1 dim2 with outlier";

		try {
			this.database.pushSubspace(id, featureReference, name);
		} catch (DatabaseAccessException e) {
			fail("Could not push the subspace.");
		}
	}

	/**
	 * Test return path of the database.
	 */
	@Test
	public void getPathTest() {
		Assert.assertTrue(this.dbFile.equals(this.database.getPath()));
	}

	/**
	 * Test the updating of the minimum/maximum of all features.
	 */
	@Test
	public void updateFeaturesMinMaxTest() {
		// features
		String[] features = { "dim0", "dim1", "dim2" };
		boolean[] outlierFlags = { false, false, true };

		// objects
		float[][] objects = { { 1.1f, 1.2f, 1.3f }, { 2.1f, 2.2f, 2.3f }, { 3.1f, 3.2f, 3.3f } };

		try {
			this.database.initFeatures(features, outlierFlags);
			this.database.pushObject(objects);
			this.database.updateFeaturesMinMax();
		} catch (DatabaseAccessException e) {
			fail("Could not update the minimum/maximum.");
		}
	}

	/**
	 * Test the correct behavior of a in-memory database.
	 */
	@Test
	public void inMemoryTest() {
		Database database = null;
		String path = ":memory:";

		try {
			database = new Database(path);
		} catch (InvalidDriverException e) {
			fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			fail(e.getMessage());
		}

		// path should be :memory: only
		if (database != null) {
			assertEquals(database.getPath(), path);
		}

		// all other tests have to work on a in-memory database as well
		String[] features = { "dim0", "dim1", "dim2" };
		boolean[] outlierFlags = { false, false, true };

		float[][] objects = { { 1.1f, 1.2f, 1.3f }, { 2.1f, 2.2f, 2.3f }, { 3.1f, 3.2f, 3.3f } };

		try {
			this.database.initFeatures(features, outlierFlags);
			this.database.pushSubspace(1, new int[] { 2, 3 }, "testSubspace");
			this.database.pushObject(objects);
			this.database.updateFeaturesMinMax();
		} catch (DatabaseAccessException e) {
			fail("In-memory database is not working correctly.");
		}
	}
}