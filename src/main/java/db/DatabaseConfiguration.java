package db;

/**
 * The class {@code DatabaseConfiguration} represents a basic set of configurations for the {@code Database}.
 */
public final class DatabaseConfiguration {

	/**
	 * The Version of the database table layout, to handle incompatibilities.
	 * 
	 * This should be incremented every time a configuration parameter changes
	 */
	public static final int LAYOUTVERSION = 4;

	/**
	 * The length of the VARCHAR fields in the database.
	 * 
	 * This does not include e.g. descriptions, which are of type TEXT
	 */
	public static final int VARCHARLENGTH = 20;

	/**
	 * The maximum size of a single transaction.
	 */
	public static final int TRANSACTIONSIZE = 3000;

	// utility class, do not construct
	private DatabaseConfiguration() {
		throw new AssertionError();
	}
}