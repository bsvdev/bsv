package controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

/**
 * This class is used to test the class {@link SelectionController}
 */
public class SelectionControllerTest {

	/**
	 * Test the basic selection and deselection
	 */
	@Test
	public void initialSelectionTest() {
		SelectionController selectionController = new SelectionController();
		int[] selection = { 1, 2, 3, 4, 5, 6 };
		int[] emptySelecion = {};

		// do a selection
		selectionController.select(selection);
		assertArrayEquals("Selection was different", selectionController.getSelection(), selection);
		assertTrue("There was something selected", selectionController.isSomethingSelected());
		assertEquals("Incorrect count of elements", 6, selectionController.getSelectedCount());

		// reset the selection
		selectionController.reset();
		assertArrayEquals("Selection was different", selectionController.getSelection(), emptySelecion);
		assertFalse("There was nothing selected", selectionController.isSomethingSelected());
		assertEquals("Incorrect count of elements", 0, selectionController.getSelectedCount());
	}

	/**
	 * Test the unselection and reselection
	 */
	@Test
	public void furtherSelectionTest() {
		SelectionController selectionController = new SelectionController();
		int[] selection = { 1, 2, 3, 4, 5, 6 };
		int[] unSelecion = { 1, 2, 3 };
		int[] resultSelection = { 4, 5, 6 };
		int[] negativeSelection = { -1 };

		// select something and unselect parts of it
		selectionController.select(selection);
		assertArrayEquals("Selection was different", selection, selectionController.getSelection());
		selectionController.unselect(unSelecion);
		assertArrayEquals("Selection was different after unselection", resultSelection,
				selectionController.getSelection());

		// do a reselection
		selectionController.reselect(unSelecion);
		assertArrayEquals("Selection was different after reselection", unSelecion, selectionController.getSelection());

		assertTrue("Item 1 is selected", selectionController.isSelected(1));
		assertTrue("Item 2 is selected", selectionController.isSelected(2));
		assertTrue("Item 3 is selected", selectionController.isSelected(3));
		assertFalse("Item 4 is  not selected", selectionController.isSelected(4));
		assertFalse("Item 5 is  not selected", selectionController.isSelected(5));
		assertFalse("Item 6 is  not selected", selectionController.isSelected(6));

		// unselect all elements
		selectionController.unselect(unSelecion);
		assertFalse("Nothing was selected", selectionController.isSomethingSelected());
		assertEquals("The array should have length zero", 0, selectionController.getSelection().length);

		// select something negative
		selectionController.select(negativeSelection);
		assertFalse("Nothing should be selected", selectionController.isSomethingSelected());
		assertEquals("The array should have length zero after selection", 0, selectionController.getSelection().length);

		// reselect something negative
		selectionController.select(selection);
		selectionController.reselect(negativeSelection);
		assertFalse("Nothing should be reselected", selectionController.isSomethingSelected());
		assertEquals("The array should have length zero after reselection", 0,
				selectionController.getSelection().length);

	}

	/**
	 * Test its methods for illegal arguments
	 */
	@Test
	public void argumentTest() {
		SelectionController selectionController = new SelectionController();
		try {
			selectionController.select(null);
			Assert.fail("The new selection was null");
		} catch (NullPointerException e) {
		}

		try {
			selectionController.unselect(null);
			Assert.fail("The new unselection was null");
		} catch (NullPointerException e) {
		}

		try {
			selectionController.reselect(null);
			Assert.fail("The new reselection was null");
		} catch (NullPointerException e) {
		}
	}
}
