package importexport.importing;

import importexport.util.CSVFileInfo;
import importexport.util.InvalidFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.bytecode.opencsv.CSVReader;
import db.Database;
import db.DatabaseAccessException;

/**
 *{@inheritDoc}.
 */
public class CSVBasedImporter extends Importer {

	/**
	 * Constructs a new instance of Importer which is specialized for csv-based file formats.
	 * 
	 * @param db
	 *            Database where imported should be stored.
	 * @param extractor
	 *            Extractor specialized for csv based formats which is extracting necessary informatinos for importing a
	 *            file.
	 */
	public CSVBasedImporter(final Database db, final CSVFileInfoExtractor extractor) {
		super(db, extractor);
	}

	/**
	 * Imports an csv-based file and a belonging .ssd-file.
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
	@Override
	public void importFile(final File input, final File algoOut) throws IOException, DatabaseAccessException,
			InvalidFileException {
		if (input == null || algoOut == null || !input.exists() || !algoOut.exists()) {
			throw new FileNotFoundException();
		}

		CSVFileInfo info = (CSVFileInfo) this.getFileInfoExtractor().extractFileInfo(input);
		CSVReader cr = new CSVReader(new FileReader(input), info.getDelimiter(), '\'',
				info.getFirstLineOfDataSegment() - 1);

		if (info.getFeatures().length < 2) {
			throw new InvalidFileException();
		}
		
		//amount of queues is equal to the amount of defined subspaces.
		LinkedBlockingQueue<Float>[] queues = this.parseAlgoOut(algoOut, info.noOfFeatures());

		//number of all features (natural features + outlierness values.
		int amountFeatures = queues.length + info.noOfFeatures();
		int infoNoOfFeatures = info.noOfFeatures();

		//because the class array is no real feature.
		if (info.getFeatures()[info.getFeatures().length - 1].matches("class(.)*")) {
			--amountFeatures;
			--infoNoOfFeatures;
		}

		//amount of all features which are detected in ssd.
		int amountObjects = queues[0].size();

		String[] allFeatures = new String[amountFeatures];
		boolean[] featureTypes = new boolean[amountFeatures];

		int i = 0;

		for (; i < infoNoOfFeatures; ++i) {
			allFeatures[i] = info.getFeatures()[i];
			featureTypes[i] = false;
		}

		for (int k = 0; i < amountFeatures; ++k, ++i) {
			allFeatures[i] = "Outlierness" + (k + 1);
			featureTypes[i] = true;
		}

		getDB().initFeatures(allFeatures, featureTypes);

		String[] line = null;
		//buffervector of build objects which will be stored next.
		float[][] batch = new float[BATCH_SIZE][];
		//current built object.
		float[] actObj = null;

		int actBatchSize = 0;

		for (int k = 0; k < amountObjects; ++k) {
			int pos = 0;
			actObj = new float[amountFeatures];
			line = cr.readNext();
			
			if (line.length == 0 || (line.length == 1  & (line[0] == null || line[0].isEmpty()))
					|| line.length == 2 & (line[0] == null || line[0].isEmpty()) & (line[1] == null || line[1].isEmpty())) {
				--k;
				continue;
			}

			for (; pos < infoNoOfFeatures; ++pos) {
				try {
					actObj[pos] = Float.parseFloat(line[pos]);
				} catch (NumberFormatException e) {
					actObj[pos] = Float.NaN;
				}
			}

			for (int m = 0; m < queues.length; ++m, ++pos) {
				actObj[pos] = queues[m].poll();
			}

			batch[actBatchSize++] = actObj;

			if (actBatchSize >= BATCH_SIZE) {
				getDB().pushObject(batch);
				actBatchSize = 0;
				batch = new float[BATCH_SIZE][];
				System.gc();
			}
		}

		// removing null vectors from last batch matrix.
		int noVectors = 0;
		while (noVectors < BATCH_SIZE && batch[noVectors] != null) {
			noVectors++;
		}

		float[][] batchOle = new float[noVectors][amountFeatures];

		for (int k = 0; k < noVectors; ++k) {
			batchOle[k] = batch[k];
		}

		// push last batch matrix.
		this.getDB().pushObject(batchOle);

		// update db min/max values
		this.getDB().updateFeaturesMinMax();

		cr.close();
	}
}