package gui;

import static org.junit.Assert.assertEquals;

import gui.views.TableView;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.DataHub;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

public class TableViewTest {
	private final String path = System.getProperty("java.io.tmpdir")
			+ "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private GroupController groupController;
	private SelectionController selectionController;
	private SubspaceController subspaceController;
	private DataHub dataHub;

	/**
	 * sets up all objects required for testing.
	 */
	@Before
	public void setUp() {
		// create working directory
		(new File(this.path)).mkdirs();

		// make sure the old file is deleted
		(new File(this.dbFile)).delete();

		try {
			this.database = new Database(this.dbFile);
			String[] features = { "Feature 1", "Feature 2" };
			boolean[] outlier = { false, false };
			this.database.initFeatures(features, outlier);

			this.subspaceController = new SubspaceController(this.database);
			this.groupController = new GroupController(this.database,
					this.subspaceController);
			this.selectionController = new SelectionController();
			this.dataHub = new DataHub(this.database, this.groupController,
					this.subspaceController);

		} catch (InvalidDriverException e) {
			Assert.fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			Assert.fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
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
			Assert.fail(e.getMessage());
		}

		// clean up database
		if (this.database != null) {
			assertEquals(true, (new File(this.dbFile)).delete());
		}
	}

	/**
	 * Test with illegal values.
	 */
	@Test
	public void testIllegal() {
		try {
			new TableView(null, this.selectionController,
					this.subspaceController);
			Assert.fail("Argument was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new TableView(this.dataHub, null, this.subspaceController);
			Assert.fail("Argument was null");
		} catch (IllegalArgumentException e) {
			// ok
		}
		try {
			new TableView(this.dataHub, this.selectionController, null);
			Assert.fail("Argument was null");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}

	/**
	 * Test with valid values.
	 */
	@Test
	public void testReal() {
		TableView tV = new TableView(this.dataHub, this.selectionController,
				this.subspaceController);
		Assert.assertNotNull(tV);
	}
}
