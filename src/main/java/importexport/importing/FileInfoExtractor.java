package importexport.importing;

import importexport.util.FileInfo;
import importexport.util.InvalidFileException;

import java.io.File;
import java.io.IOException;

/**
 * This interface provides methods for extracting header information of
 * arbitrary formatted files and storing them into an objects of type FileInfo.
 * 
 * @see FileInfo
 */
public interface FileInfoExtractor {

	/**
	 * Parses the file information of arbitrary formatted files.
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
	FileInfo extractFileInfo(final File f) throws IOException, InvalidFileException;
}
