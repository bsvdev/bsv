package importexport.exporting;

import importexport.importing.ArffFileInfoExtractor;
import importexport.importing.CSVBasedImporter;
import importexport.importing.CSVFileInfoExtractor;
import importexport.importing.Importer;
import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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

public class FileInfoInjectorTest {
	private static final String RESPATH = "src/test/resources/";
	private final String path = System.getProperty("java.io.tmpdir") + "/bsv_test";
	private final String dbFile = this.path + "/database-importerTest.bsv";
	private Database database = null;
	private SubspaceController subCon = null;
	private File arff = new File(path + "/injectorTest.arff");
	private File csv = new File(path + "/injectorTest.csv");
	private File ssd = new File(path + "/injectorTest.ssd");


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
			this.subCon = new SubspaceController(database);
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
			this.subCon = null;
		} catch (DatabaseAccessException ex) {
			Assert.fail("Databus shutdown fail: " + ex.getMessage());
		}

		// clean up database
		if (this.database != null) {
			Assert.assertEquals(true, new File(this.dbFile).delete());
		}
		arff.delete();
		csv.delete();
		ssd.delete();
	}

	private CSVFileInfo createFileInfo(final File f, FileInfoInjector injector)
			throws DatabaseAccessException {
		String relation = f.getName();
		Feature[] features = this.subCon.getSubspaces()[0].getFeatures();

		String[] featureNames = new String[features.length];
		for (int i = 0; i < features.length; ++i) {
			featureNames[i] = features[i].getName();
		}

		int fLODS = -1;

		if (injector instanceof CSVFileInfoInjector) {
			fLODS = 2;
		} else if (injector instanceof ArffFileInfoInjector) {
			fLODS = 4 + features.length;
		}
		return new CSVFileInfo(relation, featureNames, fLODS, ',');
	}

	@Test
	public void testInject1() {
		File nanarff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		File nanssd = new File(RESPATH + "csv_arff_ssd_files/nantest.ssd");
		ArffFileInfoExtractor extractor = new ArffFileInfoExtractor();
		ArffFileInfoInjector injector = new ArffFileInfoInjector();
		BufferedWriter bw = null;
		CSVFileInfo expected = new CSVFileInfo(
				"injectorTest.arff",
				new String[] { "var_0", "var_1", "class" },
				8,
				(char)0);
		CSVFileInfo info = null;
		Importer im = new CSVBasedImporter(database, extractor);

		try {
			bw = new BufferedWriter(new FileWriter(arff));
			im.importFile(nanarff, nanssd);
			injector.injectFileInfo(
					bw,
					createFileInfo(arff, injector),
					subCon.getSubspaces()[0].getFeatures());

			info = extractor.extractFileInfo(arff);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass() + "   " +arff.toURI()
					+ "\n\n" + info.getDelimiter());
		}
		Assert.assertTrue("Wrong relation", expected.getName().equals(info.getName()));
		Assert.assertTrue("Wrong delimiter", expected.getDelimiter() == info.getDelimiter());
		Assert.assertTrue("Wrong flods " + info.getFirstLineOfDataSegment(), expected.getFirstLineOfDataSegment() == info.getFirstLineOfDataSegment());
		Assert.assertTrue("Wrong features", Arrays.deepEquals(expected.getFeatures(), info.getFeatures()));

	}

	@Test
	public void testInject2() {
		File nanarff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		File nanssd = new File(RESPATH + "csv_arff_ssd_files/nantest.ssd");
		ArffFileInfoExtractor extractor = new ArffFileInfoExtractor();
		CSVFileInfoExtractor extractor2 = new CSVFileInfoExtractor();
		CSVFileInfoInjector injector = new CSVFileInfoInjector();
		BufferedWriter bw = null;
		CSVFileInfo expected = new CSVFileInfo(
				"injectorTest.csv",
				new String[] { "var_0", "var_1" },
				2,
				',');
		CSVFileInfo info = null;
		Importer im = new CSVBasedImporter(database, extractor);

		try {
			bw = new BufferedWriter(new FileWriter(csv));
			im.importFile(nanarff, nanssd);
			injector.injectFileInfo(
					bw,
					createFileInfo(csv, injector),
					subCon.getSubspaces()[0].getFeatures());

			info = extractor2.extractFileInfo(csv);
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass() + "   " + csv.toURI()
					+ "\n\n" + info.getDelimiter());
		}
		Assert.assertTrue("Wrong relation " + info.getName() + " instead of " + expected.getName(),
				expected.getName().equals(info.getName()));
		Assert.assertTrue("Wrong delimiter",
				expected.getDelimiter() == info.getDelimiter());
		Assert.assertTrue("Wrong flods " + info.getFirstLineOfDataSegment(),
				expected.getFirstLineOfDataSegment() == info.getFirstLineOfDataSegment());
		Assert.assertTrue("Wrong features " + Arrays.deepToString(info.getFeatures())
				+ " instead of " + Arrays.deepToString(expected.getFeatures()),
				Arrays.deepEquals(expected.getFeatures(), info.getFeatures()));
	}
	
	@Test
	public void testCSVInjectorNOValidFileInfoType() {
		File nanarff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		File nanssd = new File(RESPATH + "csv_arff_ssd_files/nantest.ssd");
		ArffFileInfoExtractor extractor = new ArffFileInfoExtractor();
		CSVFileInfoInjector injector = new CSVFileInfoInjector();
		BufferedWriter bw = null;
		
		FileInfo info = new XFileInfo(
				this.path + "/injectorTest.csv",
				new String[] { "var_0", "var_1" });
		Importer im = new CSVBasedImporter(database, extractor);

		try {
			bw = new BufferedWriter(new FileWriter(csv));
			im.importFile(nanarff, nanssd);
			injector.injectFileInfo(
					bw,	info,
					subCon.getSubspaces()[0].getFeatures());
			
			BufferedReader br = new BufferedReader(new FileReader(csv));
			String line = br.readLine();
			Assert.assertTrue(line == null);
			
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}
	
	@Test
	public void testArffInjectorNOValidFileInfoType() {
		File nanarff = new File(RESPATH + "csv_arff_ssd_files/nantest.arff");
		File nanssd = new File(RESPATH + "csv_arff_ssd_files/nantest.ssd");
		ArffFileInfoExtractor extractor = new ArffFileInfoExtractor();
		ArffFileInfoInjector injector = new ArffFileInfoInjector();
		BufferedWriter bw = null;
		
		FileInfo info = new XFileInfo(
				this.path + "/injectorTest.arff",
				new String[] { "var_0", "var_1" });
		Importer im = new CSVBasedImporter(database, extractor);

		try {
			bw = new BufferedWriter(new FileWriter(arff));
			im.importFile(nanarff, nanssd);
			injector.injectFileInfo(
					bw,	info,
					subCon.getSubspaces()[0].getFeatures());
			
			BufferedReader br = new BufferedReader(new FileReader(arff));
			String line = br.readLine();
			Assert.assertTrue(line == null);
			
		} catch (Throwable t) {
			Assert.fail("Unexpected Throwable " + t.getClass());
		}
	}
}
		
class XFileInfo extends FileInfo {
	public XFileInfo(String relation, String[] featureSet) {
		super(relation, featureSet);
	}	
}
