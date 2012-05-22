package gui;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.Group;
import controller.GroupController;
import controller.SubspaceController;

import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

import gui.views.ViewUtils;

import util.Operator;

import java.awt.Color;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for ViewUtils class
 */
public class ViewUtilsTest {
	/**
	 * Stores path to db file
	 */
	private String dbPath;

	/**
	 * prepared db
	 */
	private Database db;

	/**
	 * Subspace Controller
	 */
	private SubspaceController subspaceController;

	/**
	 * Group Controller
	 */
	private GroupController groupController;

	/**
	 * DataHub
	 */
	private DataHub dataHub;

	/**
	 * Simulate program start
	 */
	@Before
	public void initDatasource() {
		this.dbPath = System.getProperty("java.io.tmpdir") + "/bsv_test/database-junit-viewutils.bsv";

		try {
			this.db = new Database(this.dbPath);

			this.db.initFeatures(new String[]{"featureA", "featureB", "featureC", "featureD"},
					new boolean[]{false, false, false, false});
			this.db.pushObject(new float[][]{
				{1.f, 0.f, 2.f, 10.f},
				{-1.f, 5.f, -1.8f, 15.f},
				{1.5f, 7.f, 3.2f, 17.f}
			});
			this.db.updateFeaturesMinMax();

			this.subspaceController = new SubspaceController(this.db);
			this.groupController = new GroupController(this.db, this.subspaceController);
			this.dataHub = new DataHub(this.db, this.groupController, this.subspaceController);
		} catch (InvalidDriverException ex) {
			fail("unexpected InvalidDriverException");
		} catch (IncompatibleVersionException ex) {
			fail("unexpected IncompatibleVersionException");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Simulate program shutdown and do cleanup
	 */
	@After
	public void destroyDatasource() {
		try {
			this.dataHub = null;
			this.groupController = null;
			this.subspaceController = null;
			this.db.shutdown();
			this.db = null;
			assertEquals("Cannot remove database file", true, (new File(this.dbPath)).delete());
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test calcColor when no groups are avaible
	 */
	@Test
	public void testCalcColorWhithoutGroups() {
		try {
				// get data
				ElementData[] ed = this.dataHub.getData();
				assertEquals("Wrong result size of getData", 3, ed.length);

				// test colors
				for (ElementData data : ed) {
					Color c = ViewUtils.calcColor(data);

					assertEquals("Element should have full alpha", 255, c.getAlpha());
					assertEquals("Element should have no red value", 0, c.getRed());
					assertEquals("Element should have no green value", 0, c.getGreen());
					assertEquals("Element should have no blue value", 0, c.getBlue());
				}
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test calcColor then groups are avaible
	 */
	@Test
	public void testCalcColorWithGroups() {
		try {
			// search color feature
			Feature[] features = this.subspaceController.getActiveSubspace().getFeatures();
			Feature cFeature = null;
			for (Feature f : features) {
				if (f.getName().equals("featureA")) {
					cFeature = f;
				}
			}
			if (cFeature == null) {
				fail("color feature not found");
			}

			// create group 1
			Group group1 = this.groupController.createGroup("group1");
			group1.createStaticConstraint(new int[]{1});
			group1.setColor(0xffff00aa);
			group1.setColorFeature(cFeature);

			// create group 2
			Group group2 = this.groupController.createGroup("group2");
			group2.createStaticConstraint(new int[]{1, 2});
			group2.setColor(0xff00ffbb);

			// get data
			ElementData[] ed = this.dataHub.getData();
			assertEquals("Wrong result size of getData", 2, ed.length);

			// test colors
			for (ElementData data : ed) {
				if (data.getId() == 1) {
					Color c = ViewUtils.calcColor(data);

					assertEquals("Wrong alpha value", 230, c.getAlpha());
					assertEquals("Wrong red value", 113, c.getRed());
					assertEquals("Wrong green value", 142, c.getGreen());
					assertEquals("Wrong blue value", 179, c.getBlue());
				} else if (data.getId() == 2) {
					Color c = ViewUtils.calcColor(data);

					assertEquals("Wrong alpha value", 255, c.getAlpha());
					assertEquals("Wrong red value", 0, c.getRed());
					assertEquals("Wrong green value", 255, c.getGreen());
					assertEquals("Wrong blue value", 187, c.getBlue());
				} else {
					fail("illegal element id found: " + data.getId());
				}
			}
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test combineComponent
	 */
	@Test
	public void testCombineComponent() {
		assertEquals("Wrong combined result", 0.f,
				ViewUtils.combineComponent(new float[]{}, new float[]{}, 0),
				0.00001f);

		assertEquals("Wrong combined result", 0.4f,
				ViewUtils.combineComponent(new float[]{0.f, 1.f, 0.2f}, new float[]{0.f, 0.f, 0.f}, 3),
				0.00001f);

		assertEquals("Wrong combined result", 0.3f,
				ViewUtils.combineComponent(new float[]{0.f, 1.f, 0.2f}, new float[]{1.f, 0.5f, 0.5f}, 3),
				0.00001f);
	}

	/**
	 * Test combineAlpha
	 */
	@Test
	public void testCombineAlpha() {
		assertEquals("Wrong combined result", 0.f,
				ViewUtils.combineAlpha(new float[]{}, 0),
				0.00001f);

		assertEquals("Wrong combined result", 0.6f,
				ViewUtils.combineAlpha(new float[]{0.f, 1.f, 0.8f}, 3),
				0.00001f);
	}

	/**
	 * Test autoSort with parameter {@code null}
	 */
	@Test
	public void testAutoSortWithNull() {
		try {
			assertEquals("null expected", null, (Object) ViewUtils.autoSort(null, this.dataHub));
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test autoSort with only one feature
	 */
	@Test
	public void testAutoSortWithOneFeature() {
		try {
			// prepare features
			Feature[] features = new Feature[1];
			for (Feature f : this.subspaceController.getActiveSubspace().getFeatures()) {
				if (f.getName().equals("featureA")) {
					features[0] = f;
				}
				// ignore all other featres
			}

			// do autosort
			features = ViewUtils.autoSort(features, this.dataHub);

			// check result
			assertEquals("wrong result array length", features.length, 1);
			assertEquals("wrong feature at position 0", features[0].getName(), "featureA");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test autosort with only two features
	 */
	@Test
	public void testAutoSortWithTwoFeatures() {
		try {
			// prepare features
			Feature[] features = new Feature[2];
			for (Feature f : this.subspaceController.getActiveSubspace().getFeatures()) {
				if (f.getName().equals("featureA")) {
					features[1] = f;
				} else if (f.getName().equals("featureB")) {
					features[0] = f;
				}
				// ignore all other featres
			}

			// do autosort
			features = ViewUtils.autoSort(features, this.dataHub);

			// check result
			assertEquals("wrong result array length", features.length, 2);
			assertEquals("wrong feature at position 0", features[0].getName(), "featureB");
			assertEquals("wrong feature at position 1", features[1].getName(), "featureA");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test autoSort algorithm using correct input
	 */
	@Test
	public void testAutoSortNormal() {
		try {
			// prepare features
			Feature[] features = new Feature[4];
			for (Feature f : this.subspaceController.getActiveSubspace().getFeatures()) {
				if (f.getName().equals("featureA")) {
					features[2] = f;
				} else if (f.getName().equals("featureB")) {
					features[0] = f;
				} else if (f.getName().equals("featureC")) {
					features[1] = f;
				} else if (f.getName().equals("featureD")) {
					features[3] = f;
				}
				// ignore all other featres (e.g. effective outlierness)
			}

			// do autosort
			features = ViewUtils.autoSort(features, this.dataHub);

			// check result
			assertEquals("wrong result array length", features.length, 4);
			assertEquals("wrong feature at position 0", features[0].getName(), "featureB");
			assertEquals("wrong feature at position 1", features[1].getName(), "featureD");
			assertEquals("wrong feature at position 2", features[2].getName(), "featureA");
			assertEquals("wrong feature at position 3", features[3].getName(), "featureC");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test autoSort when no elements are aviable
	 */
	@Test
	public void testAutoSortWithZeroElements() {
		try {
			// prepare features
			Feature[] features = new Feature[3];
			for (Feature f : this.subspaceController.getActiveSubspace().getFeatures()) {
				if (f.getName().equals("featureA")) {
					features[2] = f;
				} else if (f.getName().equals("featureB")) {
					features[0] = f;
				} else if (f.getName().equals("featureC")) {
					features[1] = f;
				}
				// ignore all other featres (e.g. effective outlierness)
			}

			// setup groups
			Group g = this.groupController.createGroup("group");
			g.createStaticConstraint(new int[]{1});
			g.createDynamicConstraint(features[2], Operator.EQUAL, 5.f);

			// do autosort
			features = ViewUtils.autoSort(features, this.dataHub);

			// check result
			assertEquals("wrong result array length", features.length, 3);
			assertEquals("wrong feature at position 0", features[0].getName(), "featureB");
			assertEquals("wrong feature at position 1", features[1].getName(), "featureC");
			assertEquals("wrong feature at position 2", features[2].getName(), "featureA");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test autoSort with only one element
	 */
	@Test
	public void testAutoSortWithOneElement() {
		try {
			// prepare features
			Feature[] features = new Feature[3];
			for (Feature f : this.subspaceController.getActiveSubspace().getFeatures()) {
				if (f.getName().equals("featureA")) {
					features[2] = f;
				} else if (f.getName().equals("featureB")) {
					features[0] = f;
				} else if (f.getName().equals("featureC")) {
					features[1] = f;
				}
				// ignore all other featres (e.g. effective outlierness)
			}

			// setup groups
			Group g = this.groupController.createGroup("group");
			g.createStaticConstraint(new int[]{1});

			// do autosort
			features = ViewUtils.autoSort(features, this.dataHub);

			// check result
			assertEquals("wrong result array length", features.length, 3);
			assertEquals("wrong feature at position 0", features[0].getName(), "featureB");
			assertEquals("wrong feature at position 1", features[1].getName(), "featureC");
			assertEquals("wrong feature at position 2", features[2].getName(), "featureA");
		} catch (DatabaseAccessException ex) {
			fail("unexpected DatabaseAccessException");
		}
	}

	/**
	 * Test niceNum
	 */
	@Test
	public void testNiceNum() {
		assertEquals("Wrong nice number", 10.f, ViewUtils.niceNum(12.f, true), 0.001f);
		assertEquals("Wrong nice number", 2.f, ViewUtils.niceNum(1.5f, true), 0.001f);
		assertEquals("Wrong nice number", 0.05f, ViewUtils.niceNum(0.03f, true), 0.001f);
		assertEquals("Wrong nice number", 100.f, ViewUtils.niceNum(80.f, true), 0.001f);

		assertEquals("Wrong nice number", 1.f, ViewUtils.niceNum(-Float.MIN_NORMAL, false), 0.001f);
		assertEquals("Wrong nice number", 2.f, ViewUtils.niceNum(1.5f, false), 0.001f);
		assertEquals("Wrong nice number", 0.05f, ViewUtils.niceNum(0.02f, false), 0.001f);
		assertEquals("Wrong nice number", 100.f, ViewUtils.niceNum(80.f, false), 0.001f);
	}

	/**
	 * Test calcAxisMarkers
	 */
	@Test
	public void testCalcAxisMarkers() {
		assertArrayEquals("Wrong axis markers", new float[]{
					0.f, // nfrac
					-10.5f,
					-10.f,
					-5.f,
					0.f,
					5.f,
					10.f,
					15.f,
					20.f,
					20.75f
				},
				ViewUtils.calcAxisMarkers(-10.5f, 20.75f, 500, 50),
				0.00001f);

		assertArrayEquals("Wrong axis markers", new float[]{
					1.f, // nfrac
					0.3f,
					0.2f,
					0.1f
				},
				ViewUtils.calcAxisMarkers(0.3f, 0.1f, 300, 100),
				0.00001f);
	}
}
