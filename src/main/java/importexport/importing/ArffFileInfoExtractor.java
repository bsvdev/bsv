package importexport.importing;

import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;
import importexport.util.InvalidFileException;
import importexport.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This interface provides methods for extracting header information of
 * arff-based files and storing them into an objects of type FileInfo.
 * 
 * @see FileInfo
 * @see FileInfoExtractor
 */
public class ArffFileInfoExtractor extends CSVFileInfoExtractor {

	/**
	 * Parses the file information of arff-based files.
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

		String relation = null;
		String[] attributes = null;
		List<String> attributeList = new ArrayList<String>();
		int lastHeaderLine = 0;
		CSVReader reader = new CSVReader(new FileReader(f), ' ');
		String[] line = reader.readNext();
		String tmp = null;

		while (line != null && !line[0].equals("@data")) {
			tmp = line[0];

			if (tmp.equals("@relation")) {
				relation = line[1];
			} else if (tmp.equals("@attribute")) {
				attributeList.add(line[1]);
			}

			lastHeaderLine++;
			line = reader.readNext();
		}

		if (line == null) {
			lastHeaderLine = -3; //ain't working without that line. Avoid NullPointerException
		}						 //during import action.

		attributes = new String[attributeList.size()];

		for (int i = 0; i < attributes.length; ++i) {
			attributes[i] = attributeList.get(i);
		}
		
		/*
		 * ain't working without "+ 2". if you remove "+ 2" the importer will 
		 * start with parsing at wrong a line
		 */
		return new CSVFileInfo(relation, attributes, lastHeaderLine + 2, getDelimiter(f));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected char getDelimiter(final File f) throws IOException {
		if (f == null || !f.exists()) {
			throw new FileNotFoundException();
		}

		char ret = 0;
		BufferedReader read = new BufferedReader(new FileReader(f));
		String actLine = read.readLine();

		while (actLine != null && actLine.charAt(0) == '@') {
			actLine = read.readLine();
			if (actLine != null && actLine.isEmpty()) {
				actLine = "@";
			}
		}

		if (actLine != null) {
			ret = Utility.filterDelimiterFromString(actLine);
		}

		read.close();

		return ret;
	}
}
