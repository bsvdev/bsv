package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.Operator;
import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * The class is used to test all group functionalities, including the {@link Group}, {@link StaticConstraint} and
 * {@link DynamicConstraint} classes.
 */
public class GroupControllerTest {

	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_controller_tests";
	private final String dbFile = this.path + "/database-junit-group.bsv";

	private Database database = null;
	private GroupController groupController;
	private SubspaceController subspaceController;

	/**
	 * Set up a clean database before we do the testing on it and init the group controller
	 */
	@Before
	public void setup() {
		// create working directory
		(new File(this.path)).mkdirs();

		// make sure the old file is deleted
		(new File(this.dbFile)).delete();

		try {
			// create a new database with two features
			this.database = new Database(this.dbFile);
			String[] features = { "Feature 1", "Feature 2" };
			boolean[] outlier = { false, false };
			this.database.initFeatures(features, outlier);

			this.subspaceController = new SubspaceController(this.database);
			this.groupController = new GroupController(this.database, this.subspaceController);

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
	 * This method tests the given group
	 * 
	 * @param group
	 *            the group to test
	 * @param id
	 *            the expected id
	 * @param name
	 *            the expected name
	 * @param ColorFeature
	 *            the expected color feature
	 */
	private void groupTest(Group group, int id, String name, Feature colorFeature) {
		assertEquals("Inappropriate id", id, group.getId());
		assertEquals("Inappropriate name", name, group.getName());
		assertEquals("Inappropriate id", "", group.getDescription());
		assertEquals("Inappropriate color feature", colorFeature, group.getColorFeature());
		assertTrue("Inappropriate visibility", group.isVisible());
	}

	/**
	 * Test the group controller and its methods
	 */
	@Test
	public void groupControllerTest() {
		// Test constructor
		try {
			new GroupController(null, this.subspaceController);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			new GroupController(this.database, null);
			Assert.fail("subspace controller was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// Test create methods
		try {
			this.groupController.createGroup(null);
			Assert.fail("name was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			this.groupController.createGroup("aaaaaaaaaaaaaaaaaaaaaaaaa");
			Assert.fail("name was to long");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		assertEquals("Incorrect max length for String", groupController.maxStringLength(),
				DatabaseConfiguration.VARCHARLENGTH);
	}

	/**
	 * Test if there is no group returned from a new database, create one and test its parameters
	 */
	@Test
	public void initialStateAndCreateGroupTest() {

		try {
			// test if there is no group in database and create one initial
			assertEquals("There was a initial group in database", 0, this.groupController.getGroups().length);
			Group testingGroup = this.groupController.createGroup("testing group 1");
			assertEquals("The group was not created", 1, this.groupController.getGroups().length);

			// test parameter of initial group
			groupTest(testingGroup, 1, "testing group 1", null);

			// create another group
			this.groupController.createGroup("testing group 2");
			assertEquals("The second group was not created", 2, groupController.getGroups().length);

			// test parameter of groups after getting them from database
			testingGroup = this.groupController.getGroups()[0];
			groupTest(testingGroup, 1, "testing group 1", null);

			testingGroup = this.groupController.getGroups()[1];
			groupTest(testingGroup, 2, "testing group 2", null);

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test creation and removing of groups
	 */
	@Test
	public void createAndRemoveGroupTest() {

		try {
			// check state of group count and create two new ones
			assertEquals("False number of groups", 0, this.groupController.getGroups().length);

			this.groupController.createGroup("testing group 1");
			this.groupController.createGroup("testing group 2");

			groupTest(this.groupController.getGroups()[0], 1, "testing group 1", null);
			groupTest(this.groupController.getGroups()[1], 2, "testing group 2", null);

			// remove the first one and test again
			Group testingGroup = this.groupController.getGroups()[0];
			this.groupController.removeGroup(testingGroup);
			assertEquals("False number of groups", 1, this.groupController.getGroups().length);

			// check the remaining group
			groupTest(this.groupController.getGroups()[0], 2, "testing group 2", null);

			// create new groups and test the parameters
			this.groupController.createGroup("testing group 3");
			this.groupController.createGroup("testing group 4");
			groupTest(this.groupController.getGroups()[0], 2, "testing group 2", null);
			groupTest(this.groupController.getGroups()[1], 3, "testing group 3", null);
			groupTest(this.groupController.getGroups()[2], 4, "testing group 4", null);

			assertEquals("False number of groups", 3, this.groupController.getGroups().length);

			// remove another group and test the parameters again
			this.groupController.removeGroup(new Group(this.groupController, this.database, 3, "testing group 3", true,
					-1, null, ""));

			assertEquals("False number of groups", 2, this.groupController.getGroups().length);
			groupTest(this.groupController.getGroups()[0], 2, "testing group 2", null);
			groupTest(this.groupController.getGroups()[1], 4, "testing group 4", null);

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

	}

	/**
	 * Test a single group and modify single parameters
	 */
	@Test
	public void singleGroupTest() {
		try {
			Group group = this.groupController.createGroup("testing group");
			groupTest(group, 1, "testing group", null);

			assertEquals("Incorrect to string", "testing group", group.toString());
			assertEquals("Incorrect max string length", group.maxStringLength(), DatabaseConfiguration.VARCHARLENGTH);

			group.setName("new Name");
			assertEquals("Incorrect name", "new Name", group.getName());
			group.setColor(-1);
			assertEquals("Incorrect color", -1, group.getColor());
			group.setDescription("Testing the description");
			assertEquals("Incorrect description", "Testing the description", group.getDescription());
			group.setVisible(false);
			assertFalse("Incorrect visibility", group.isVisible());
			group.setColorFeature(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"Feature 1", false, false, 0, 1));
			assertEquals("Incorrect color feature", 1, group.getColorFeature().getId());
			group.setColorFeature(new Feature(this.groupController.getSubspaceController(), this.database, -1,
					"Effect.", false, true, 0, 1));
			assertEquals("Incorrect color feature", -1, group.getColorFeature().getId());
			group.setColorFeature(null);
			assertEquals("Incorrect color feature", group.getColorFeature(), null);

			// test if all changes are done in database
			group = this.groupController.getGroups()[0];
			assertEquals("Incorrect name in database", "new Name", group.getName());
			assertEquals("Incorrect color in database", -1, group.getColor());
			assertEquals("Incorrect description in database", "Testing the description", group.getDescription());
			assertFalse("Incorrect visibility in database", group.isVisible());
			// assertEquals("Incorrect color feature in database", -1, group.getColorFeature().getId());

			// check remove single group
			assertEquals("Incorrect number of groups", 1, this.groupController.getGroups().length);
			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"test", false, false, 0, 1), Operator.EQUAL, 0.0f);
			int[] selection = { 1, 2 };
			group.createStaticConstraint(selection);
			group.remove();
			assertEquals("Incorrect number of groups", 0, this.groupController.getGroups().length);

			// Test color features
			Group groupNull = this.groupController.createGroup("group null");
			groupNull.setColorFeature(null);
			Group groupFeature = this.groupController.createGroup("group Feature");
			groupFeature.setColorFeature(new Feature(this.subspaceController, this.database, 1, "Feature 1", false,
					false, 0, 1));
			Group groupEffectOut = this.groupController.createGroup("group outlierness");
			groupEffectOut.setColorFeature(new Feature(this.subspaceController, this.database, -1, "Effect. Out.",
					false, true, 0, 1));
			assertEquals("Incorrect number of groups", 3, this.groupController.getGroups().length);

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the creation and removing of static and dynamic Constraints
	 */
	@Test
	public void singleGroupConstraintTest() {
		try {
			Group group = this.groupController.createGroup("testing group");

			assertEquals("Incorrect number of constraints", 0, group.getConstraints().length);

			// test dynamic constraint
			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f);
			assertEquals("Incorrect number of constraints", 1, group.getConstraints().length);
			group.removeConstraint(group.getConstraints()[0]);
			assertEquals("Incorrect number of constraints", 0, group.getConstraints().length);

			// test static constraint
			int[] selection = { 1, 2, 3, 4, 5, 6 };
			group.createStaticConstraint(selection);
			assertEquals("Incorrect number of constraints", 1, group.getConstraints().length);
			group.removeConstraint(group.getConstraints()[0]);
			assertEquals("Incorrect number of constraints", 0, group.getConstraints().length);

			// test the rebuilding of constraints from database
			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f);
			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"Outlierness 1", true, false, 0, 1), Operator.EQUAL, 0.0f);
			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, -1,
					"Effect. Out.", false, true, 0, 1), Operator.EQUAL, 0.0f);
			group.createStaticConstraint(selection);
			group = this.groupController.getGroups()[0];
			assertEquals("Incorrect number of constraints", 4, group.getConstraints().length);

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test a single dynamic constraint and set, get parameters
	 */
	@Test
	public void singleDynamicConstraintTest() {
		try {
			Group group = this.groupController.createGroup("testing group");

			group.createDynamicConstraint(new Feature(this.groupController.getSubspaceController(), this.database, 1,
					"Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f);
			assertEquals("Incorrect number of constraints", group.getConstraints().length, 1);
			DynamicConstraint constraint = (DynamicConstraint) group.getConstraints()[0];

			// test the dynamic constraint
			assertEquals("Incorrect id", constraint.getId(), 1);
			assertEquals("Incorrect feature", constraint.getFeature().getId(), 1);
			assertEquals("Incorrect operator", constraint.getOperator(), Operator.EQUAL);
			assertEquals("Incorrect value", constraint.getValue(), 0.0f, 0.000000001f);
			assertTrue("Incorrect active flag", constraint.isActive());

			// set all values and check their changes
			constraint.setFeature(new Feature(this.groupController.getSubspaceController(), this.database, 2,
					"Feature 2", false, false, 0, 0));
			assertEquals("Incorrect feature", constraint.getFeature().getId(), 2);
			constraint.setOperator(Operator.GREATER);
			assertEquals("Incorrect operator", constraint.getOperator(), Operator.GREATER);
			constraint.setValue(1.0f);
			assertEquals("Incorrect value", constraint.getValue(), 1.0f, 0.000000001f);
			constraint.setActive(false);
			assertFalse("Incorrect active flag", constraint.isActive());
			constraint.remove();

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test a single static constraint and its set and get methods
	 */
	@Test
	public void singleStaticConstraintTest() {
		try {
			Group group = this.groupController.createGroup("testing group");

			int[] selection = { 1, 2, 3, 4 };
			group.createStaticConstraint(selection);
			assertEquals("Incorrect number of constraints", group.getConstraints().length, 1);
			StaticConstraint constraint = (StaticConstraint) group.getConstraints()[0];

			// testing the static constraint
			assertEquals("Incorrect id", constraint.getId(), 1);
			assertEquals("Incorrect number of items in selection", constraint.getSelection().length, selection.length);
			assertEquals("Incorrect id in selection", constraint.getSelection()[0], selection[0]);
			assertEquals("Incorrect id in selection", constraint.getSelection()[1], selection[1]);
			assertEquals("Incorrect id in selection", constraint.getSelection()[2], selection[2]);
			assertEquals("Incorrect id in selection", constraint.getSelection()[3], selection[3]);
			assertTrue("Incorrect is active flag", constraint.isActive());

			// set valus and test them
			constraint.setActive(false);
			assertFalse("Incorrect is active flag", constraint.isActive());
			int[] newSelection = { 5, 6 };
			constraint.setSelection(newSelection);
			assertEquals("Incorrect number of items in the selection", constraint.getSelection().length,
					newSelection.length);
			assertEquals("Incorrect id in selection", constraint.getSelection()[0], newSelection[0]);
			assertEquals("Incorrect id in selection", constraint.getSelection()[1], newSelection[1]);
			constraint.remove();

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests all constructors and setters in class {@link Group } for illegal argument exceptions
	 */
	@Test
	public void groupArgumentTest() {

		// Test constructor
		try {
			new Group(null, this.database, 1, "test", true, 0, null, "");
			Assert.fail("Group controller was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Group(this.groupController, null, 1, "test", true, 0, null, "");
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Group(this.groupController, this.database, -1, "test", true, 0, null, "");
			Assert.fail("id was negative");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Group(this.groupController, this.database, 1, null, true, 0, null, "");
			Assert.fail("Name was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Group(this.groupController, this.database, 1, "aaaaaaaaaaaaaaaaaaaaaaaaaa", true, 0, null, "");
			Assert.fail("Name was to long");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Group(this.groupController, this.database, 1, "test", true, 0, null, null);
			Assert.fail("Description was null");
		} catch (IllegalArgumentException e) {
		}

		// Test setters
		Group group = new Group(this.groupController, this.database, 1, "testing group", true, 0, null, "");
		try {
			group.setDescription(null);
			Assert.fail("Description was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			group.setName(null);
			Assert.fail("Name was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			group.setName("aaaaaaaaaaaaaaaaaaaaaaaaa");
			Assert.fail("Name was to long");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// Test create methods
		try {
			group.createDynamicConstraint(null, Operator.EQUAL, 0.0f);
			Assert.fail("Feature was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			group.createDynamicConstraint(new Feature(this.subspaceController, this.database, 1, "Feature 1", false,
					false, 0, 1), null, 0.0f);
			Assert.fail("Operator was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			group.createStaticConstraint(null);
			Assert.fail("Selection was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			int[] selection = new int[0];
			group.createStaticConstraint(selection);
			Assert.fail("Selection has length zero");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

	}

	/**
	 * Tests all constructors and setters in class {@link DynamicConstraint} for illegal argument exceptions
	 */
	@Test
	public void dynamicConstraintArgumentTest() {

		// Test constructor
		try {
			new DynamicConstraint(null, this.database, 1, new Feature(this.groupController.getSubspaceController(),
					this.database, 1, "Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f, true);
			Assert.fail("Group controller was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new DynamicConstraint(this.groupController, null, 1, new Feature(this.groupController
					.getSubspaceController(), this.database, 1, "Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f,
					true);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new DynamicConstraint(this.groupController, this.database, -1, new Feature(this.groupController
					.getSubspaceController(), this.database, 1, "Feature 1", false, false, 0, 1), Operator.EQUAL, 0.0f,
					true);
			Assert.fail("Id was negative");
		} catch (IllegalArgumentException e) {
		}
		try {
			new DynamicConstraint(this.groupController, this.database, 1, null, Operator.EQUAL, 0.0f, true);
			Assert.fail("Feature was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new DynamicConstraint(this.groupController, this.database, 1, new Feature(this.groupController
					.getSubspaceController(), this.database, 1, "Feature 1", false, false, 0, 1), null, 0.0f, true);
			Assert.fail("Operator was null");
		} catch (IllegalArgumentException e) {
		}

		// Test setters
		DynamicConstraint constraint = new DynamicConstraint(this.groupController, this.database, 1, new Feature(
				this.groupController.getSubspaceController(), this.database, 1, "Feature 1", false, false, 0, 1),
				Operator.EQUAL, 0.0f, true);
		try {
			constraint.setFeature(null);
			Assert.fail("Feature was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			constraint.setOperator(null);
			Assert.fail("Operator was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests all constructors and setters in class {@link StaticConstraint} for illegal argument exceptions
	 */
	@Test
	public void staticConstraintArgumentTest() {
		int[] selection = { 1, 2, 3 };
		// Test constructor
		try {
			new StaticConstraint(null, this.database, 1, 1, selection, true);
			Assert.fail("Group controller was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new StaticConstraint(this.groupController, null, 1, 1, selection, true);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new StaticConstraint(this.groupController, this.database, -1, 1, selection, true);
			Assert.fail("Id was negative");
		} catch (IllegalArgumentException e) {
		}
		try {
			new StaticConstraint(this.groupController, this.database, 1, -1, selection, true);
			Assert.fail("Group id was nnegative");
		} catch (IllegalArgumentException e) {
		}
		try {
			new StaticConstraint(this.groupController, this.database, 1, 1, null, true);
			Assert.fail("Selection was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			int[] shortSelection = new int[0];
			new StaticConstraint(this.groupController, this.database, 1, 1, shortSelection, true);
			Assert.fail("Selection has length zero");
		} catch (IllegalArgumentException e) {
		}

		// Test setters
		StaticConstraint constraint = new StaticConstraint(this.groupController, this.database, 1, 1, selection, true);
		try {
			constraint.setSelection(null);
			Assert.fail("Selection was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			int[] shortSelection = new int[0];
			constraint.setSelection(shortSelection);
			Assert.fail("Selection has length zero");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}
}
