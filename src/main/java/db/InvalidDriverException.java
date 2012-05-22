package db;

import util.Failure;

/**
 * The class {@code InvalidDriverException} is used to indicate an invalid database driver.
 */
public class InvalidDriverException extends Exception {

	/**
	 * UID used for serialization
	 */
	private static final long serialVersionUID = 3670563391892111821L;

	/**
	 * Constructs an new {@code InvalidDriverException}, used to indicate an invalid database driver.
	 */
	public InvalidDriverException() {

	}

	/**
	 * Constructs an new {@code InvalidDriverException}, used to indicate an invalid database driver.
	 *
	 * @param msg
	 *            the message of the exception
	 */
	public InvalidDriverException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an new {@code InvalidDriverException}, used to indicate an invalid database driver.
	 *
	 * @param fail
	 *            the failure of the exception
	 */
	public InvalidDriverException(Failure fail) {
		super(fail.toString());
	}
}
