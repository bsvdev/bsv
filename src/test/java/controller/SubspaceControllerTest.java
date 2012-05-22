package controller;

import controller.effectiveoutlierness.Calculation;
import controller.effectiveoutlierness.Min;
import controller.effectiveoutlierness.Average;
import controller.effectiveoutlierness.Max;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * The class is used to test all subspace functionalities, including the class {@link Subspace} and {@link Feature}
 */
public class SubspaceControllerTest {

	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_controller_tests";
	private final String dbFile = this.path + "/database-junit-group.bsv";
	private Database database = null;
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
			String[] features = { "Feature 1", "Feature 2", "Outlierness 1", "Outlierness 2", "Outlierness 3" };
			boolean[] outlier = { false, false, true, true, true };
			this.database.initFeatures(features, outlier);

			// create some subspaces
			int[] ids1 = { 1, 2, 3 };
			this.database.pushSubspace(1, ids1, "Subspace 1");
			int[] ids2 = { 1, 4 };
			this.database.pushSubspace(2, ids2, "Subspace 2");
			int[] ids3 = { 2, 5 };
			this.database.pushSubspace(3, ids3, "Subspace 3");

			// create some objects
			float[][] objects = { { 0.0f, 0.2f, 0.1f, 0.3f, 0.5f }, { 1.0f, 0.8f, 0.9f, 0.7f, 0.5f } };
			this.database.pushObject(objects);
			this.database.updateFeaturesMinMax();

			this.subspaceController = new SubspaceController(this.database);

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
	 * Test the {@link SubspaceController} and its methods
	 */
	@Test
	public void subspaceControllerTest() {

		try {
			// check initial state and get subspace
			assertEquals("Incorrect name for initial subspace", "All Features", this.subspaceController
					.getActiveSubspace().getName());
			assertEquals("Incorrect numbers of features in initial All Features subspace", 6, this.subspaceController
					.getActiveSubspace().getFeatures().length);
			assertEquals("Incorrect number of subspaces", 4, this.subspaceController.getSubspaces().length);
			assertEquals("Incorrect name for all features", "All Features",
					this.subspaceController.getSubspaces()[0].getName());
			assertEquals("Incorrect name for subspace 1", "Subspace 1 - (Feature 1,Feature 2,Outlierness 1)",
					this.subspaceController.getSubspaces()[1].getName());
			assertEquals("Incorrect name for subspace 2", "Subspace 2 - (Feature 1,Outlierness 2)",
					this.subspaceController.getSubspaces()[2].getName());
			assertEquals("Incorrect name for subspace 3", "Subspace 3 - (Feature 2,Outlierness 3)",
					this.subspaceController.getSubspaces()[3].getName());

			// change active subspace
			this.subspaceController.setActiveSubspace(this.subspaceController.getSubspaces()[3]);
			assertEquals("Incorrect name for active subspace", "Subspace 3 - (Feature 2,Outlierness 3)",
					this.subspaceController.getActiveSubspace().getName());
			assertEquals("Incorrect numbers of features in active subspace", 3, this.subspaceController
					.getActiveSubspace().getFeatures().length);

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the constructor and the setter for illegal argument exception
	 */
	@Test
	public void subspaceControllerArgumentTest() {

		try {
			new SubspaceController(null);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			this.subspaceController.setActiveSubspace(null);
			Assert.fail("New active subspace was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			this.subspaceController.setCalculateEffectiveOutliernessBy(null);
			Assert.fail("New effective outlierness calculation was null");
		} catch (IllegalArgumentException e) {
		}

	}

	/**
	 * Test the effective outlierness handling in subspace controller
	 */
	@Test
	public void effectiveOutliernessTest() {

		assertEquals("Incorrect number of effective outlierness calculations", 3,
				this.subspaceController.getAllCalculations().length);
		assertEquals("Incorrect name for initial calculation", "Average", this.subspaceController
				.getCalculateEffectiveOutliernessBy().getName());
		this.subspaceController.setCalculateEffectiveOutliernessBy(this.subspaceController.getAllCalculations()[1]);
		assertEquals("Incorrect name for outlierness calculation", "Max", this.subspaceController
				.getCalculateEffectiveOutliernessBy().getName());
		assertEquals("Incorrect name for outlierness calculation", "Max", this.subspaceController
				.getCalculateEffectiveOutliernessBy().toString());

	}

	/**
	 * Test the class {@link Subspace} and its methods
	 */
	@Test
	public void subspaceTest() {
		Subspace subspace = this.subspaceController.getActiveSubspace();
		try {
			assertEquals("Incorrect id of subspace", 0, subspace.getId());
			assertEquals("Incorrect name of subspace", "All Features", subspace.getName());
			assertEquals("Incorrect name of subspace", "All Features", subspace.toString());
			assertEquals("Incorrect number of features", 6, subspace.getFeatures().length);
			assertTrue("Subspaces are not equal", subspace.equals(this.subspaceController.getActiveSubspace()));
			assertFalse("The subspaces are equal", subspace.equals(this.subspaceController.getSubspaces()[3]));
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the constructor of a subspace for illegal argument exception
	 */
	@Test
	public void subspaceArgumentTest() {
		Integer[] features = { 1, 2 };
		try {
			new Subspace(null, this.database, 1, "Subspace", features);
			Assert.fail("Subspace controller was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Subspace(this.subspaceController, null, 1, "Subspace", features);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Subspace(this.subspaceController, this.database, -1, "Subspace", features);
			Assert.fail("Id was negative");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Subspace(this.subspaceController, this.database, 1, null, features);
			Assert.fail("Name was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Subspace(this.subspaceController, this.database, 1, "Subspace", null);
			Assert.fail("Features was null");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test the equal method of class {@link Subspace}
	 */
	@Test
	public void subspaceEqualTest() {
		Integer[] ids = { 1, 2, 3, 4 };
		Subspace subspace1 = new Subspace(this.subspaceController, this.database, 1, "Subspace 1", ids);
		Subspace subspace2 = new Subspace(this.subspaceController, this.database, 1, "Subspace 1", ids);
		Subspace subspace3 = new Subspace(this.subspaceController, this.database, 2, "Subspace 2", ids);

		assertTrue("Subpsaces are the same", subspace1.equals(subspace1));
		assertTrue("Subspaces are equal", subspace1.equals(subspace2));
		assertFalse("Subspaces are not equal", subspace1.equals(subspace3));
		assertFalse("Object was no subspace", subspace1.equals(new Object()));

	}

	/**
	 * Test the class {@link Feature} and its methods
	 */
	@Test
	public void featureTest() {
		try {
			Feature feature = this.subspaceController.getActiveSubspace().getFeatures()[1];

			assertEquals("Incorrect id", 1, feature.getId());
			assertEquals("Incorrect Name", "Feature 1", feature.getName());
			assertEquals("Incorrect Name", "Feature 1", feature.toString());
			assertFalse("Incorrect outlier flag", feature.isOutlier());
			assertFalse("Incorrect virtual flag,", feature.isVirtual());
			assertEquals("Incorrect max string length", DatabaseConfiguration.VARCHARLENGTH, feature.maxStringLength());
			assertEquals("Incorrect min value for feature 1", 0.0f, feature.getMinValue(), 0.000000001f);
			assertEquals("Incorrect max value for feature 1", 1.0f, feature.getMaxValue(), 0.000000001f);
			assertEquals(feature.getId(), this.subspaceController.getActiveSubspace().getFeatures()[1].getId());

			// test setter
			feature.setName("new Feature 1");
			assertEquals("Incorrect new name", "new Feature 1", feature.getName());

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the class {@link Feature} and its methods
	 */
	@Test
	public void effectiveOutliernessFeatureTest() {
		try {
			// calculate the effective outlierness to check the getMin and getMax methods
			calculateEffectiveOutlierness(this.subspaceController.getCalculateEffectiveOutliernessBy());

			Feature feature = this.subspaceController.getActiveSubspace().getFeatures()[0];

			assertEquals("Incorrect id", -1, feature.getId());
			assertEquals("Incorrect Name", "Effect. Outlierness", feature.getName());
			assertEquals("Incorrect Name", "Effect. Outlierness", feature.toString());
			assertFalse("Incorrect outlier flag", feature.isOutlier());
			assertTrue("Incorrect virtual flag,", feature.isVirtual());
			assertEquals("Incorrect max string length", DatabaseConfiguration.VARCHARLENGTH, feature.maxStringLength());
			assertEquals("Incorrect min value for feature 1", 0.3f, feature.getMinValue(), 0.000001f);
			assertEquals("Incorrect max value for feature 1", 0.7f, feature.getMaxValue(), 0.000001f);
			assertEquals(feature.getId(), this.subspaceController.getActiveSubspace().getFeatures()[0].getId());

			// test setter
			feature.setName("new Name");
			assertEquals("Incorrect new name", "new Name", feature.getName());

		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the constructor and setters in class {@link Feature} for illegal argument exceptions
	 */
	@Test
	public void featureArgumentTest() {

		// test constructor
		try {
			new Feature(null, this.database, 1, "Feature 1", false, false, 0, 1);
			Assert.fail("Subspace controller was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Feature(this.subspaceController, null, 1, "Feature 1", false, false, 0, 1);
			Assert.fail("Database was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Feature(this.subspaceController, this.database, 1, null, false, false, 0, 1);
			Assert.fail("Name was null");
		} catch (IllegalArgumentException e) {
		}
		try {
			new Feature(this.subspaceController, this.database, 1, "aaaaaaaaaaaaaaaaaaaaaaaaa", false, false, 0, 1);
			Assert.fail("Name was to long");
		} catch (IllegalArgumentException e) {
		}

		// test the setters
		Feature feature = null;
		try {
			feature = this.subspaceController.getActiveSubspace().getFeatures()[1];
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			feature.setName(null);
			Assert.fail("Name was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			feature.setName("aaaaaaaaaaaaaaaaaaaaaaaa");
			Assert.fail("Name was to long");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test the equal method of class {@link Feature}
	 */
	@Test
	public void featureEqualTest() {
		Feature feature1 = new Feature(this.subspaceController, this.database, 1, "Feature 1", false, false, 0, 1);
		Feature feature2 = new Feature(this.subspaceController, this.database, 2, "Feature 2", false, false, 0, 1);
		Feature feature3 = new Feature(this.subspaceController, this.database, 1, "Feature 3", false, false, 0, 1);
		Feature feature4 = new Feature(this.subspaceController, this.database, 1, "Feature 1", true, false, 0, 1);

		assertTrue("Objects are the same", feature1.equals(feature1));
		assertFalse("Second feature was null", feature1.equals(null));
		assertFalse("Second feature has other id", feature1.equals(feature2));
		assertFalse("Second feature has other name", feature1.equals(feature3));
		assertFalse("Second feature has other outlier flag", feature1.equals(feature4));
		assertFalse("Second feature was no feature object", feature1.equals(new Object()));
	}

	/**
	 * Calculates the effective outlierness for two test elements, according to the given strategy
	 *
	 * @param effOut
	 *            the strategy to calculate the effective outlierness by
	 * @return the calculated effective outlierness
	 */
	private float[] calculateEffectiveOutlierness(Calculation effOut) {
		// the result vector for both calculations
		float[] result = new float[2];
		Feature[] features = null;
		try {
			features = this.subspaceController.getActiveSubspace().getFeatures();
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// init an element for calculation
		int[] featureIds = new int[features.length - 1];
		for (int i = 1; i < features.length; i++) {
			featureIds[i - 1] = features[i].getId();
		}

		// first element
		float[] values1 = { 0.0f, 0.2f, 0.1f, 0.3f, 0.5f };
		Group[] groups = new Group[0];
		ElementData element1 = new ElementData(1, featureIds, values1, groups);
		// calculate the effective outlierness
		effOut.calculate(features, element1);
		result[0] = element1.getValue(new Feature(this.subspaceController, this.database, -1, "Eff. Out.", false, true,
				0, 1));

		// second element
		float[] values2 = { 1.0f, 0.8f, 0.9f, 0.7f, 0.5f };
		groups = new Group[0];
		ElementData element2 = new ElementData(1, featureIds, values2, groups);
		// calculate the effective outlierness
		effOut.calculate(features, element2);
		result[1] = element2.getValue(new Feature(this.subspaceController, this.database, -1, "Eff. Out.", false, true,
				0, 1));
		return result;
	}

	/**
	 * Test the class {@link EffectiveOutliernessAverage}
	 */
	@Test
	public void effectiveOutliernessAverageTest() {

		Average effOut = new Average();
		assertEquals("Incorrect average calculation", 0.3f, calculateEffectiveOutlierness(effOut)[0], 0.000001f);
		assertEquals("Incorrect average calculation", 0.7f, calculateEffectiveOutlierness(effOut)[1], 0.000001f);

		// test the min / max methods
		assertEquals("Incorrect min outlierness", 0.3f, effOut.getMinValue(), 0.000001f);
		assertEquals("Incorrect min outlierness", 0.7f, effOut.getMaxValue(), 0.000001f);

		assertEquals("Incorrect name", "Average", effOut.getName());
	}

	/**
	 * Test the class {@link EffectiveOutliernessMax}
	 */
	@Test
	public void effectiveOutliernessMaxTest() {

		Max effOut = new Max();
		assertEquals("Incorrect average calculation", 0.5f, calculateEffectiveOutlierness(effOut)[0], 0.000001f);
		assertEquals("Incorrect average calculation", 0.9f, calculateEffectiveOutlierness(effOut)[1], 0.000001f);

		// test the min / max methods
		assertEquals("Incorrect min outlierness", 0.5f, effOut.getMinValue(), 0.000001f);
		assertEquals("Incorrect min outlierness", 0.9f, effOut.getMaxValue(), 0.000001f);

		assertEquals("Incorrect name", "Max", effOut.getName());
	}

	/**
	 * Test the class {@link EffectiveOutliernessMin}
	 */
	@Test
	public void effectiveOutliernessMinTest() {

		Min effOut = new Min();
		assertEquals("Incorrect average calculation", 0.1f, calculateEffectiveOutlierness(effOut)[0], 0.000001f);
		assertEquals("Incorrect average calculation", 0.5f, calculateEffectiveOutlierness(effOut)[1], 0.000001f);

		// test the min / max methods
		assertEquals("Incorrect min outlierness", 0.1f, effOut.getMinValue(), 0.000001f);
		assertEquals("Incorrect min outlierness", 0.5f, effOut.getMaxValue(), 0.000001f);

		assertEquals("Incorrect name", "Min", effOut.getName());
	}
}
