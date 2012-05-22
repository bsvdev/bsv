package importexport.exporting;

import importexport.importing.ArffFileInfoExtractor;
import importexport.importing.CSVBasedImporter;
import importexport.importing.Importer;
import importexport.util.CSVFileInfo;
import importexport.util.InvalidFileException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;
import controller.DataHub;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

public class ExporterTest {
	private static final String RESPATH = "src/test/resources/";
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_test";
	private final String dbFile = this.path + "/database-exporterTest.bsv";
	private Database database = null;
	private SubspaceController subCon = null;
	private DataHub daHub = null;
	private GroupController grCon = null;
	private SelectionController selCon = null;
	private File arff = new File(this.path + "/exportTest.arff");
	private File csv = new File(this.path + "/exportTest.csv");
	private File ssd = new File(this.path + "/exportTest.ssd");


	/**
	 * Set up a clean database before we do the testing on it.
	 */
	@Before
	public void setup() {
		// create workding dir
		(new File(this.path)).mkdirs();

		// make sure the old file is deleted
		new File(this.dbFile).delete();
		arff.delete();
		csv.delete();
		ssd.delete();

		try {
			this.database = new Database(this.dbFile);
			this.selCon = new SelectionController();
			this.subCon = new SubspaceController(database);
			this.grCon = new GroupController(database, subCon);
			this.daHub = new DataHub(database, grCon, subCon);
			arff.createNewFile();
			csv.createNewFile();
			ssd.createNewFile();
		} catch (IllegalArgumentException e) {
			Assert.fail("Database setup failed; path is invalid: " + e.getMessage());
		} catch (InvalidDriverException e) {
			Assert.fail("Database setup failed; SQL driver is invalid: " + e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
		} catch (IncompatibleVersionException e) {
			Assert.fail("Database setup failed; connection could not be created: " + e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getClass().toString());
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
		if (arff.exists())
			arff.delete();
		if (csv.exists())
			csv.delete();
		if (ssd.exists())
			ssd.delete();
	}

	@Test
	public void testNullArguments1_0() {
		try {
			Exporter ex = new Exporter(daHub, null, null, null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments1_1() {
		try {
			Exporter ex = new Exporter(null, this.selCon, null, null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments1_2() {
		try {
			Exporter ex = new Exporter(null, null, this.subCon,null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments1_3() {
		try {
			Exporter ex = new Exporter(null, null, null, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments2_0() {
		try {
			Exporter ex = new Exporter(daHub, selCon, null, null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments2_1() {
		try {
			Exporter ex = new Exporter(daHub, null, this.subCon, null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments2_2() {
		try {
			Exporter ex = new Exporter(daHub, null, null, new ArffFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments2_3() {
		try {
			Exporter ex = new Exporter(null, this.selCon, null, new ArffFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments2_4() {
		try {
			Exporter ex = new Exporter(null, null, this.subCon, new ArffFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments3_0() {
		try {
			Exporter ex = new Exporter(daHub, selCon, subCon, null) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments3_1() {
		try {
			Exporter ex = new Exporter(daHub, selCon, null, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments3_2() {
		try {
			Exporter ex = new Exporter(daHub, null, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments3_3() {
		try {
			Exporter ex = new Exporter(null, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
			Assert.fail("Exporter can't instantiated with null arguments!");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testNullArguments4() {
		try {
			Exporter ex = new Exporter(daHub, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testGetDataHub() {
		Exporter ex = new Exporter(daHub, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
		Assert.assertTrue(ex.getDatahub() != null);
	}

	@Test
	public void testGetSelectionController() {
		Exporter ex = new Exporter(daHub, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
		Assert.assertTrue(ex.getSelectionController() != null);
	}

	@Test
	public void testGetSubspaceController() {
		Exporter ex = new Exporter(daHub, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
		Assert.assertTrue(ex.getSubspaceController() != null);
	}

	@Test
	public void getInjector() {
		Exporter ex = new Exporter(daHub, selCon, subCon, new CSVFileInfoInjector()) {
				@Override
				public void exportFile(final File f, final boolean woOutlierness)
						throws IOException,
						InvalidFileException,
						DatabaseAccessException {

				}
			};
		Assert.assertTrue(ex.getInjector() != null);
	}

	@Test
	public void testExportFileWithNullArguments() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new ArffFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportFile(null, true);
			Assert.fail("Export with a null File shouldn't be possible.");
		} catch (InvalidFileException e) {
			Assert.assertTrue(true);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void arffExportTest1() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new ArffFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		CSVFileInfo info = null;
		CSVFileInfo expected = new CSVFileInfo(
				"exportTest.arff",
				new String[] { "var_0", "var_1", "class" },
				8,
				(char)0);
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportFile(arff, true);
			info = new ArffFileInfoExtractor().extractFileInfo(arff);


		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}

		Assert.assertTrue("Wrong relation", expected.getName().equals(info.getName()));
		Assert.assertTrue("Wrong delimiter", expected.getDelimiter() == info.getDelimiter());
		Assert.assertTrue("Wrong flods " + info.getFirstLineOfDataSegment(), expected.getFirstLineOfDataSegment() == info.getFirstLineOfDataSegment());
		Assert.assertTrue("Wrong features", Arrays.deepEquals(expected.getFeatures(), info.getFeatures()));
	}


	@Test
	public void testArffExportNoSSD() {
		ssd.delete();
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new ArffFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportFile(arff, false);

			Assert.assertTrue(!ssd.exists());

		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testCSVExportNoSSD() {
		ssd.delete();
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new CSVFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportFile(csv, false);

			Assert.assertTrue(!ssd.exists());

		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void csvExportTest1() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new ArffFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		CSVFileInfo info = null;
		CSVFileInfo expected = new CSVFileInfo(
				"exportTest.csv",
				new String[] { "var_0", "var_1", "class" },
				8,
				(char)0);
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportFile(csv, true);
			info = new ArffFileInfoExtractor().extractFileInfo(csv);


		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}

		Assert.assertTrue("Wrong relation", expected.getName().equals(info.getName()));
		Assert.assertTrue("Wrong delimiter", expected.getDelimiter() == info.getDelimiter());
		Assert.assertTrue("Wrong flods " + info.getFirstLineOfDataSegment(), expected.getFirstLineOfDataSegment() == info.getFirstLineOfDataSegment());
		Assert.assertTrue("Wrong features", Arrays.deepEquals(expected.getFeatures(), info.getFeatures()));
	}

	@Test
	public void testExportSSDWithNullSSDFile() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		Exporter ex = new CSVBasedExporter(daHub, selCon, subCon, new ArffFileInfoInjector());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		try {
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));
			ex.exportSSD(null);
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExportSSDNotExisingFile() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		File f = new File(this.path + "no.ssd");

		try {
			List<String[]> expected = new CSVReader(
					new FileReader(RESPATH + "csv_arff_ssd_files/nantest.ssd"), ',', '\'', 4).readAll();
			im.importFile(
					oldArff,
					new File(RESPATH + "csv_arff_ssd_files/nantest.ssd"));

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportSSD(new File(this.path + "no.ssd"));
			if (f.exists()) {
				CSVReader read = new CSVReader(new FileReader(f), ';', '\'', 3);
				List<String[]> l = read.readAll();
				if (f.exists()) {
					if (expected != null && l != null && expected.size() == l.size()) {
						boolean succ = true;
						for (int i = 0; i < l.size(); ++i) {
							for (int j = 0; j < l.get(i).length; ++j) {
								succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
							}
						}
						Assert.assertTrue(succ);
					} else {
						Assert.fail("Amount of Objects as to be the same");
					}
				} else
					Assert.fail("Something wents wrong.");
			} else {
				Assert.fail("Something wents wrong.");
			}
		} catch (Throwable t) {
			Assert.fail();
		} finally {
			if (f.exists())
				f.delete();
		}
	}

	@Test
	public void testExportSSD1() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");

		try {
			List<String[]> expected = new CSVReader(
					new FileReader(RESPATH + "csv_arff_ssd_files/nantest.ssd"), ',', '\'', 4).readAll();
			im.importFile(oldArff, new File(RESPATH
					+ "csv_arff_ssd_files/nantest.ssd"));

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportSSD(ssd);
			CSVReader read = new CSVReader(new FileReader(ssd), ';', '\'', 3);
			List<String[]> l = read.readAll();
			if (ssd.exists()) {
				if (expected != null && l != null && expected.size() == l.size()) {
					boolean succ = true;
					for (int i = 0; i < l.size(); ++i) {
						for (int j = 0; j < l.get(i).length; ++j) {
							succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
						}
					}
					Assert.assertTrue(succ);
				} else {
					Assert.fail("Amount of Objects as to be the same");
				}
			} else {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExportSSD2() {
		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");

		try {
			List<String[]> expected = new CSVReader(
					new FileReader(oldSSD), ',', '\'', 16).readAll();
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportSSD(ssd);
			CSVReader read = new CSVReader(new FileReader(ssd), ';', '\'', 15);
			List<String[]> l = read.readAll();
			if (ssd.exists()) {
				boolean succ = true;
				FileReader expecReader = new FileReader(oldSSD);
				FileReader testReader = new FileReader(ssd);
				for (int i = 0; i <= 13; ++i) {
					succ &= ((expecReader.read()) == (testReader.read()));
				}


				if (expected != null && l != null && expected.size() == l.size()) {
					for (int i = 0; i < l.size(); ++i) {
						for (int j = 0; j < l.get(i).length; ++j) {
							succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
						}
					}
					Assert.assertTrue(succ);
				} else {
					Assert.fail("Amount of Objects has to be the same");
				}
			} else {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExpotWithNoImportedData1() {
		try {
			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(arff, true);
		} catch (Throwable t) {
			t.printStackTrace();
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExpotWithNoImportedData2() {
		try {
			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(arff, false);
		} catch (Throwable t) {
			t.printStackTrace();
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}
	

	@Test
	public void testExportArffWithSSD() {

		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");
		try {
			if (arff.exists())
				arff.delete();
			if (!arff.exists())
				arff.createNewFile();
		} catch (IOException e) {}
		
		try {
			List<String[]> expected = new CSVReader(
					new FileReader(oldSSD), ',', '\'', 16).readAll();
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(arff, true);
			CSVReader read = new CSVReader(new FileReader(ssd), ';', '\'', 15);
			List<String[]> l = read.readAll();
			if (ssd.exists()) {
				boolean succ = true;
				FileReader expecReader = new FileReader(oldSSD);
				FileReader testReader = new FileReader(ssd);
				for (int i = 0; i <= 13; ++i) {
					succ &= ((expecReader.read()) == (testReader.read()));
				}


				if (expected != null && l != null && expected.size() == l.size()) {
					for (int i = 0; i < l.size(); ++i) {
						for (int j = 0; j < l.get(i).length; ++j) {
							succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
						}
					}
					Assert.assertTrue(succ);
				} else {
					Assert.fail("Amount of Objects has to be the same");
				}
			} else {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {}
		
		// Tests exported arff-file.
		boolean succA = true;
			String[] newLine = null;
			String[] oldLine = null;
		try {
			CSVReader oldArffReader = new CSVReader(new FileReader(oldArff), ',', '\'', 36);
			CSVReader newArffReader = new CSVReader(new FileReader(arff), ',', '\'', 38);
			do {
				newLine = newArffReader.readNext();
				oldLine = oldArffReader.readNext();
				if (oldLine == null) break;
				
				for (int i = 0; i < oldLine.length - 1; ++i) {
					succA &= new Float(Float.parseFloat(newLine[i])).equals(Float.parseFloat(oldLine[i]));
				}

			} while (newLine != null);
			
			if (oldLine != null) {
				Assert.fail("old and new file have to have the same size");
			}
			
			
		} catch (Throwable e) {
			Assert.fail("Unexpected Throwable " + e.getClass());
		}
		Assert.assertTrue("Old and new file don't have the same data content", succA);
	}

	@Test
	public void testExportArffWOSSD() {

		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");
		try {
			if (arff.exists())
				arff.delete();
			if (ssd.exists())
				ssd.delete();
			if (!arff.exists())
				arff.createNewFile();
		} catch (IOException e) {}
		
		try {
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(arff, false);
			if (ssd.exists()) {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {}
		
		if (ssd.exists()) {
			Assert.fail("Something wents wrong. There shouldn't be a ssd-file.");
		}
		
		// Tests exported csv-file.
		boolean succA = true;
			String[] newLine = null;
			String[] oldLine = null;
		try {
			CSVReader oldArffReader = new CSVReader(new FileReader(oldArff), ',', '\'', 36);
			CSVReader newArffReader = new CSVReader(new FileReader(arff), ',', '\'', 38);
			do {
				newLine = newArffReader.readNext();
				oldLine = oldArffReader.readNext();
				if (oldLine == null) break;
				
				for (int i = 0; i < oldLine.length - 1; ++i) {
					succA &= new Float(Float.parseFloat(newLine[i])).equals(Float.parseFloat(oldLine[i]));
				}

			} while (newLine != null);
			
			if (oldLine != null) {
				Assert.fail("old and new file have to have the same size");
			}
			
			
		} catch (Throwable e) {}
		Assert.assertTrue("Old and new file don't have the same data content", succA);
	}

	@Test
	public void testExportCSVWithSSD() {

		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");
		try {
			if (csv.exists())
				csv.delete();
			if (!csv.exists())
				csv.createNewFile();
		} catch (IOException e) {}
		
		try {
			List<String[]> expected = new CSVReader(
					new FileReader(oldSSD), ',', '\'', 16).readAll();
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new CSVFileInfoInjector());

			ex.exportFile(csv, true);
			CSVReader read = new CSVReader(new FileReader(ssd), ';', '\'', 15);
			List<String[]> l = read.readAll();
			if (ssd.exists()) {
				boolean succ = true;
				FileReader expecReader = new FileReader(oldSSD);
				FileReader testReader = new FileReader(ssd);
				for (int i = 0; i <= 13; ++i) {
					succ &= ((expecReader.read()) == (testReader.read()));
				}

				if (expected != null && l != null && expected.size() == l.size()) {
					for (int i = 0; i < l.size(); ++i) {
						for (int j = 0; j < l.get(i).length; ++j) {
							succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
						}
					}
					Assert.assertTrue(succ);
				} else {
					Assert.fail("Amount of Objects has to be the same");
				}
			} else {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {}
		
		// Tests exported csv-file.
		boolean succA = true;
			String[] newLine = null;
			String[] oldLine = null;
		try {
			CSVReader oldArffReader = new CSVReader(new FileReader(oldArff), ',', '\'', 36);
			CSVReader newCSVReader = new CSVReader(new FileReader(csv), ',', '\'', 1);
			do {
				newLine = newCSVReader.readNext();
				oldLine = oldArffReader.readNext();
				if (oldLine == null) break;
				
				for (int i = 0; i < oldLine.length - 1; ++i) {
					succA &= new Float(Float.parseFloat(newLine[i])).equals(Float.parseFloat(oldLine[i]));
				}
			} while (newLine != null);
			if (oldLine != null) {
				Assert.fail("old and new file have to have the same size");
			}
			
			
		} catch (Throwable e) {
		}
		Assert.assertTrue("Old and new file don't have the same data content", succA);
	}

	@Test
	public void testExportCSVWOSSD() {

		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");
		try {
			if (csv.exists())
				csv.delete();
			if (ssd.exists())
				ssd.delete();
			if (!csv.exists())
				csv.createNewFile();
		} catch (IOException e) {}
		
		try {
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(csv, false);
			if (ssd.exists()) {
				Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {}
		
		if (ssd.exists()) {
			Assert.fail("Something wents wrong. There shouldn't be a ssd-file.");
		}
		
		// Tests exported csv-file.
		boolean succA = true;
			String[] newLine = null;
			String[] oldLine = null;
		try {
			CSVReader oldArffReader = new CSVReader(new FileReader(oldArff), ',', '\'', 36);
			CSVReader newCSVReader = new CSVReader(new FileReader(csv), ',', '\'', 1);
			do {
				newLine = newCSVReader.readNext();
				oldLine = oldArffReader.readNext();
				if (oldLine == null) break;
				
				for (int i = 0; i < oldLine.length - 1; ++i) {
					succA &= new Float(Float.parseFloat(newLine[i])).equals(Float.parseFloat(oldLine[i]));
				}

			} while (newLine != null);
			
			if (oldLine != null) {
				Assert.fail("old and new file have to have the same size");
			}
			
			
		} catch (Throwable e) {}
		Assert.assertTrue("Old and new file don't have the same data content", succA);
	}
	
	@Test
	public void testExportSelectionWithSSD() {
		this.selCon.select(new int[] {0, 1, 2, 3, 4, 5});

		Importer im = new CSVBasedImporter(database, new ArffFileInfoExtractor());
		File oldArff = new File(RESPATH + "csv_arff_ssd_files/breast.arff");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/breast_for_testing.ssd");
		try {
			if (arff.exists())
				arff.delete();
			if (!arff.exists())
				arff.createNewFile();
		} catch (IOException e) {}
		
		try {
			List<String[]> expected = new CSVReader(
					new FileReader(oldSSD), ',', '\'', 16).readAll();
			im.importFile(oldArff, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);
			Exporter ex = new CSVBasedExporter(hub, selCon, sc, new ArffFileInfoInjector());

			ex.exportFile(arff, true);
			CSVReader read = new CSVReader(new FileReader(ssd), ';', '\'', 15);
			List<String[]> l = read.readAll();
			if (ssd.exists()) {
				boolean succ = true;
				FileReader expecReader = new FileReader(oldSSD);
				FileReader testReader = new FileReader(ssd);
				for (int i = 0; i < 6; ++i) {
					succ &= ((expecReader.read()) == (testReader.read()));
				}


				if (expected != null && l != null && expected.size() == l.size()) {
					for (int i = 0; i < l.size(); ++i) {
						for (int j = 0; j < l.get(i).length; ++j) {
							succ &= new Float(Float.parseFloat(expected.get(i)[j])).equals(Float.parseFloat(l.get(i)[j]));
						}
					}
					Assert.assertTrue(succ);
				} else {
					Assert.fail("Amount of Objects has to be the same");
				}
			} else {
					Assert.fail("Something wents wrong.");
			}

		} catch (Throwable t) {}
		
		// Tests exported arff-file.
		boolean succA = true;
			String[] newLine = null;
			String[] oldLine = null;
		try {
			CSVReader oldArffReader = new CSVReader(new FileReader(oldArff), ',', '\'', 36);
			CSVReader newArffReader = new CSVReader(new FileReader(arff), ',', '\'', 38);

			for (int k = 0; k <= 5; ++k) {
				newLine = newArffReader.readNext();
				oldLine = oldArffReader.readNext();
				if (oldLine == null) break;
				
				for (int i = 0; i < oldLine.length - 1; ++i) {
					succA &= new Float(Float.parseFloat(newLine[i])).equals(Float.parseFloat(oldLine[i]));
				}

			}
			
			if (oldLine != null) {
				Assert.fail("old and new file have to have the same size");
			}
			
			
		} catch (Throwable e) {	}
		Assert.assertTrue("Old and new file don't have the same data content", succA);
	}
}
