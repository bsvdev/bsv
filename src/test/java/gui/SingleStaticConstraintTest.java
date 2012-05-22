package gui;

import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gui.groupsPanel.GroupsPanel;
import gui.groupsPanel.SingleGroup;
import gui.groupsPanel.SingleStaticConstraint;
import controller.Group;
import controller.GroupController;
import controller.SelectionController;
import controller.StaticConstraint;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Tests the SingleStaticConstraint class.
 *
 */
public class SingleStaticConstraintTest {
	private final String path = System.getProperty("java.io.tmpdir")
			+ "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private SubspaceController subspaceController;
	private GroupController groupController;
	private SelectionController selectionController;
	private StaticConstraint staticConstraint;
	private Group group;
	private SingleGroup singleGroup;
	private GroupsPanel groupsPanel;

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
			int[] elements = new int[1];
			elements[0] = 1;
			this.selectionController.select(elements);
			this.staticConstraint = this.group
					.createStaticConstraint(this.selectionController
							.getSelection());
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
			new SingleStaticConstraint(null, this.group, this.staticConstraint,
					this.singleGroup);
			Assert.fail("Controller was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleStaticConstraint(this.selectionController, null,
					this.staticConstraint, this.singleGroup);
			Assert.fail("Group was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleStaticConstraint(this.selectionController, this.group,
					null, this.singleGroup);
			Assert.fail("StaticConstraint was null");
		} catch (IllegalArgumentException e) {
			// ok
		}

		try {
			new SingleStaticConstraint(this.selectionController, this.group,
					this.staticConstraint, null);
			Assert.fail("StaticConstraint was null");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}
	
	/**
	 * Test with valid values.
	 */
	@Test
	public void realTest() {
		SingleStaticConstraint sst = new SingleStaticConstraint(
				this.selectionController, this.group, this.staticConstraint,
				this.singleGroup);
		Assert.assertNotNull(sst);
	}
}
