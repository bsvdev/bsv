package importexport.util;

/**
 * An object of type FileInfo contains basic informations, which are needed for file imports, about an File which
 * contains table structured data, but not the concrete dataset.
 */
public class FileInfo {

	/**
	 * Name of the dataset.
	 */
	private final String name;

	/**
	 * Feature names of the dataset.
	 */
	private final String[] features;

	/**
	 * Constructs a concrete object of type FileInfo
	 * 
	 * @param relation
	 *            Name of the dataset.
	 * @param featureSet
	 *            column names of the dataset.
	 */
	public FileInfo(final String relation, final String[] featureSet) {
		this.name = relation;
		this.features = featureSet;
	}

	/**
	 * Returns the name of the dataset.
	 * 
	 * @return name of dataset.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Returns column names of the dataset.
	 * 
	 * @return column names of the dataset.
	 */
	public final String[] getFeatures() {
		return this.features;
	}

	/**
	 * Returns the number of features of the data set.
	 * 
	 * @return the number of features
	 */
	public final int noOfFeatures() {
		return (features != null ? features.length : 0);
	}
}
