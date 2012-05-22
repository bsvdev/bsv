package gui;

import static org.junit.Assert.assertEquals;
import gui.groupsPanel.GroupsPanel;
import gui.groupsPanel.SingleConstraintPanel;
import gui.groupsPanel.SingleGroup;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.Group;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Tests the SingleConstraintPanel.
 *
 */
public class SingleConstraintPanelTest {

	private final String path = System.getProperty("java.io.tmpdir")
			+ "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private SubspaceController subspaceController;
	private GroupController groupController;
	private SelectionController selectionController;

	private Group group;
	private SingleGroup singleGroup;
	private GroupsPanel groupsPanel;

	private SingleConstraintPanel scp;

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
			this.group = groupController.createGroup("Group1");
			this.groupsPanel = new GroupsPanel(this.groupController,
					this.selectionController, this.subspaceController);
			this.singleGroup = new SingleGroup(this.groupController,
					this.selectionController, this.subspaceController,
					this.group, this.groupsPanel);
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
	public void badArgumentsTest() {
		try {
			new SingleConstraintPanel(this.subspaceController, null, null,
					this.singleGroup);
			Assert.fail("Controller was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleConstraintPanel(null, this.group, null, this.singleGroup);
			Assert.fail("Controller was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleConstraintPanel(this.subspaceController, this.group,
					null, this.singleGroup);
			// ok
		} catch (IllegalArgumentException e) {
			Assert.fail("Controller was null");
		} catch (NullPointerException e) {
			Assert.fail("Controller was null");
		}

		try {
			new SingleConstraintPanel(this.subspaceController, this.group,
					null, null);
			Assert.fail("Controller was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleConstraintPanel(null, null, null, null);
			Assert.fail("Controller was null");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}
	
	/**
	 * Tests with vaid values.
	 */
	@Test
	public void realTest() {
		this.scp = new SingleConstraintPanel(this.subspaceController,
				this.group, null, this.singleGroup);
		Assert.assertNotNull(this.scp);
	}
}
