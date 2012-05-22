package importexport.util;

/**
 * {@link InvalidFileException} indicates that a file with errors is tried to be imported.
 */
public class InvalidFileException extends RuntimeException {

	/**
	 * serialVersionUID of this Exception.
	 */
	private static final long serialVersionUID = -1233341812338414304L;

	/**
	 * Creates a new instance of <code>InvalidFileException</code> without detail message.
	 */
	public InvalidFileException() {
		super();
	}

	/**
	 * Constructs an instance of <code>InvalidFileException</code> with the specified detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public InvalidFileException(final String msg) {
		super(msg);
	}
}
