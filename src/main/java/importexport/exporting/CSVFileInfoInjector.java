package importexport.exporting;

import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import controller.Feature;
import db.Database;
import db.DatabaseAccessException;

/**
 * Injects the header of a .csv-file which should be build.
 */
public class CSVFileInfoInjector implements FileInfoInjector {

	/**
	 * Produces a String containing the file informations of a set of data which should be 
	 * exported as an csv-based (e.g. ".csv" or ".arff") formatted file.
	 * 
	 * @param fInfo
	 *            Object of {@link FileInfo} which contains needed information.
	 * @param bw
	 *            writer which injects {@link FileInfo} informations into the new file.
	 * @param features
	 *            array of all features in the database.
	 * 
	 * @throws IOException
	 *             threw if arbitrary IO error has occured.
	 * @throws DatabaseAccessException
	 *             threw if any error occurs while trying to get information out of the database.
	 * 
	 * @see FileInfo
	 * @see Database
	 * @see DatabaseAccessException
	 */
	@Override
	public void injectFileInfo(final BufferedWriter bw, final FileInfo fInfo, final Feature[] features)
			throws IOException, DatabaseAccessException {

		if (fInfo instanceof CSVFileInfo) {
			int normalFeatNr = 0;
			for (Feature f : features) {
				if (!f.isOutlier()) {
					++normalFeatNr;
				}
			}

			Feature[] newFeats = Arrays.copyOfRange(features, 1, normalFeatNr);
			StringBuilder head = new StringBuilder(Arrays.deepToString(newFeats));
			head.deleteCharAt(0);
			head.deleteCharAt(head.length() - 1);

			bw.append(head.toString());
			bw.newLine();
			bw.flush();
		}
	}
}
