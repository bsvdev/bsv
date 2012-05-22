package importexport;

import importexport.importing.ArffFileInfoExtractor;
import importexport.importing.CSVBasedImporter;
import importexport.importing.CSVFileInfoExtractor;
import importexport.importing.Importer;
import importexport.util.UnsupportedFileExtensionException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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

public class ExportLogicTest {
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


	@BeforeClass
	public static void testGetExportFormatsWOInit() {
		try {
			ExportLogic.getInstance().getExportFormats();
			Assert.fail("ExportLogic cannot be non-null");
		} catch (NullPointerException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

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

		try {
			this.database = new Database(this.dbFile);
			this.selCon = new SelectionController();
			this.subCon = new SubspaceController(database);
			this.grCon = new GroupController(database, subCon);
			this.daHub = new DataHub(database, grCon, subCon);
			arff.createNewFile();
			csv.createNewFile();
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
	public void testInitWithNullArguments() {
		try {
			ExportLogic.init(null, null, null);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(this.daHub, null, null);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(null, this.selCon, null);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(null, null, this.subCon);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(this.daHub, this.selCon, null);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(this.daHub, null, this.subCon);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(null, this.selCon, this.subCon);
			Assert.fail("ExportLogic can't instantiated with null args");
		} catch (IllegalArgumentException e) {
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
		try {
			ExportLogic.init(this.daHub, this.selCon, this.subCon);
			Assert.assertTrue(ExportLogic.getInstance() != null);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExportFormats() {
		ExportLogic.init(daHub, selCon, subCon);
		String[] extensions = ExportLogic.getInstance().getExportFormats();
		boolean succ = true;
		if (extensions.length > 0) {
			succ &= Arrays.asList(extensions).contains("arff");
			succ &= Arrays.asList(extensions).contains("csv");
			Assert.assertTrue(succ);
			succ &= Arrays.asList(extensions).contains("xxx");
			Assert.assertTrue("Not supported File extension.", !succ);
		} else {
			Assert.fail();
		}
	}

	@Test
	public void testExportNotValidExtensionWithSSD() {
		ExportLogic.init(daHub, selCon, subCon);
		try {
			ExportLogic.getInstance().exportFile(new File(path + "/nonValidExtension.xxx"), true);
			Assert.fail("Export shouldn't able with a non valid file extension.");
		} catch(UnsupportedFileExtensionException e) {

		} catch(Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExportNotValidExtensionWOSSD() {
		ExportLogic.init(daHub, selCon, subCon);
		try {
			ExportLogic.getInstance().exportFile(new File(path + "/nonValidExtension.xxx"), false);
			Assert.fail("Export shouldn't able with a non valid file extension.");
		} catch(UnsupportedFileExtensionException e) {

		} catch(Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}

	@Test
	public void testExportNoFileNameExtension() {
		ExportLogic.init(daHub, selCon, subCon);
		try {
			ExportLogic.getInstance().exportFile(new File(path + "/noExtension"), false);
			Assert.fail("Export shouldn't able with a non valid file extension.");
		} catch(UnsupportedFileExtensionException e) {

		} catch(Throwable t) {
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

			ExportLogic.init(hub, selCon, sc);
			ExportLogic ex = ExportLogic.getInstance();

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

			ExportLogic.init(hub, selCon, sc);
			ExportLogic ex = ExportLogic.getInstance();

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

			ExportLogic.init(hub, selCon, sc);
			ExportLogic ex = ExportLogic.getInstance();

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

			ExportLogic.init(hub, selCon, sc);
			ExportLogic ex = ExportLogic.getInstance();

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
	public void testIE_FileWithMoreThan3000Objects() {

		Importer im = new CSVBasedImporter(database, new CSVFileInfoExtractor());
		File oldCSV = new File(RESPATH + "csv_arff_ssd_files/synth_01_copy_with_20000.csv");
		File oldSSD = new File(RESPATH + "csv_arff_ssd_files/20000_objekte_5_subspaces.ssd");
		try {
			if (csv.exists())
				csv.delete();
			if (ssd.exists())
				ssd.delete();
			if (!csv.exists())
				csv.createNewFile();
		} catch (IOException e) {}

		try {
			im.importFile(oldCSV, oldSSD);

			SubspaceController sc = new SubspaceController(database);
			GroupController gc = new GroupController(database, sc);
			DataHub hub = new DataHub(database, gc, sc);

			ExportLogic.init(hub, selCon, sc);
			ExportLogic ex = ExportLogic.getInstance();

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
			CSVReader oldArffReader = new CSVReader(new FileReader(oldCSV), ',', '\'', 1);
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
}
