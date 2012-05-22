package controller;

import java.util.HashSet;
import java.util.Observable;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The class {@code SelectionController} manages the current selection from the UI. You can select several points, reset
 * your selection or request a list of all currently selected points.
 */
public class SelectionController extends Observable {
	/**
	 * Stores current selection.
	 */
	private HashSet<Integer> selection = null;

	/**
	 * Stores if something is selected.
	 */
	private boolean somethingSelected;

	/**
	 * Constructs a new SelectionController with empty selection.
	 */
	public SelectionController() {
		this.reset();
	}

	/**
	 * Selects the given {@code elements} and saves it.
	 *
	 * @param elements
	 *            The selection.
	 */
	public void select(int[] elements) {
		if (elements == null) {
			throw new NullPointerException("Elements can't be null!");
		}

		this.addToSelection(elements);
	}

	/**
	 * Unselects the given {@code elements} and saves it.
	 *
	 * @param elements
	 *            The unselection.
	 */
	public void unselect(int[] elements) {
		if (elements == null) {
			throw new NullPointerException("Elements can't be null!");
		}

		for (int i = 0; i < elements.length; i++) {
			this.selection.remove(elements[i]);
		}

		if (this.selection.isEmpty()) {
			this.somethingSelected = false;
		}

		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Resets the current selection, selects the given {@code elements} and saves it.
	 *
	 * @param elements
	 *            The selection.
	 */
	public void reselect(int[] elements) {
		if (elements == null) {
			throw new NullPointerException("Elements can't be null!");
		}

		this.selection = new HashSet<Integer>((int) (elements.length * 1.2f), 1.f);
		this.somethingSelected = false;

		this.addToSelection(elements);
	}

	/**
	 * Dismisses the currently saved selection.
	 */
	public void reset() {
		if (this.selection == null) {
			this.selection = new HashSet<Integer>();
		} else {
			this.selection.clear();
		}

		this.somethingSelected = false;

		this.setChanged();
		this.notifyObservers(this);
	}

	/**
	 * Returns the currently saved selection.
	 *
	 * @return The selection.
	 */
	public int[] getSelection() {
		Integer[] tmp = new Integer[this.selection.size()];
		this.selection.toArray(tmp);

		return ArrayUtils.toPrimitive(tmp);
	}

	/**
	 * Returns how many objects are selected.
	 *
	 * @return count of selected objects.
	 */
	public int getSelectedCount() {
		return selection.size();
	}

	/**
	 * Checks if there are elements selected.
	 *
	 * @return {@code true} if something is selected, {@code false} otherwise.
	 */
	public boolean isSomethingSelected() {
		return this.somethingSelected;
	}

	/**
	 * Checks if a specified element is selected.
	 *
	 * @param id
	 *            id of the element to check.
	 * @return {@code true} if element is selected, {@code false} otherwise.
	 */
	public boolean isSelected(int id) {
		return this.selection.contains(id);
	}

	/**
	 * Adds given elemnts to selection
	 *
	 * @param elements
	 *                  ids of elements, that should be selected
	 */
	private void addToSelection(int[] elements) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] > 0) {
				this.selection.add(elements[i]);
				this.somethingSelected = true;
			}
		}

		this.setChanged();
		this.notifyObservers();
	}
}
