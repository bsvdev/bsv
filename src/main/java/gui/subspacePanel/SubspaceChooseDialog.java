package gui.subspacePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import controller.Subspace;
import controller.SubspaceController;

/**
 * The dialog, shown to select or search for a new subspace.
 */
public class SubspaceChooseDialog extends JPanel {
	private static final long serialVersionUID = 2297719658462819379L;

	/**
	 * The parent frame in which this dialog is shown.
	 */
	private final JDialog parentFrame;

	/**
	 * The {@link SubspaceController} to get the subspaces from.
	 */
	private final SubspaceController subspaceController;

	/**
	 * A list with all existing subspaces.
	 */
	private Subspace[] subspaces;

	/**
	 * A list with the filtered subspaces.
	 */
	private Subspace[] visibleSubspaces;

	/**
	 * The text field to enter the search query.
	 */
	private final JTextField search;

	/**
	 * The list, visualized in the UI.
	 */
	private final JList subspaceList;

	/**
	 * Constructor of a new choose dialog.
	 * 
	 * @param parentFrame
	 *            the dialog frame this dialog belongs to
	 * @param subspaceController
	 *            the {@link SubspaceController} to get the subspaces from
	 */
	public SubspaceChooseDialog(final JDialog parentFrame, final SubspaceController subspaceController) {
		if (parentFrame == null || subspaceController == null) {
			throw new IllegalArgumentException("parentFrame or subspaceController was null");
		}
		// init the dialog
		this.parentFrame = parentFrame;
		this.subspaceController = subspaceController;
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(0xff99d1f4));
		this.setVisible(true);
		search = new JTextField("");
		subspaceList = new JList();
		subspaceList.setBackground(new Color(0xff99d1f4));
		subspaceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		subspaceList.setLayoutOrientation(JList.VERTICAL);

		subspaceList.addKeyListener(new java.awt.event.KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				actKeyEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// nothing to do
			}
		});

		subspaceList.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (subspaceList.getSelectedIndex() >= 0) {
					subspaceController.setActiveSubspace(visibleSubspaces[subspaceList.getSelectedIndex()]);
					parentFrame.setVisible(false);
				}
			}
		});

		search.addKeyListener(new java.awt.event.KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				actKeyEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				updateList();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// If the up or down arrow keys are pressed, jump out of the text field and move the selection
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					subspaceList.setSelectedIndex(subspaceList.getSelectedIndex() + 1);
					subspaceList.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					subspaceList.setSelectedIndex(subspaceList.getSelectedIndex() - 1);
					subspaceList.requestFocusInWindow();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(subspaceList);
		this.add(search, BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * The methods updates the internal list of subspaces and updates all displays.
	 * 
	 * @param subspaces
	 *            the new list of subspaces
	 */
	public void updateView(Subspace[] subspaces) {
		if (subspaces == null) {
			throw new IllegalArgumentException("Subspaces was null");
		}
		this.subspaces = subspaces;
		this.subspaceList.setListData(subspaces);
		this.subspaceList.setVisibleRowCount(15);
		search.selectAll();
		search.requestFocusInWindow();

		updateList();
	}

	/**
	 * This method determines the pressed key and acts to it.
	 * 
	 * @param e
	 *            the released key event
	 */
	private void actKeyEvent(KeyEvent e) {
		switch (e.getKeyChar()) {
		case KeyEvent.VK_ENTER:
			// select a new active subspace
			if (subspaceList.getSelectedIndex() >= 0) {
				this.subspaceController.setActiveSubspace(visibleSubspaces[subspaceList.getSelectedIndex()]);
				this.parentFrame.setVisible(false);
			}
			break;
		case KeyEvent.VK_ESCAPE:
			// exit the dialog
			this.parentFrame.setVisible(false);
			break;
		default:
			break;
		}
	}

	/**
	 * Instant Search
	 * 
	 * This method updates the shown list of subspaces, checking the search query.
	 */
	private void updateList() {
		// split the search query to get single words
		String[] searchQuery = search.getText().toLowerCase().trim().split("[\\s]");
		ArrayList<Subspace> newList = new ArrayList<Subspace>();

		// iterate the subspaces and check if it reaches the search query
		for (Subspace current : subspaces) {
			boolean found = true;

			// Check single search queries, if the subspace name contains it
			for (String currentSearch : searchQuery) {
				if (!current.toString().toLowerCase().contains(currentSearch)) {
					// if one query is not contained, exit the loop
					found = false;
					break;
				}
			}

			// the name contains all queries, so add it to the result list
			if (found) {
				newList.add(current);
			}
		}

		// copy the ArrayList to an Array and update the dialog
		Subspace[] newListArray = new Subspace[newList.size()];
		newList.toArray(newListArray);

		this.subspaceList.setListData(newListArray);
		visibleSubspaces = newListArray;

		// move the selection to the currently active subspace, if it is still visible
		subspaceList.setSelectedIndex(0);
		for (int i = 0; i < visibleSubspaces.length; i++) {
			if (subspaceController.getActiveSubspace().equals(visibleSubspaces[i])) {
				subspaceList.setSelectedIndex(i);
			}
		}
	}
}
