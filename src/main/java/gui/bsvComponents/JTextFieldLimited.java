package gui.bsvComponents;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * The class represents a {@link JTextField} with a maximum number of symbols to enter into the field.
 */
public class JTextFieldLimited extends JTextField {
	private static final long serialVersionUID = -8522713013854692376L;

	/**
	 * The constructor of a JTextField with a restricted number of symbols.
	 * 
	 * @param limit
	 *            the maximum length of the string
	 */
	public JTextFieldLimited(int limit) {
		super();
		this.setDocument(new LimitedDocument(limit));
	}

	/**
	 * The class represents a {@link PlainDocument} with a limited size.
	 */
	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = -5157864138154909358L;

		/**
		 * The limited size.
		 */
		private final int limit;

		/**
		 * Constructs a new limited document.
		 * 
		 * @param limit
		 *            the maximum length
		 */
		public LimitedDocument(int limit) {
			super();
			this.limit = limit;
		}

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null) {
				throw new IllegalArgumentException("str may not be null");
			}

			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}
	}
}
