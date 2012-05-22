package db;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class {@code InvalidDatabaseTest} tests the exception handling of the database storage system.
 */
public class InvalidDatabaseTest {
	private final String resPath = "src/test/resources/databases/";

	private final String invalidVersionPath = resPath + "InvalidVersion.bsv";
	private final String notReadablePath = resPath + "NotReadable.bsv";
	private final String notWritablePath = resPath + "NotWritable.bsv";

	private Database invalidVersionDatabase = null;
	private Database notReadableDatabase = null;
	private Database notWritableDatabase = null;

	/**
	 * Set up permissions, because we are not able to deliver files without e.g. read permission.
	 */
	@Before
	public void setup() {

		try {
			(new File(notReadablePath)).setReadable(false);
			(new File(notWritablePath)).setWritable(false);
		} catch (NullPointerException e) {
			fail("Illegal file operation.");
		} catch (SecurityException e) {
			fail("Illegal file operation.");
		}
	}

	/**
	 * Clear permissions, because we are not able to deliver files without e.g. read permission.
	 */
	@After
	public void tearDown() {
		try {
			if (invalidVersionDatabase != null) {
				invalidVersionDatabase.shutdown();
			}

			if (notReadableDatabase != null) {
				notReadableDatabase.shutdown();
			}

			if (notWritableDatabase != null) {
				notWritableDatabase.shutdown();
			}
		} catch (DatabaseAccessException e) {
			fail("Unexpected Exception.");
		}

		invalidVersionDatabase = null;
		notReadableDatabase = null;
		notWritableDatabase = null;

		// reset permissions
		try {
			(new File(notReadablePath)).setReadable(true);
			(new File(notWritablePath)).setWritable(true);
		} catch (NullPointerException e) {
			fail("Illegal file operation.");
		} catch (SecurityException e) {
			fail("Illegal file operation.");
		}
	}

	/**
	 * Tests the incompatible layout handling.
	 */
	@Test
	public void invalidVersionTest() {

		try {
			invalidVersionDatabase = new Database(invalidVersionPath);
			fail("Expected incompatible version exception.");
		} catch (IncompatibleVersionException e) {
			// success!
		} catch (InvalidDriverException e) {
			fail("Expected incompatible version exception.");
		} catch (DatabaseAccessException e) {
			fail("Expected incompatible version exception.");
		}
	}

	/**
	 * Tests the inability to establish a connection to the database.
	 */
	@Test
	public void notReadableTest() {

		try {
			notReadableDatabase = new Database(notReadablePath);
		} catch (IncompatibleVersionException e) {
			e.printStackTrace();
		} catch (InvalidDriverException e) {
			e.printStackTrace();
		} catch (DatabaseAccessException e) {
			// success
			if (e.getMessage() != "CONNECTION") {
				fail("Unexpected Eception.");
			}
		}
	}

	/**
	 * Tests the inability to write to the database.
	 */
	@Test
	public void notWritableTest() {
		String[] features = { "dim0", "dim1", "dim2" };
		boolean[] outlierFlags = { false, false, true };

		try {
			notWritableDatabase = new Database(notWritablePath);
			notWritableDatabase.initFeatures(features, outlierFlags);
		} catch (IncompatibleVersionException e) {
			fail("Unexpected Eception.");
		} catch (InvalidDriverException e) {
			fail("Unexpected Eception.");
		} catch (DatabaseAccessException e) {
			// success
			if (e.getMessage() != "WRITE") {
				fail("Unexpected Eception.");
			}
		}
	}
}
