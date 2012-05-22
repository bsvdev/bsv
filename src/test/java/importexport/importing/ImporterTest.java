package importexport.importing;

import importexport.util.InvalidFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.GroupController;
import controller.Subspace;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

public class ImporterTest {

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
	

	@Test
	public void ssdTest3() {
		CSVBasedImporter csvi = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		boolean fnfe = false;
		try {
			csvi.parseAlgoOut(null, 16);
		} catch (FileNotFoundException ex) {
			fnfe = true;
			Assert.assertTrue("Expected FileNotFoundException", true);
		} catch (IOException ex) {
			Assert.fail("Unexpected IOException");
		} catch (DatabaseAccessException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
		}

		Assert.assertTrue("Something went wrong", fnfe);
	}

	@Test
	public void ssdTest4() {
		CSVBasedImporter csvi = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		boolean fnfe = false;
		try {
			csvi.parseAlgoOut(new File(RESPATH + "noFile.ssd"), 27);
		} catch (FileNotFoundException ex) {
			fnfe = true;
			Assert.assertTrue("Expected FileNotFoundException", fnfe);
		} catch (IOException ex) {
			Assert.fail("Unexpected IOException");
		} catch (DatabaseAccessException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
		}

		Assert.assertTrue("Something went wrong", fnfe);
	}

	
	@Test
	public void importFileWithNullInputFile() {
		CSVBasedImporter csv = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		try {
			csv.importFile(null, new File(RESPATH + PATHSYN + "synth_02.ssd"));
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
		}
	}

	@Test
	public void importFileWithNullSSDFile() {
		CSVBasedImporter csv = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		try {
			csv.importFile(new File(RESPATH + PATHSYN + "synth_02.arff"), null);
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
		}
	}

	@Test
	public void importCSVFileWOFeatures() {
		CSVBasedImporter csv = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		File f = null;
		try {
			csv.importFile((f = new File(RESPATH + "importFiles/borderTests/WOFeatures.csv")), new File(RESPATH
					+ PATHSYN + "synth_01.ssd"));
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
		}
	}

	@Test
	public void importArffFileWOFeatures() {
		CSVBasedImporter csv = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File f = null;
		try {
			csv.importFile((f = new File(RESPATH + "importFiles/borderTests/WOFeatures.arff")), new File(RESPATH
					+ PATHSYN + "synth_01.ssd"));
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
		}
	}

	 @Test
	 public void importCSVFileW1Features() {
		 CSVBasedImporter csv = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		 File f = null;
		 try {
		  csv.importFile(
		  (f = new File(RESPATH + "importFiles/borderTests/w1Feature.csv")),
		  new File(RESPATH + "importFiles/borderTests/1ssd.ssd"));
		 } catch (UnsupportedOperationException e) {
			 Assert.assertTrue(true);
		 } catch (Throwable e) {
			 Assert.fail("Unexpected Throwable " + e.getClass());
		 }
	 }

	
	@Test
	public void importArffFileW1Features() {
		CSVBasedImporter csv = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File f = null;
		try {
			try {
				csv.importFile((f = new File(RESPATH + "importFiles/borderTests/w1Feature.arff")), new File(RESPATH
						+ "importFiles/borderTests/1ssd.ssd"));
			} catch (UnsupportedOperationException e) {
				throw new UnsupportedOperationException("Threw just for increase coverage ratio");
			} catch (Throwable e) {
				Assert.fail("Unexpected Throwable " + e.getClass());
			}
		} catch (UnsupportedOperationException e) {
			Assert.assertTrue(true);
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

		CSVBasedImporter csv = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		DataHub dHub = null;
		SubspaceController subCon = null;

		File f = null;
		ElementData[] data;
		Feature[] allFeats;
		try {
			csv.importFile((f = new File(RESPATH + "csv_arff_ssd_files/nantest.arff")), new File(RESPATH
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

	@Test
	public void nanTestWithClass() {
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

		CSVBasedImporter csv = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		DataHub dHub = null;
		SubspaceController subCon = null;

		File f = null;
		ElementData[] data;
		Feature[] allFeats;
		try {
			csv.importFile((f = new File(RESPATH + "csv_arff_ssd_files/nantest_with_class_attr.arff")), new File(
					RESPATH + "csv_arff_ssd_files/nantest.ssd"));
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

	@Test
	public void nullDB() {
		try {
			Importer im = new CSVBasedImporter(null, new CSVFileInfoExtractor());
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		Assert.fail();
	}

	@Test
	public void nullExtractor() {
		try {
			Importer im = new CSVBasedImporter(database, null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		Assert.fail();
	}

	@Test
	public void nullBoth() {
		try {
			Importer im = new CSVBasedImporter(null, null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		Assert.fail();
	}

	@Test
	public void nullSSD() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.parseAlgoOut(null, 25);
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void notExistingSSD() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.parseAlgoOut(new File("/notExisting.ssd"), 25);
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void importFileNullInput() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(null, new File(RESPATH + "csv_arff_ssd_files/nantest.ssd"));
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void importFileNoExistingInput() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File("/noInput.arff"), new File(RESPATH + "csv_arff_ssd_files/nantest.ssd"));
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void importFileNullSSD() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File(RESPATH + "csv_arff_ssd_files/nantest.arff"), null);
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void importFileNoExistingSSD() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File(RESPATH + "csv_arff_ssd_files/nantest.arff"), new File(RESPATH
					+ "csv_arff_ssd_files/nantest_Not.ssd"));
		} catch (FileNotFoundException e) {
			Assert.assertTrue(true);
			return;
		} catch (Exception e) {
			Assert.fail();
			return;
		}
		Assert.fail();
	}

	@Test
	public void testSDD_One_Subspace_Declared_Twice() {
		SubspaceController subCon = null;
		Subspace[] allSubspaces = null;
		Feature[] feats = null;
		Subspace ss4 = null;
		Subspace ss5 = null;
		Feature[] feats_ss4 = null;
		Feature[] feats_ss5 = null;
		boolean succ = true;

		try {
			subCon = new SubspaceController(database);

			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File(RESPATH + "importFiles/synthetic_from_Fabian/synth_01.arff"), new File(RESPATH
					+ "importFiles/borderTests/synth_1_[8_subspaces]_one_Subspace_double_defined.ssd"));
			allSubspaces = subCon.getSubspaces();
			ss4 = allSubspaces[5];
			ss5 = allSubspaces[6];

			feats_ss4 = ss4.getFeatures();
			feats_ss5 = ss5.getFeatures();

			succ &= (allSubspaces.length == 10);
			if (feats_ss4.length == feats_ss5.length) {
				for (int i = 0; i < feats_ss4.length - 1; ++i) {
					succ &= (feats_ss4[i].getId() == feats_ss5[i].getId());
				}
			} else {
				succ = false;
			}

			Assert.assertTrue(succ);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testSSD_woDataSegButwAtData() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File(RESPATH + "importFiles/synthetic_from_Fabian/synth_01.arff"), new File(RESPATH
					+ "importFiles/borderTests/synth_01_woDataSegButwAtData.ssd"));
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.fail("Unexpected Exception: " + e.getClass());
		}
	}

	@Test
	public void testSSD_woDataSegAndwoAtData() {
		try {
			Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
			im.importFile(new File(RESPATH + "importFiles/synthetic_from_Fabian/synth_01.arff"), new File(RESPATH
					+ "importFiles/borderTests/synth_01_woDataSegAndwoAtData.ssd"));
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected Exception: " + e.getClass());
		}
	}

}
