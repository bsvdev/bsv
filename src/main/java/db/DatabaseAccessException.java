package db;

import util.Failure;

/**
 * The class {@code DatabaseAccessException} is used to indicate an access error in the data tier.
 */
public class DatabaseAccessException extends Exception {

	/**
	 * UID used for serialization
	 */
	private static final long serialVersionUID = 2227924042681420480L;

	/**
	 * Constructs an {@code DatabaseAccessException}, used to indicate an access error in the data tier.
	 */
	public DatabaseAccessException() {

	}

	/**
	 * Constructs an {@code DatabaseAccessException}, used to indicate an access error in the data tier.
	 *
	 * @param msg
	 *            the message of the exception
	 */
	public DatabaseAccessException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an {@code DatabaseAccessException}, used to indicate an access error in the data tier.
	 *
	 * @param fail
	 *            the failure of the exception
	 */
	public DatabaseAccessException(Failure fail) {
		super(fail.toString());
	}
}
