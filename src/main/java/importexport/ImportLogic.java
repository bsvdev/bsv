package importexport;

import importexport.importing.ArffFileInfoExtractor;
import importexport.importing.CSVBasedImporter;
import importexport.importing.CSVFileInfoExtractor;
import importexport.importing.Importer;
import importexport.util.InvalidFileException;
import importexport.util.UnsupportedFileExtensionException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import db.Database;
import db.DatabaseAccessException;

/**
 * ImportLogic administrates importers and importing files.
 */
public final class ImportLogic {

	/**
	 * Concrete instance for the ImportLogic.
	 */
	private static ImportLogic instance = new ImportLogic();

	/**
	 * Stores associations between ending of a filename and an concrete importer.
	 */
	private final HashMap<String, Importer> importer;

	/**
	 * Database where the imported data will be stored.
	 */
	private Database db = null;

	/**
	 * Creates the Instance for the import logic.
	 */
	private ImportLogic() {
		importer = new HashMap<String, Importer>();
		db = null;
	}

	/**
	 * Adds an concrete importer to this ImportLogic.
	 * 
	 * @param formatEnding
	 *            Ending of files which should be imported with the transmitted importer.
	 * @param concreteImporter
	 *            The concrete importer which should import files with the given ending.
	 */
	private void add(final String formatEnding, final Importer concreteImporter) {
		this.importer.put(formatEnding, concreteImporter);
	}

	/**
	 * Initialize or rather reinitialize the static ImportLogic instance.
	 * 
	 * @param db
	 *            Database object needed for importing data from csv-based files.
	 */
	public static void init(final Database db) {
		if (db == null) {
			throw new IllegalArgumentException("Database is needed for importing data!");
		}

		instance.db = db;
		instance.add("arff", new CSVBasedImporter(db, new ArffFileInfoExtractor()));
		instance.add("csv", new CSVBasedImporter(db, new CSVFileInfoExtractor()));
	}

	/**
	 * Returns the initialized instance of the ImpoortLogic.
	 * 
	 * For getting an instance of ImportLogic you have to initialize it with a non null Database object.
	 * 
	 * @return the static instance of class ImportLogic or null if instance isn't initialized.
	 */
	public static ImportLogic getInstance() {
		if (instance.db != null) {
			return instance;
		}

		return null;
	}

	/**
	 * Imports a csv-based file with a valid output file of an datamining algorithm.
	 * 
	 * @param input
	 *            File which should be imported.
	 * @param algoOutput
	 *            Algorithm output.
	 * 
	 * @throws IOException
	 *             threw if something other is going wrong.
	 * @throws DatabaseAccessException
	 *             threw if connection to Database failed.
	 * @throws UnsupportedFileExtensionException
	 *             threw if someone is trying to import a file with an unsupported file extension.
	 * @throws InvalidFileException
	 *             threw if a not-importable file is imported.
	 * @throws InterruptedException
	 *             threw if current Thread got interrupted.
	 */
	public void importFile(final File input, final File algoOutput) throws IOException, DatabaseAccessException,
			UnsupportedFileExtensionException, InvalidFileException, InterruptedException {

		String extension = "";
		int lastDot = input.getName().lastIndexOf('.');

		if (lastDot > -1) {
			extension = input.getName().substring(lastDot + 1, input.getName().length());
			Importer actImporter = importer.get(extension);

			if (actImporter != null) {
				actImporter.importFile(input, algoOutput);
			} else {
				throw new UnsupportedFileExtensionException();
			}
		} else {
			throw new InvalidFileException();
		}
	}

	/**
	 * Returns a String[] with all available format endings.
	 * 
	 * @return all available format endings as String[].
	 */
	public String[] getImportFormats() {
		String[] formats = importer.keySet().toArray(new String[importer.keySet().size()]);
		Arrays.sort(formats);

		return formats;
	}
}
