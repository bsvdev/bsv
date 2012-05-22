package db;

import util.Failure;

/**
 * The class {@code IncompatibleVersionException} is used to indicate a old database layout.
 */
public class IncompatibleVersionException extends Exception {

	/**
	 * UID used for serialization
	 */
	private static final long serialVersionUID = -495637367408978849L;

	/**
	 * Constructs an {@code IncompatibleVersionException}, used to indicate a old database layout.
	 */
	public IncompatibleVersionException() {

	}

	/**
	 * Constructs an {@code IncompatibleVersionException}, used to indicate a old database layout.
	 *
	 * @param msg
	 *            the message of the exception
	 */
	public IncompatibleVersionException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an {@code IncompatibleVersionException}, used to indicate a old database layout.
	 *
	 * @param fail
	 *            the failure of the exception
	 */
	public IncompatibleVersionException(Failure fail) {
		super(fail.toString());
	}
}
