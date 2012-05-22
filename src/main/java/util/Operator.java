package util;

/**
 * The {@code Operator} defines the mode of the comparison in a {@code DynamicConstraint}.
 */
public enum Operator {
	/**
	 * =
	 */
	EQUAL,

	/**
	 * !=
	 */
	NOT_EQUAL,

	/**
	 * &lt
	 */
	LESS,

	/**
	 * &lt=
	 */
	LESS_OR_EQUAL,

	/**
	 * &gt;
	 */
	GREATER,

	/**
	 * &gt;=
	 */
	GREATER_OR_EQUAL
}