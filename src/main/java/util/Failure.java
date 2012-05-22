package util;

/**
 * The {@code Failure} defines potential low level failures, we can't handle locally.
 */
public enum Failure {
	/**
	 * Indicates broken connection.
	 */
	CONNECTION,

	/**
	 * Indicates invalid driver.
	 */
	DRIVER,

	/**
	 * Indicates inability to set a configuration.
	 */
	CONFIG,

	/**
	 * Indicates inability to create the layout.
	 */
	LAYOUT,

	/**
	 * Indicates invalid version of the database layout.
	 */
	VERSION,

	/**
	 * Indicates failed read operation.
	 */
	READ,

	/**
	 * Indicates failed write operation.
	 */
	WRITE
}
