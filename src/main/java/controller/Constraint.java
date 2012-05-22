package controller;

import db.DatabaseAccessException;

/**
 * A {@code Constraint} describes a set of points.
 */
public interface Constraint {

	/**
	 * Activates this {@code Constraint}, determining whether the {@code Constraint} is used to filter all points. It
	 * updates the value in the {@link Database}, too.
	 * 
	 * @param active
	 *            Activates or deactivates this {@code Constraint}
	 * @throws DatabaseAccessException
	 *             if the write operation in {@link Database}
	 */
	void setActive(boolean active) throws DatabaseAccessException;

	/**
	 * Returns the unique identifier of this {@code Constraint}.
	 * 
	 * @return the identifier
	 */
	int getId();

	/**
	 * Returns if this {@code Constraint} is active.
	 * 
	 * @return True, if the {@code Constraint} is active.
	 */
	boolean isActive();

	/**
	 * Deletes this {@code Constraint} from the {@link Database}.
	 * 
	 * @throws DatabaseAccessException
	 *             if the write operation failed in {@link Database}.
	 */
	void remove() throws DatabaseAccessException;

}
