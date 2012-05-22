package gui;

import static org.junit.Assert.assertEquals;
import gui.groupsPanel.ShowGroups;
import gui.views.DataTableModel;
import gui.views.DetailView;
import gui.views.TableView;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.DataHub;
import controller.ElementData;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Tests the DetailView class.
 * 
 */
public class DetailViewTest {
	private final String path = System.getProperty("java.io.tmpdir")
			+ "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private SelectionController selectionController;
	private SubspaceController subspaceController;
	private GroupController groupController;

	private DataHub dataHub;
	private DetailView detailView;

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
			this.selectionController = new SelectionController();

			
			this.groupController = new GroupController(this.database,
					this.subspaceController);
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
			new DetailView(null, this.selectionController,
					this.subspaceController);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}
		try {
			new DetailView(this.dataHub, null,
					this.subspaceController);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}
		try {
			new DetailView(this.dataHub, this.selectionController,
					null);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			// ok
		}
	}
	
	/**
	 * Tests with valid values
	 */
	@Test
	public void testValid() {
		this.detailView = new DetailView(this.dataHub, this.selectionController,
				this.subspaceController);
		Assert.assertNotNull(this.detailView);
	}
}
