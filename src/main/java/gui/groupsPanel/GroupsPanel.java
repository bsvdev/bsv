package gui.groupsPanel;

import gui.main.EventController;
import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.Group;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * The Panel where information about groups is visualised. Enables the user to do operations on existing groups or to
 * create such.
 * 
 */
public class GroupsPanel extends JPanel {


	private static final long serialVersionUID = 3777729076386241842L;
	
	/**
	 * Reference to the GroupController.
	 */
	private final GroupController groupController;
	/**
	 * Reference to the SelectionController.
	 */
	private final SelectionController selectionController;
	/**
	 * Reference to the SubspaceController.
	 */
	private final SubspaceController subspaceController;

	private AddGroupAction addGroupAction;
	
	/*
	 * Different Swing components used in this class.
	 */
	private JLabel groupTitle;
	private JButton newGroupButton;
	private JPanel mainPanel;
	private JScrollPane scroller;
	private JPanel upperPanel;
	
	/**
	 * Speed of the jscrollpane.
	 */
	private final static int SCROLL_SPEED = 16;
	/**
	 * ResourceBundle for the Strings.
	 */
	private final ResourceBundle rb;

	/**
	 * Constructor
	 * 
	 * @param groupController
	 *            reference to the GroupController.
	 * @param selectionController
	 *            reference to the SelectionController.
	 * @param subspaceController
	 *            reference to the SubspaceController.
	 */
	public GroupsPanel(GroupController groupController, SelectionController selectionController,
			SubspaceController subspaceController) {

		if (groupController == null || selectionController == null || subspaceController == null) {
			throw new IllegalArgumentException("Controller cannot be null");
		}
		this.groupController = groupController;
		this.selectionController = selectionController;
		this.subspaceController = subspaceController;
		this.rb = Settings.getInstance().getResourceBundle();

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
		initMainPanel();
		initActions();
		initScrollPane();
		initUpperComponent();
		updateGroups();
	}

	private void initScrollPane() {
		if (this.scroller != null) {
			this.remove(this.scroller);
		}
		this.scroller = new JScrollPane(this.mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scroller.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		this.add(scroller, BorderLayout.CENTER);
	}

	private void initMainPanel() {
		this.mainPanel = new JPanel();
		this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
	}

	/**
	 * Updates the title of the GroupsPanel. Used when a group is added or deleted, for example.
	 */
	private void updateTitle() {
		try {
			this.groupTitle.setText(rb.getString("GroupPanelTitle") + " " + this.groupController.getGroups().length);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseGroupReadFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void initUpperComponent() {
		groupTitle = new JLabel();
		Font titleFont = new Font(this.groupTitle.getFont().getName(), Font.BOLD, this.groupTitle.getFont().getSize());
		this.groupTitle.setFont(titleFont);
		updateTitle();
		newGroupButton = new JButton(this.addGroupAction);
		this.newGroupButton.setText(rb.getString("AddNewGroupWithoutPlus"));
		try {
			this.newGroupButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/plus_smallest.png"))));

		} catch (IOException e) {
			newGroupButton.setText(rb.getString("AddNewGroup"));
		}
		this.upperPanel = new JPanel();
		this.upperPanel.setLayout(new BoxLayout(this.upperPanel, BoxLayout.X_AXIS));
		this.upperPanel.add(groupTitle);
		this.upperPanel.add(Box.createHorizontalGlue());
		this.upperPanel.add(newGroupButton);
		this.add(upperPanel, BorderLayout.PAGE_START);
	}

	private void initActions() {
		this.addGroupAction = new AddGroupAction();
		EventController.getInstance().setAction(this.addGroupAction, "eventAddNewGroup");
	}

	/**
	 * Creates and adds a SingleGroupPanel(!) to the GroupsPanel.
	 * 
	 * @param g
	 *            the group, null if creating a new group.
	 */
	private void addGroup(Group g) {
		String groupName = rb.getString("InitialGroupName");
		try {
			Group newGroup;
			if (g == null) {
				newGroup = groupController.createGroup(groupName);
			} else {
				newGroup = g;
			}

			SingleGroup sg = new SingleGroup(groupController, selectionController, subspaceController, newGroup, this);
			updateTitle();
			this.mainPanel.add(sg);
			this.validate();
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseGroupCreationFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a single group from the panel.
	 * 
	 * @param sg
	 *            the single group panel to be deleted.
	 */
	protected void deleteGroupFromPanel(SingleGroup sg) {
		updateTitle();
		this.mainPanel.remove(sg);
		this.validate();
		refreshScroller();
	}

	private void refreshScroller() {
		this.scroller.setVisible(false);
		this.scroller.setVisible(true);
	}

	/**
	 * Used once upon loading the groups at the start. Gets and adds all existing groups from the database.
	 */
	private void updateGroups() {
		try {
			Group[] allGroups = this.groupController.getGroups();
			for (int i = 0; i < allGroups.length; i++) {
				// NONE OF THE GROUPS SHOULD BE NULL!
				if (allGroups[i] != null) {
					this.addGroup(allGroups[i]);
				}
			}
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseGroupReadFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Defines the add group action.
	 */
	class AddGroupAction extends AbstractAction {

		private static final long serialVersionUID = 8681117452559508024L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			addGroup(null);
		}
	}
}
