package gui.groupsPanel;

import gui.main.EventController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;

/**
 * The class is used to show and hide the entire groups dialog
 */
public class ShowGroups extends JPanel {

	private static final long serialVersionUID = 91691881553546489L;
	
	/**
	 * shows/hides the GroupsPanel.
	 */
	private final JButton showButton;
	
	/**
	 * Instance of the GroupsPanel.
	 */
	private final GroupsPanel groupsPanel;
	
	/**
	 * Show/hide action.
	 */
	private final ShowHideAction showHideAction;
	
	/*
	 * Icons used in this class.
	 */
	private ImageIcon ARROW_RIGHT;
	private ImageIcon ARROW_LEFT;

	/**
	 * Constructor of a new {@code ShowGroups}
	 * 
	 * @param groupController
	 *            reference to the {@link GroupController}
	 * @param selectionController
	 *            reference to the {@link SelectionController}
	 * @param subspaceController
	 *            reference to the {@link SubspaceController}
	 */
	public ShowGroups(GroupController groupController, SelectionController selectionController,
			SubspaceController subspaceController) {

		if (groupController == null || selectionController == null || subspaceController == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		this.setLayout(new BorderLayout());

		try {
			ARROW_RIGHT = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/arrow_right.png")));
			ARROW_LEFT = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/arrow_left.png")));
		} catch (IOException e) {
			// show the button without icons
		}

		this.groupsPanel = new GroupsPanel(groupController, selectionController, subspaceController);
		this.showHideAction = new ShowHideAction();
		this.showButton = new JButton(this.showHideAction);
		this.showButton.setIcon(ARROW_LEFT);

		this.showButton.setMinimumSize(new Dimension(20, 300));
		this.showButton.setMaximumSize(new Dimension(20, 300));
		this.showButton.setPreferredSize(new Dimension(20, 300));

		this.add(this.groupsPanel, BorderLayout.CENTER);
		this.add(this.showButton, BorderLayout.LINE_START);

		EventController.getInstance().setAction(new ShowHideAction(), "eventShowHideGroupsPanel");

	}
	
	/**
	 * Shows/hides the GroupsPanel.
	 */
	private void showHide() {
		if (this.groupsPanel.isVisible()) {
			this.groupsPanel.setVisible(false);
			this.showButton.setIcon(ARROW_RIGHT);
		} else {
			this.groupsPanel.setVisible(true);
			this.showButton.setIcon(ARROW_LEFT);
		}

	}

	/**
	 * Defines the show/hide action.
	 */
	class ShowHideAction extends AbstractAction {

		private static final long serialVersionUID = -3358458202550928581L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			showHide();
		}
	}
}
