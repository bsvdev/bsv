package gui.groupsPanel;

import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Group;
import controller.SelectionController;
import controller.StaticConstraint;
import db.DatabaseAccessException;

/**
 * Implements a single static constraint panel. Adds and shows a selection as constraint.
 * 
 */
public class SingleStaticConstraint extends JPanel {

	private static final long serialVersionUID = -7773691472288702531L;
	
	/**
	 * Reference to the group.
	 */
	private final Group group;
	
	/**
	 * Reference to the single group(UI).
	 */
	private final SingleGroup singleGroup;
	
	/**
	 * the constraint.
	 */
	private final StaticConstraint constraint;

	/*
	 * Actions
	 */
	private ChangeConstraintStatus changeConstraintStatus;
	private DeleteConstraintAction deleteConstraintAction;

	/*
	 * Swing components used in this class.
	 */
	private final JLabel name;
	private final JCheckBox status;
	private JButton deleteButton;
	
	/**
	 * ResourceBundle for the Strings.
	 */
	private final ResourceBundle rb;
	
	/*
	 * Dimension constants.
	 */
	private static final Dimension MAXDIM = new Dimension(1200, 40);
	private static final Dimension MINDIM = new Dimension(190, 40);

	/**
	 * Constructor.
	 * 
	 * @param selectionController
	 *            Reference to the SelectionController.
	 * @param group
	 *            Reference to the Group to which this constraint belongs.
	 * @param constraint
	 *            Reference to a constraint. When this parameter is null, then a new constraint is added, otherwise the
	 *            constraint is initialized according to this parameter.
	 * @param singleGroup
	 *            Reference to the SingleGroup where this panel is shown.
	 */
	public SingleStaticConstraint(SelectionController selectionController, Group group, StaticConstraint constraint,
			SingleGroup singleGroup) {
		if (selectionController == null || group == null || constraint == null || singleGroup == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		this.group = group;
		this.singleGroup = singleGroup;

		initAction();
		initButtons();
		this.rb = Settings.getInstance().getResourceBundle();
		this.constraint = constraint;
		this.setMinimumSize(MINDIM);
		this.setMaximumSize(MAXDIM);

		this.name = new JLabel("  " + rb.getString("staticConstraint"));

		this.status = new JCheckBox(this.changeConstraintStatus);
		if (this.constraint != null) {
			this.status.setSelected(this.constraint.isActive());
		} else {
			this.status.setSelected(true);
		}

		addComponents();
	}

	private void addComponents() {
		this.setLayout(new BorderLayout());

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		buttonsPanel.add(this.status);
		buttonsPanel.add(this.deleteButton);

		this.add(this.name, BorderLayout.LINE_START);
		this.add(buttonsPanel, BorderLayout.CENTER);
	}

	private void initButtons() {
		Dimension deleteButtonDim = new Dimension(20, 20);
		this.deleteButton = new JButton(deleteConstraintAction);

		try {
			this.deleteButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/delete_small.png"))));
		} catch (IOException e) {
			this.deleteButton.setText("x");
		}

		this.deleteButton.setSize(deleteButtonDim);
		this.deleteButton.setMinimumSize(deleteButtonDim);
		this.deleteButton.setMaximumSize(deleteButtonDim);
		this.deleteButton.setPreferredSize(deleteButtonDim);
		this.deleteButton.validate();
	}

	private void initAction() {
		this.deleteConstraintAction = new DeleteConstraintAction();
		this.changeConstraintStatus = new ChangeConstraintStatus();
	}

	/**
	 * Changes the status of the constraint.
	 */
	private void changeStatus() {
		try {
			if (this.constraint != null) {
				this.constraint.setActive(this.status.isSelected());
			}
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Deletes this constraint.
	 */
	private void deleteThis() {
		try {
			this.group.removeConstraint(this.constraint);
			this.singleGroup.deleteSelection(this);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Defines the delete constraint action.
	 */
	class DeleteConstraintAction extends AbstractAction {

		private static final long serialVersionUID = -4392775497941115954L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			deleteThis();
		}
	}

	/**
	 * Defines the change status action.
	 */
	class ChangeConstraintStatus extends AbstractAction {

		private static final long serialVersionUID = -5805086227174847339L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			changeStatus();
		}
	}
}
