package importexport.util;

/**
 * {@code UnsupportedFileExtensionException} is used to indicate that a user is trying to import an unsupported type of
 * files.
 */
public class UnsupportedFileExtensionException extends RuntimeException {

	/**
	 * serialVersionUID of this Exception.
	 */
	private static final long serialVersionUID = 472154721689825584L;

	/**
	 * Constructs an new UnsupportedFileExtensionException.
	 */
	public UnsupportedFileExtensionException() {
		super();
	}

	/**
	 * Constructs an new UnsupportedFileExtensionException.
	 * 
	 * @param msg
	 *            Concrete error message.
	 */
	public UnsupportedFileExtensionException(final String msg) {
		super(msg);
	}
}
