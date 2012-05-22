package importexport.util;

/**
 * {@inheritDoc} <br />
 * <br />
 * CSVFileInfo is specialized for CSV based file types like .csv, .arff and so on.
 */
public class CSVFileInfo extends FileInfo {
	private final int firstLineOfDataSeg;
	private final char delimiter;

	/**
	 * Constructs a new CSVFileInfo object.
	 * 
	 * @param name
	 *            Name of the dataset.
	 * @param features
	 *            Column names of the dataset.
	 * @param firstLineOfDataSeg
	 *            Line number where the dataset begins.
	 * @param delimiter
	 *            Delimiter of the csv based file.
	 */
	public CSVFileInfo(final String name, final String[] features, final int firstLineOfDataSeg, final char delimiter) {
		super(name, features);

		this.firstLineOfDataSeg = firstLineOfDataSeg;
		this.delimiter = delimiter;
	}

	/**
	 * Line number where the concrete dataset begins.
	 * 
	 * @return first line of concrete data.
	 */
	public final int getFirstLineOfDataSegment() {
		return this.firstLineOfDataSeg;
	}

	/**
	 * Returns delimiter which is used in the CSV based file which should be imported.
	 * 
	 * @return delimiter of the file.
	 */
	public final char getDelimiter() {
		return this.delimiter;
	}
}
