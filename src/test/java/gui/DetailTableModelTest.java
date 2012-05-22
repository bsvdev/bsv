package gui;

import static org.junit.Assert.assertEquals;
import gui.settings.Settings;
import gui.views.DetailTableModel;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.Feature;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Tests the DetailTableModel class.
 * 
 */
public class DetailTableModelTest {
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private SubspaceController subspaceController;

	private Feature[] features;
	private float[][] floatData;
	private final String[] columnNames = { "min", "max" };

	private DetailTableModel dtm;

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
			this.features = this.subspaceController.getActiveSubspace().getFeatures();
			this.floatData = new float[this.features.length + 1][this.columnNames.length];
			floatData[0][0] = 1f;
			floatData[0][1] = 3f;
			floatData[1][0] = 0.1f;
			floatData[1][1] = 0.5f;
			floatData[2][0] = 77f;
			floatData[2][1] = 99f;
			this.dtm = new DetailTableModel(this.features, this.floatData, this.columnNames, true);

		} catch (InvalidDriverException e) {
			Assert.fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
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
	 * Tests with valid values.
	 */
	@Test
	public void testValid() {
		Assert.assertNotNull(dtm);
	}

	/**
	 * Tests getColumnCount method.
	 */
	@Test
	public void testGetColumnCount() {
		Assert.assertEquals(2, this.dtm.getColumnCount());
	}

	/**
	 * Tests getColumnName method.
	 */
	// @Test
	public void testGetColumnName() {
		Assert.assertEquals("min", this.dtm.getColumnName(0));
		Assert.assertEquals("max", this.dtm.getColumnName(1));
	}

	/**
	 * Tests getRowCount method.
	 */
	@Test
	public void testGetRowCount() {
		Assert.assertEquals(4, this.dtm.getRowCount());
	}

	/**
	 * Tests getValueAt method.
	 */
	@Test
	public void testGetValueAt() {
		Assert.assertEquals(Settings.getInstance().getResourceBundle().getString("effectOutlierness"),
				this.dtm.getValueAt(0, 0));
		Assert.assertEquals(1.0f, this.dtm.getValueAt(0, 1));
		Assert.assertEquals(3f, this.dtm.getValueAt(0, 2));
		Assert.assertEquals("Feature 1", this.dtm.getValueAt(1, 0));
		Assert.assertEquals(0.1f, this.dtm.getValueAt(1, 1));
		Assert.assertEquals(0.5f, this.dtm.getValueAt(1, 2));
		Assert.assertEquals("Feature 2", this.dtm.getValueAt(2, 0));
		Assert.assertEquals(77f, this.dtm.getValueAt(2, 1));
		Assert.assertEquals(99f, this.dtm.getValueAt(2, 2));
	}

	/**
	 * Tests isCellEditable method.
	 */
	@Test
	public void testIsCellEditable() {
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 0));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 1));
		Assert.assertEquals(false, this.dtm.isCellEditable(2, 2));
		Assert.assertEquals(false, this.dtm.isCellEditable(1, 2));
	}
}
