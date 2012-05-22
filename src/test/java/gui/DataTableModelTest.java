package gui;

import static org.junit.Assert.assertEquals;
import gui.settings.Settings;
import gui.views.DataTableModel;
import gui.views.TableView;

import java.io.File;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.DataHub;
import controller.ElementData;
import controller.Group;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Tests the DataTableModel class.
 */
public class DataTableModelTest {

	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_groups_tests";
	private final String dbFile = this.path + "/database-junit-groups.bsv";
	private Database database = null;
	private SelectionController selectionController;
	private SubspaceController subspaceController;
	private GroupController groupController;

	private DataTableModel dtm;
	private DataHub dataHub;
	private TableView tV;

	private final int[] featureIds = { 1, 2 };
	private final float[] values = { 1.1f, 1.2f };

	private ElementData elementData;
	private ElementData[] elementArray;

	private final Group[] groups = new Group[0];

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

			this.elementData = new ElementData(11, featureIds, values, groups);
			this.elementArray = new ElementData[2];
			this.elementArray[0] = this.elementData;
			this.elementData = new ElementData(12, featureIds, values, groups);
			this.elementArray[1] = this.elementData;

			this.groupController = new GroupController(this.database, this.subspaceController);
			this.dataHub = new DataHub(this.database, this.groupController, this.subspaceController);
			this.tV = new TableView(this.dataHub, this.selectionController, this.subspaceController);

			try {
				this.dtm = new DataTableModel(this.subspaceController.getActiveSubspace().getFeatures(),
						this.elementArray, selectionController, tV);
			} catch (DatabaseAccessException e) {
				Assert.fail();
				e.printStackTrace();
			}
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
	 * Tests getColumnName with illegal values.
	 */
	@Test
	public void testIllegalGetColumnName() {
		Assert.assertEquals(null, this.dtm.getColumnName(-1));
		// Assert.assertEquals(null, this.dtm.getColumnName(-100));
		Assert.assertEquals(null, this.dtm.getColumnName(6));
		// Assert.assertEquals(null, this.dtm.getColumnName(666));
	}

	/**
	 * Tests getValueAt with Illegal values.
	 */
	@Test
	public void testIllegalGetValueAt() {
		try {
			this.dtm.getValueAt(-1, 1);
			Assert.fail("Argument was out of bounds");
		} catch (IllegalArgumentException e) {
			// ok
		}
		try {
			this.dtm.getValueAt(0, 6);
			Assert.fail("Argument was out of bounds");
		} catch (IllegalArgumentException e) {
			// ok
		}
		try {
			this.dtm.getValueAt(2, 5);
			Assert.fail("Argument was out of bounds");
		} catch (IllegalArgumentException e) {
			// ok
		}
	}

	/**
	 * Tests the corresponding method - getColumnCount.
	 */
	@Test
	public void testGetColumnCount() {
		Assert.assertEquals(6, this.dtm.getColumnCount());
	}

	/**
	 * Tests the corresponding method - getColumnName.
	 */
	@Test
	public void testGetColumnName() {
		Assert.assertEquals("", this.dtm.getColumnName(0));
		if (Settings.getInstance().getClass().equals(Locale.GERMAN)) {
			Assert.assertEquals("Farbe", this.dtm.getColumnName(1));
		}
		if (Settings.getInstance().getClass().equals(Locale.ENGLISH)) {
			Assert.assertEquals("Color", this.dtm.getColumnName(1));
		}
		Assert.assertEquals("ID", this.dtm.getColumnName(2));
		Assert.assertEquals(Settings.getInstance().getResourceBundle().getString("effectOutlierness"),
				this.dtm.getColumnName(3));
		Assert.assertEquals("Feature 1", this.dtm.getColumnName(4));
		Assert.assertEquals("Feature 2", this.dtm.getColumnName(5));
	}

	/**
	 * Tests the corresponding method - getRowCount.
	 */
	@Test
	public void testGetRowCount() {
		Assert.assertEquals(2, this.dtm.getRowCount());
	}

	/**
	 * Tests the corresponding method - getValueAt.
	 */
	@Test
	public void testGetValueAt() {
		Assert.assertEquals(false, this.dtm.getValueAt(0, 0));
		Assert.assertEquals(false, this.dtm.getValueAt(1, 0));
		Assert.assertEquals(11, this.dtm.getValueAt(0, 2));
		Assert.assertEquals(12, this.dtm.getValueAt(1, 2));
		Assert.assertEquals(1.1f, this.dtm.getValueAt(0, 4));
		Assert.assertEquals(1.2f, this.dtm.getValueAt(1, 5));
	}

	/**
	 * Tests the corresponding method - isCellEditable.
	 */
	@Test
	public void testIsCellEditable() {
		Assert.assertEquals(true, this.dtm.isCellEditable(0, 0));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 1));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 2));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 3));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 4));
		Assert.assertEquals(false, this.dtm.isCellEditable(0, 5));
	}

}
