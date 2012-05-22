package importexport.importing;

import gui.settings.Settings;
import importexport.util.InvalidFileException;
import importexport.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.bytecode.opencsv.CSVReader;
import db.Database;
import db.DatabaseAccessException;
import db.DatabaseConfiguration;

/**
 * Class Importer is an abstract importer for an arbitrary based files.
 */
public abstract class Importer {

	/**
	 * N<sup>o</sup> of object which should be pushed together.
	 */
	public static final int BATCH_SIZE = DatabaseConfiguration.TRANSACTIONSIZE;

	/**
	 * Position where definition of a subspace base begins.
	 */
	private static final int START_BASE_DEF = 5;

	/**
	 * Position where the dimension is mentioned.
	 */
	private static final int POS_SS_SIZE = 4;

	/**
	 * Database where data will stored.
	 */
	private final Database database;

	/**
	 * Extracts all necessary informations from a file which are needed for storing the data correctly.
	 */
	private final FileInfoExtractor infoExtractor;

	/**
	 * Constructs an Importer.
	 *
	 * @param db
	 *            place where imported data will be stored.
	 * @param extractor
	 *            needed for extracting informations from dataset.
	 */
	public Importer(final Database db, final FileInfoExtractor extractor) {
		if (db == null || extractor == null) {
			throw new IllegalArgumentException();
		}

		this.database = db;
		this.infoExtractor = extractor;
	}

	/**
	 * Imports an arbitrary file and a belonging .ssd-file.
	 *
	 * @param input
	 *            Reference of the file which should be imported.
	 * @param algoOut
	 *            Reference of the .ssd file which is made by a Datamining algorithm.
	 *
	 * @throws IOException
	 *             threw if something other is going wrong.
	 * @throws DatabaseAccessException
	 *             threw if something went wrong with the Database connection.
	 * @throws InvalidFileException
	 *             threw if importing file isn't valid.
	 */
	public abstract void importFile(File input, File algoOut) throws IOException, DatabaseAccessException,
			InvalidFileException;

	/**
	 * Parses .ssd-Files and submits subspaces to Database.
	 *
	 * @param algoOut
	 *            Output file of an Dataminig algortihm containing detected subspaces.
	 *
	 * @return a queue of with outlierness values to each object in each subspace.
	 *
	 * @throws IOException
	 *             threw if something other is going wrong.
	 * @throws DatabaseAccessException
	 *             threw if something went wrong with the Database connection.
	 */
	@SuppressWarnings(value = { "unchecked" })
	protected final LinkedBlockingQueue<Float>[] parseAlgoOut(final File algoOut, int startOutliernessRef)
			throws IOException, DatabaseAccessException {
		if (algoOut == null || !algoOut.exists()) {
			throw new FileNotFoundException(Settings.getInstance().getResourceBundle().getString("noFile"));
		}

		int curOutlierRef = startOutliernessRef;

		String line = null;
		int[] actSubspace = null;
		int idx = 1;

		BufferedReader reader = new BufferedReader(new FileReader(algoOut));
		line = reader.readLine();
		int noSubspaces = 0;
		char delimiter = '0';

		while (line != null && !line.equals("@data")) {
			actSubspace = line.isEmpty() ? null : parseLine(line);

			if (actSubspace != null) {
				int[] tmp = new int[actSubspace.length + 1];
				tmp[0] = curOutlierRef++;

				for (int i = 1; i < tmp.length; ++i) {
					tmp[i] = actSubspace[i - 1];
				}

				database.pushSubspace(idx, tmp, "Subspace " + idx);
				noSubspaces++;
			}

			line = reader.readLine();
			idx++;
		}

		try {
			line = reader.readLine();
			delimiter = Utility.filterDelimiterFromString(line);
		} catch (NullPointerException npe) {
			throw new InvalidFileException();
		}

		// Pos in Array is equal to the idx - 1 of a subspace
		LinkedBlockingQueue<Float>[] queue = new LinkedBlockingQueue[noSubspaces];

		// first line of dataseg is a specialcase.
		String[] sa = line.split("" + delimiter);
		int tmp;
		CSVReader cr = new CSVReader(reader, delimiter);

		while (sa != null) {
			if (sa.length == 0 || (sa.length == 1  & (sa[0] == null || sa[0].isEmpty()))
					|| sa.length == 2 & (sa[0] == null || sa[0].isEmpty()) & (sa[1] == null || sa[1].isEmpty())) {
				sa = cr.readNext();
				continue;
			}			
			
			tmp = Integer.parseInt(sa[1].trim());

			if (queue[tmp] == null) {
				queue[tmp] = new LinkedBlockingQueue<Float>();
			}

			queue[tmp].offer(Float.parseFloat(sa[2]));
			sa = cr.readNext();
		}

		reader.close();
		System.gc();

		return queue;
	}

	/**
	 * Parses a subspace definition line of a .sdd-File.
	 *
	 * @param line
	 *            of a ssd file
	 * @return an int[] with feature ids spanning a feature space.
	 */
	private int[] parseLine(final String line) {
		String[] splittedLine = line.split("(\\,|\\[|\\]|\\s+)");

		int k = -1;

		int l = 0;

		for (int i = 0; i < splittedLine.length; ++i) {
			if (!splittedLine[i].isEmpty()) {
				l++;
			}
		}

		String[] splittedLine2 = new String[l];
		l = 0;

		for (int i = 0; i < splittedLine.length; ++i) {
			if (!splittedLine[i].isEmpty()) {
				splittedLine2[l++] = splittedLine[i];
			}
		}

		splittedLine = splittedLine2;

		int[] res = new int[Integer.parseInt(splittedLine[POS_SS_SIZE])];

		if (res.length > 0) {
			for (int i = START_BASE_DEF; i < splittedLine.length; ++i) {
				k = i - START_BASE_DEF;
				res[k] = Integer.parseInt(splittedLine[i]) + 1;
			}
		}

		return res;
	}

	/**
	 * Returns referenced Database object.
	 *
	 * @return the held Database object.
	 */
	protected final Database getDB() {
		return this.database;
	}

	/**
	 * Returns referenced FileInfoExtractor object.
	 *
	 * @return the held FileInfoExtractor object.
	 */
	protected final FileInfoExtractor getFileInfoExtractor() {
		return this.infoExtractor;
	}
}
