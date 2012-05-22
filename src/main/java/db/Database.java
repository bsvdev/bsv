package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import util.Failure;

/**
 * The class {@code Database} represents the database storage system.
 */
public class Database {
	// the path to the sql provider
	private String path = null;

	// for now we only support sqlite
	private final String driver = "jdbc:sqlite";
	private final String driverName = "org.sqlite.JDBC";

	// the database connection; context session for the database
	private Connection connection = null;

	/**
	 * Constructs a database.
	 *
	 * @param path
	 *            the path to the database provider
	 * @throws InvalidDriverException
	 *             if the SQL driver is invalid
	 * @throws IncompatibleVersionException
	 *             if the database layout is incompatible
	 * @throws DatabaseAccessException
	 *             if the connection to the database could not be created
	 */
	public Database(String path) throws InvalidDriverException, IncompatibleVersionException, DatabaseAccessException {

		initProvider(path);

		// for now, driver and connection are hardcoded
		initDriver();
		initConnection();

		// driver optimization, pragmas
		initConfig();

		// create tables on first use, or check version if tables exists
		initLayout();
	}

	/**
	 * Shuts down the database.
	 *
	 * No other operations are accepted after this call.
	 *
	 * @throws DatabaseAccessException
	 *             if the connection cannot be closed
	 */
	public void shutdown() throws DatabaseAccessException {
		try {
			this.connection.close();
		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.CONNECTION);
		}
	}

	/**
	 * Sets the path for the database to {@code path}
	 *
	 * @param path
	 *            the path
	 */
	private void initProvider(String path) {
		this.path = path;
	}

	/**
	 * Loads the required jdbc driver
	 *
	 * @throws InvalidDriverException
	 *             if driver was invalid or could not be loaded
	 */
	private void initDriver() throws InvalidDriverException {
		try {
			Class.forName(this.driverName);
		} catch (ClassNotFoundException e) {
			// unable to load driver; invalid
			throw new InvalidDriverException(Failure.DRIVER);
		}
	}

	/**
	 * Establishes a connection with the database
	 *
	 * @throws DatabaseAccessException
	 *             if the connection could not be created
	 */
	private void initConnection() throws DatabaseAccessException {
		try {
			this.connection = DriverManager.getConnection(this.driver + ":" + this.path);

		} catch (SQLException e) {
			// unable to create the connection; access error
			throw new DatabaseAccessException(Failure.CONNECTION);
		}
	}

	/**
	 * Sets the SQLite specific pragmas to boost the performance
	 *
	 * @throws DatabaseAccessException
	 *             if the pragmas could not be set in database
	 */
	private void initConfig() throws DatabaseAccessException {
		Statement stmt = null;

		try {
			stmt = this.connection.createStatement();

			// With synchronous OFF, SQLite continues without syncing
			// as soon as it has handed data off to the operating system.
			stmt.execute("PRAGMA synchronous = OFF;");

			// The MEMORY journaling mode stores the rollback journal in volatile RAM.
			// This saves disk I/O but at the expense of database safety and integrity.
			stmt.execute("PRAGMA journal_mode = MEMORY;");

			// The journal_size_limit pragma may be used to limit the size of rollback-journal.
			// -1 means no limit.
			stmt.execute("PRAGMA journal_size_limit = -1;");

			// If the argument N is negative, then the number of cache pages
			// is adjusted to use approximately N*1024 bytes of memory.
			stmt.execute("PRAGMA cache_size = -50000;");

			// Once an encoding has been set for a database, it cannot be changed.
			stmt.execute("PRAGMA encoding = \"UTF-8\";");

			// When temp_store is MEMORY temporary tables and indices are kept
			// in as if they were pure in-memory databases memory.
			stmt.execute("PRAGMA temp_store = MEMORY;");

			stmt.close();

		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.CONFIG);
		}
	}

	/**
	 * Checks if the database layout exists already and create new one if needed
	 *
	 * @throws DatabaseAccessException
	 *             if the creation of new tables fails
	 * @throws IncompatibleVersionException
	 *             if the existing layout is deprecated
	 */
	private void initLayout() throws DatabaseAccessException, IncompatibleVersionException {
		Statement stmt = null;
		ResultSet rs;
		int version = -1;

		try {
			stmt = this.connection.createStatement();

			rs = stmt.executeQuery("SELECT Version FROM Metadata;");
			version = rs.getInt(1);

			stmt.close();

			if (version != DatabaseConfiguration.LAYOUTVERSION) {
				throw new IncompatibleVersionException(Failure.VERSION);
			}
		} catch (SQLException e) {
			// if there isn't at least the Metadata table with the Version field, we operate on a clean database
			createTables();
		}

	}

	/**
	 * Creates a new database with all tables
	 *
	 * @throws DatabaseAccessException
	 *             if the creation failed
	 */
	private void createTables() throws DatabaseAccessException {
		Statement stmt = null;
		PreparedStatement prepStmt = null;

		try {
			stmt = this.connection.createStatement();

			// be sure to drop all tables in case someone manipulated the database manually
			stmt.executeUpdate("DROP TABLE IF EXISTS DynamicConstraints;");
			stmt.executeUpdate("DROP TABLE IF EXISTS Features;");
			stmt.executeUpdate("DROP TABLE IF EXISTS Groups;");
			stmt.executeUpdate("DROP TABLE IF EXISTS Metadata;");
			stmt.executeUpdate("DROP TABLE IF EXISTS Objects;");
			stmt.executeUpdate("DROP TABLE IF EXISTS StaticConstraints;");
			stmt.executeUpdate("DROP TABLE IF EXISTS Subspaces;");

			// populate database with tables.. by using ugly sql
			stmt.executeUpdate("CREATE TABLE DynamicConstraints(Id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " Operator INTEGER, FeatureReference INTEGER,"
					+ " GroupReference INTEGER, Value FLOAT, Active BOOLEAN);");
			stmt.executeUpdate("CREATE TABLE Features(Id INTEGER PRIMARY KEY AUTOINCREMENT," + " Name VARCHAR("
					+ DatabaseConfiguration.VARCHARLENGTH + "), OutlierFlag BOOLEAN, Min FLOAT, Max FLOAT);");
			stmt.executeUpdate("CREATE TABLE Groups(Id INTEGER PRIMARY KEY AUTOINCREMENT, Name VARCHAR("
					+ DatabaseConfiguration.VARCHARLENGTH + "),"
					+ " Visibility BOOLEAN, Color INTEGER, ColorCalculatedByFeature INTEGER, Description TEXT);");
			stmt.executeUpdate("CREATE TABLE Metadata(Version INTEGER);");

			// Object table is created in initFeatures, to boost performance

			stmt.executeUpdate("CREATE TABLE StaticConstraints(Id INTEGER, GroupReference INTEGER,"
					+ " ObjectReference INTEGER, Active BOOLEAN);");
			stmt.executeUpdate("CREATE TABLE Subspaces(Id INTEGER, FeatureReference INTEGER," + " Name VARCHAR("
					+ DatabaseConfiguration.VARCHARLENGTH + "));");

			stmt.close();

			// after creating the tables, write the layout version
			prepStmt = this.connection.prepareStatement("INSERT INTO Metadata VALUES(?);");
			prepStmt.setInt(1, DatabaseConfiguration.LAYOUTVERSION);
			prepStmt.execute();

			prepStmt.close();

		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.LAYOUT);
		}
	}

	/**
	 * Returns the database connection.
	 *
	 * @return the database connection
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Returns true if database exists but is empty.
	 * True also could indicate a read error. Check if Database is readable before using this method.
	 *
	 * @return true if database exists but is empty
	 */
	public boolean isEmpty() {
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			stmt = this.connection.createStatement();

			// without features we are not able to operate correctly
			rs = stmt.executeQuery("SELECT COUNT(Id) FROM Features;");
			count = rs.getInt(1);

			stmt.close();

		} catch (SQLException e) {
			// for now, do not handle read exceptions, b/c exception here also indicates the failure to get features
		}

		return (0 == count);

	}

	/**
	 * Stores a range of new objects in the database.
	 *
	 * Stores the object's values for each feature within the range of objects
	 *
	 * @param objects
	 *            the objects with their values of the features to store
	 * @throws DatabaseAccessException
	 *             if the write operation failed at database level
	 */
	public void pushObject(float[][] objects) throws DatabaseAccessException {
		PreparedStatement prepStmt = null;

		// dynamically insert values in columns of a object row
		String sql = "INSERT INTO Objects VALUES(NULL";
		for (int i = 1; i <= objects[0].length; ++i) {
			sql += ", ?";
		}
		sql += ");";

		try {
			prepStmt = this.connection.prepareStatement(sql);

			// for each object
			for (float[] values : objects) {
				// for each value in the object
				for (int featureId = 1; featureId <= values.length; ++featureId) {
					prepStmt.setFloat(featureId, values[featureId - 1]);
				}
				prepStmt.addBatch();
			}

			// do not atomically write each insert, but write them all at once, thus boosting write performance
			this.connection.setAutoCommit(false);
			prepStmt.executeBatch();
			this.connection.setAutoCommit(true);

			prepStmt.close();

		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}

	/**
	 * Initializes all features, based on their name.
	 *
	 * @param features
	 *            the name of the features
	 * @param outlierFlags
	 *            the outlier flags of the features, indicating that this is a custom feature
	 * @throws DatabaseAccessException
	 *             if the write operation failed at database level
	 */
	public void initFeatures(String[] features, boolean[] outlierFlags) throws DatabaseAccessException {
		Statement stmt = null;
		PreparedStatement prepStmt = null;

		// build sql table query: Id | "1" | "2" | ... by doing this, we are able to store all values in one row
		String sql = "CREATE TABLE IF NOT EXISTS Objects(Id INTEGER PRIMARY KEY AUTOINCREMENT";
		for (int i = 1; i <= features.length; ++i) {
			// escaping the id is important, due to the fact that we named the row that way (e.g. "1")
			sql += ", \"" + i + "\" FLOAT";
		}
		sql += ");";

		try {
			stmt = this.connection.createStatement();

			// objects table creation
			stmt.executeUpdate(sql);

			stmt.close();

			prepStmt = this.connection.prepareStatement("INSERT INTO Features VALUES(NULL, ?, ?, 0, 1);");

			// add all insertions to the batch
			for (int i = 0; i < features.length; ++i) {
				prepStmt.setString(1, features[i]);
				prepStmt.setBoolean(2, outlierFlags[i]);

				prepStmt.addBatch();
			}

			// perform the transaction
			this.connection.setAutoCommit(false);
			prepStmt.executeBatch();
			this.connection.setAutoCommit(true);

			prepStmt.close();

		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}

	/**
	 * Stores a new subspace.
	 *
	 * @param id
	 *            the id of the subspace
	 * @param featureReference
	 *            the features of the subspace
	 * @param name
	 *            the name of the subspace
	 * @param visible
	 *            the visibility flag of the subspace
	 * @throws DatabaseAccessException
	 *             if the write operation failed at database level
	 */
	public void pushSubspace(int id, int[] featureReference, String name)
			throws DatabaseAccessException {

		PreparedStatement prepStmt = null;

		try {
			prepStmt = this.connection.prepareStatement("INSERT INTO Subspaces VALUES(?, ?, ?);");

			// the id is unique for this subspace
			prepStmt.setInt(1, id);

			// add all insertions to the batch
			for (int featureId : featureReference) {
				prepStmt.setInt(2, featureId);
				prepStmt.setString(3, name);

				prepStmt.addBatch();
			}

			// perform the transaction
			this.connection.setAutoCommit(false);
			prepStmt.executeBatch();
			this.connection.setAutoCommit(true);

			prepStmt.close();

		} catch (SQLException e) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}

	/**
	 * Returns the database path.
	 *
	 * @return path to database
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Updates min/max values of all features
	 *
	 * @throws DatabaseAccessException
	 *             if there was a sql exception
	 */
	public void updateFeaturesMinMax() throws DatabaseAccessException {
		Statement stmt;
		ResultSet rs;

		try {
			stmt = this.connection.createStatement();
			rs = stmt.executeQuery("SELECT Id FROM Features;");
			ArrayList<Integer> featureIds = new ArrayList<Integer>();
			while (rs.next()) {
				featureIds.add(rs.getInt("Id"));
			}
			stmt.close();

			for (Integer id : featureIds) {
				stmt = this.connection.createStatement();
				rs = stmt.executeQuery("SELECT MIN(\"" + id + "\"), MAX(\"" + id + "\") FROM Objects;");
				float min = rs.getFloat(1);
				float max = rs.getFloat(2);
				rs.close();
				stmt.close();

				PreparedStatement prepStmt = this.connection
						.prepareStatement("UPDATE Features SET Min=?, Max=? WHERE Id=?");
				prepStmt.setFloat(1, min);
				prepStmt.setFloat(2, max);
				prepStmt.setInt(3, id);
				prepStmt.execute();
				prepStmt.close();
			}
		} catch (SQLException ex) {
			throw new DatabaseAccessException(Failure.WRITE);
		}
	}
}