package importexport.util;

/**
 * Utility class with some useful functions.
 */
public final class Utility {

	/**
	 * Private constructor
	 */
	private Utility() {
		throw new AssertionError();
	}

	/**
	 * Proofs if the transmitted string is a valid representation of a floating point number.
	 * 
	 * @param s
	 *            transmitted string.
	 * @return returns true if {@code s} is valid representation of a floating point number.
	 */
	public static boolean isFloat(final String s) {
		boolean isFloat = true;

		try {
			Float.parseFloat(s);
		} catch (NumberFormatException e) {
			isFloat = false;
		}

		return isFloat;
	}

	/**
	 * Filters the used delimiter in the data segment of a csv based file.
	 * 
	 * @param s
	 *            String from the delimiter should be filtered.
	 * @return the delimiter if the line contains one, otherwise (char) 0;
	 */
	public static char filterDelimiterFromString(final String s) {
		char tmp;
		char ret = 0;

		for (int i = 0; i < s.length(); ++i) {
			tmp = s.charAt(i);

			if (tmp == ',' || tmp == ';' || tmp == ':' || tmp == ' ' || tmp == '\t') {
				ret = tmp;
				break;
			}
		}

		return ret;
	}
}
