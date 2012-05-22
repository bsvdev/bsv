package importexport;

import importexport.util.InvalidFileException;
import importexport.util.UnsupportedFileExtensionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.GroupController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 *
 */
public class ImportLogicTest {

	private static final String RESPATH = "src/test/resources/";
	private static final String PATHSYN = "importFiles/synthetic_from_Fabian/";
	// temporary file; for now we use the local resource folder to store the sqlite database
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_test";
	private final String dbFile = this.path + "/database-importerTest.bsv";
	private Database database = null;

	/**
	 * Set up a clean database before we do the testing on it.
	 */
	@Before
	public void setup() {
		// create workding dir
		(new File(this.path)).mkdirs();

		// make sure the old file is deleted
		new File(this.dbFile).delete();

		try {
			this.database = new Database(this.dbFile);
		} catch (IllegalArgumentException e) {
			Assert.fail("Database setup failed; path is invalid: " + e.getMessage());
		} catch (InvalidDriverException e) {
			Assert.fail("Database setup failed; SQL driver is invalid: " + e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
		} catch (IncompatibleVersionException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
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
		} catch (DatabaseAccessException ex) {
			Assert.fail("Databus shutdown fail: " + ex.getMessage());
		}

		// clean up database
		if (this.database != null) {
			Assert.assertEquals(true, new File(this.dbFile).delete());
		}
	}

	@BeforeClass
	public static void getLogicWOPreInit() {
		ImportLogic ilog = ImportLogic.getInstance();
		Assert.assertTrue("Instance of the ImportLogic: " + ilog, ilog == null);
	}

	@Test
	public void testInitLogicWithNullDB() {
		try {
			ImportLogic.init(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable");
		}
	}


	@Test
	public void getFormatsTest() {
		ImportLogic.init(database);
		String[] formats = ImportLogic.getInstance().getImportFormats();
		String[] expectedFormats = { "arff", "csv" };
		boolean cond = true;
		for (int i = 0; i < formats.length; ++i) {
			cond &= formats[i].equals(expectedFormats[i]);
		}
		Assert.assertTrue(cond);
	}

	@Test
	public void unsupportedExtension() {
		ImportLogic.init(database);
		try {
			try {
				ImportLogic.getInstance().importFile(new File(RESPATH + PATHSYN + "synth_02.xml"),
						new File(RESPATH + PATHSYN + "synth_02.ssd"));
			} catch (UnsupportedFileExtensionException e) {
				throw new UnsupportedFileExtensionException("Caught and rethrew!");
			} catch (Throwable e) {
				Assert.fail("Unexpected Throwable");
			}
		} catch (UnsupportedFileExtensionException e) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testIllegalFileFormatEnding() {
		ImportLogic.init(database);
		ImportLogic il = ImportLogic.getInstance();
		File input = new File(RESPATH + PATHSYN + "synth_02_shorted.xxx");
		File algoOut = new File(RESPATH + PATHSYN + "synth_02_new.ssd");

		try {
			il.importFile(input, algoOut);
		} catch (UnsupportedFileExtensionException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable: " + t.getClass());
		}
	}

	@Test
	public void testNoFileExtension() {
		ImportLogic.init(database);
		ImportLogic il = ImportLogic.getInstance();
		File input = new File(RESPATH + PATHSYN + "synth_02_shorted");
		File algoOut = new File(RESPATH + PATHSYN + "synth_02_new.ssd");

		try {
			il.importFile(input, algoOut);
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable: " + t.getClass());
		}
	}

	@Test
	public void nanTest() {
		ArrayList<Float[]> expectedData = new ArrayList<Float[]>();
		expectedData.add(new Float[] { 5.91968f, 6.49206f, 0.385896387491947f });
		expectedData.add(new Float[] { 6.33095f, 5.33452f, 0.526838378442537f });
		expectedData.add(new Float[] { 6.35201f, Float.NaN, 0.216665836806271f });
		expectedData.add(new Float[] { 6.51123f, 6.5377f, 0.16653227931975f });
		expectedData.add(new Float[] { Float.NaN, 6.73791f, 0.126494212444904f });
		expectedData.add(new Float[] { 5.4715f, 5.62603f, 0.854828750401612f });
		expectedData.add(new Float[] { 5.95774f, 7.07044f, 0.865829938004325f });
		expectedData.add(new Float[] { 5.28039f, 6.78115f, 0.241111958104565f });
		expectedData.add(new Float[] { Float.NaN, Float.NaN, 0.84632708456882f });
		expectedData.add(new Float[] { 5.89448f, 6.13219f, 0.757529632891082f });

		boolean succ = true;

		ImportLogic.init(database);
		ImportLogic il = ImportLogic.getInstance();
		DataHub dHub = null;
		SubspaceController subCon = null;

		File f = null;
		ElementData[] data;
		Feature[] allFeats;
		try {
			il.importFile((f = new File(RESPATH + "csv_arff_ssd_files/nantest.arff")), new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			subCon = new SubspaceController(database);
			dHub = new DataHub(database, new GroupController(database, subCon), subCon);

			data = dHub.getData();
			allFeats = subCon.getSubspaces()[0].getFeatures();

			for (int i = 0; i < data.length; ++i) {
				for (int j = 1; j < allFeats.length; ++j) {
					succ &= expectedData.get(i)[j - 1].equals(data[i].getValue(allFeats[j]));
				}
			}

		} catch (Exception e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
			return;
		}
		Assert.assertTrue(succ);
	}
}
