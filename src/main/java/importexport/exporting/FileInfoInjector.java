package importexport.exporting;

import importexport.util.FileInfo;

import java.io.BufferedWriter;
import java.io.IOException;

import controller.Feature;
import db.Database;
import db.DatabaseAccessException;

/**
 * Interface which offers all methods for injecting file headers of arbitrary file formats.
 */
public interface FileInfoInjector {

	/**
	 * Produces a String containing the file informations of a set of data which should be 
	 * exported as an arbitrary file.
	 * 
	 * @param fInfo
	 *            Object of Header which contains needed information.
	 * @param bw
	 *            writer which injects header informations into the new file.
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
	void injectFileInfo(final BufferedWriter bw, final FileInfo fInfo, final Feature[] features) throws IOException,
			DatabaseAccessException;
}
