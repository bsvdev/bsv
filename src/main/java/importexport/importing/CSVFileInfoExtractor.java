package importexport.importing;

import gui.settings.Settings;
import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;
import importexport.util.InvalidFileException;
import importexport.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;

import au.com.bytecode.opencsv.CSVReader;


/**
 * This interface provides methods for extracting header information of
 * csv-based files and storing them into an objects of type FileInfo.
 * 
 * @see FileInfo
 * @see FileInfoExtractor
 */
public class CSVFileInfoExtractor implements FileInfoExtractor {

	/**
	 * Parses the file information of csv-based files.
	 * 
	 * @param f
	 *            File which contains header which should be extracted.
	 * @return an object of Header with extracted informations.
	 * 
	 * @throws IOException
	 *             threw if something other is going wrong.
	 * @throws InvalidFileException
	 *             threw if someone tries to import a defect file.
	 * @see FileInfo
	 */
	@Override
	public CSVFileInfo extractFileInfo(final File f) throws IOException, InvalidFileException {
		if (f == null || !f.exists()) {
			throw new FileNotFoundException();
		}

		int firstDataSegLine = 2;
		String relation = f.getName();
		String[] attributes = null;
		char delimiter = getDelimiter(f);

		CSVReader reader = new CSVReader(new FileReader(f.getAbsoluteFile()), delimiter);
		attributes = reader.readNext();

		for (int i = 0; i < attributes.length; ++i) {
			attributes[i] = attributes[i].trim();
		}

		/*
		 * This part should indicate if the csv-file which 
		 * should be imported has no features (header / column names).
		 */
		int tally = 0;
		if (attributes != null && attributes.length > 0) {
			if (attributes.length == 2) {
				if ((attributes[0] == null || attributes[0].isEmpty())
						&& ((attributes[1] == null || attributes[1].isEmpty()))) {
					throw new InvalidFileException();
				}
			}

			for (String x : attributes) {
				if (Utility.isFloat(x)) {
					tally++;
				}
			}

			if (tally > 1) {
				ResourceBundle rb = Settings.getInstance().getResourceBundle();

				throw new InvalidFileException(rb.getString("Import.ErrorCSVFeaturesAreFloats")
						+ rb.getString("Import.NoInvalidFeatureNames") + " " + tally);
			}
		}

		reader.close();

		return new CSVFileInfo(relation, attributes, firstDataSegLine, delimiter);
	}

	/**
	 * Determines character is used as separator.
	 * 
	 * @param f
	 *            csv-file which should be scanned.
	 * @return the used separator.
	 * 
	 * @throws IOException
	 *             threw if something other is going wrong.
	 */
	protected char getDelimiter(final File f) throws IOException {
		if (f == null || !f.exists()) {
			throw new FileNotFoundException();
		}

		char ret = 0;
		BufferedReader read = new BufferedReader(new FileReader(f));
		String actLine = read.readLine();

		if (actLine != null) {
			ret = Utility.filterDelimiterFromString(actLine);
		}

		read.close();

		return ret;
	}
}
