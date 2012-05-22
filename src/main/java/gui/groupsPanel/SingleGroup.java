package gui.groupsPanel;

import gui.bsvComponents.BSVComboBox;
import gui.bsvComponents.BSVSlider;
import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import controller.Constraint;
import controller.Feature;
import controller.Group;
import controller.GroupController;
import controller.SelectionController;
import controller.StaticConstraint;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * Represents a single group in the groups panel. Shows the group name, status, constraints and offers implementation
 * for the actions and functions of a single group.
 */
public class SingleGroup extends JPanel {

	private static final long serialVersionUID = 739326032396441224L;

	/**
	 * Name of the Group.
	 */
	private JTextField groupName;

	/*
	 * All actions used in this class.
	 */
	private NewConstraintAction newConstraintAction;
	private DeleteGroupAction deleteGroupAction;
	private ChangeGroupStatus changeGroupStatus;
	private ChangeColorAction changeColorAction;
	private ShowNotesAction showNotesAction;
	private AddSelectionAction addSelectionAction;
	private OkColorAction okColorAction;
	private CancelColorAction cancelColorAction;

	/**
	 * The color chooser dialog.
	 */
	private GroupColorChooser groupColorChooser;

	/**
	 * Reference to the Group class.
	 */
	private final Group group;

	/*
	 * All controllers used in this class.
	 */
	private final GroupController groupController;
	private final SubspaceController subspaceController;
	private final SelectionController selectionController;

	/*
	 * Different Swing components used in this class.
	 */
	private JCheckBox activate;
	private JButton add;
	private JButton deleteButton;
	private JButton addSelection;
	private JButton colorButton;
	private JButton notesButton;
	private JPanel innerPanel;

	/**
	 * Maximum dimension constant
	 */
	private static final Dimension MAXDIM = new Dimension(1200, 40);

	/**
	 * Minimum dimension constant
	 */
	private static final Dimension MINDIM = new Dimension(190, 40);

	/**
	 * Reference to the GroupsPanel.
	 */
	private final GroupsPanel groupsPanel;
	
	/**
	 * JPanel containing all buttons in a single group.
	 */
	private JPanel downPanel;
	
	/**
	 * The color of this group.
	 */
	private Color groupColor;

	/**
	 * ResourceBundle for the Strings.
	 */
	private final ResourceBundle rb;

	/**
	 *
	 * Constructor
	 *
	 * @param groupController
	 *            Reference to the GroupController.
	 * @param selectionController
	 *            Reference to the SelectionController.
	 * @param subspaceController
	 *            Reference to the SubspaceCOntroller.
	 * @param g
	 *            The group being shown.
	 * @param gp
	 *            Reference to the parent component (GroupsPanel).
	 */
	public SingleGroup(GroupController groupController, SelectionController selectionController,
			SubspaceController subspaceController, Group g, GroupsPanel gp) {
		if (groupController == null || selectionController == null || subspaceController == null || g == null
				|| gp == null) {
			throw new IllegalArgumentException("Controller cannot be null");
		}
		this.group = g;
		this.groupController = groupController;
		this.selectionController = selectionController;
		this.subspaceController = subspaceController;
		this.groupsPanel = gp;
		this.rb = Settings.getInstance().getResourceBundle();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

		initActions();
		initUpperPart();

		initInnerPanel();
		initDownPanel();
		initInitialConstraints();
	}

	/**
	 * Deletes a single constraint panel from the single group panel.
	 *
	 * @param toDelete
	 *            The panel to be deleted.
	 */
	protected void deleteConstraint(SingleConstraintPanel toDelete) {
		this.innerPanel.remove(toDelete);
		this.groupsPanel.validate();
	}

	/**
	 * Deletes a static constraint(selection).
	 *
	 * @param toDelete
	 *            The panel to be deleted
	 */
	protected void deleteSelection(SingleStaticConstraint toDelete) {
		this.innerPanel.remove(toDelete);
		this.groupsPanel.validate();
	}

	/**
	 * Adds the constraints that were already in the database for this group
	 */
	private void initInitialConstraints() {
		Constraint[] allConstraints = this.group.getConstraints();

		for (int i = 0; i < allConstraints.length; i++) {
			if (allConstraints[i] instanceof controller.DynamicConstraint) {
				SingleConstraintPanel current = new SingleConstraintPanel(this.subspaceController, this.group,
						allConstraints[i], this);
				this.innerPanel.add(current);
			}
			if (allConstraints[i] instanceof controller.StaticConstraint) {
				SingleStaticConstraint current = new SingleStaticConstraint(this.selectionController, this.group,
						(StaticConstraint) allConstraints[i], this);
				this.innerPanel.add(current);
			}
		}
		this.groupsPanel.validate();
	}

	/**
	 * Makes it possible to change the group name.
	 */
	private void enableNameChange() {
		this.groupName.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					groupName.setEditable(true);
					groupName.setFocusable(true);
					groupName.selectAll();
				}
			}
		});

		KeyListener keyListener = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						if (groupName.getText() != null && groupName.getText().length() > 0) {
							group.setName(groupName.getText());
						} else {
							groupName.setText(group.getName());
						}
						groupsPanel.validate();
						groupName.setEditable(false);
						groupName.setFocusable(false);
						groupName.validate();
					} catch (DatabaseAccessException ex) {
						JOptionPane.showMessageDialog(groupsPanel, Settings.getInstance().getResourceBundle()
								.getString("databaseGroupNameWriteFailed"), Settings.getInstance().getResourceBundle()
								.getString("databaseExceptionRead"), JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyTyped(KeyEvent arg0) {

			}
		};
		this.groupName.addKeyListener(keyListener);
		this.addFocusListener(new NameFocusListener());

	}

	/**
	 * Sets properties for the delete button.
	 */
	private void initDeleteButton() {
		Dimension deleteButtonDim = new Dimension(20, 20);
		this.deleteButton = new JButton(deleteGroupAction);
		try {
			this.deleteButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/delete_small.png"))));
		} catch (IOException e) {
			this.deleteButton.setText("x");
		}
		this.deleteButton.setToolTipText(rb.getString("ToolTipDeleteGroup"));
		this.deleteButton.setSize(deleteButtonDim);
		this.deleteButton.setMinimumSize(deleteButtonDim);
		this.deleteButton.setMaximumSize(deleteButtonDim);
		this.deleteButton.setPreferredSize(deleteButtonDim);
		this.deleteButton.validate();
	}

	/**
	 * Initializes the upper panel of a group(the name, the enable/disable button, the delete button).
	 */
	private void initUpperPart() {
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());

		this.groupName = new JTextField();
		this.groupName.setDocument(new TextDoc(this.groupController.maxStringLength()));
		this.groupName.setFont(new Font("Serif", Font.BOLD, 13));
		this.groupName.setMinimumSize(new Dimension(200, 25));
		this.groupName.setPreferredSize(new Dimension(200, 25));
		this.groupName.setOpaque(false);
		this.groupName.setBackground(new Color(0, 0, 0, 0));
		this.groupName.setBorder(BorderFactory.createEmptyBorder());
		this.groupName.setText(group.getName());
		this.groupName.setEditable(false);
		this.groupName.setToolTipText(rb.getString("ToolTipChangeGroupName"));
		enableNameChange();

		// group status button
		this.activate = new JCheckBox(this.changeGroupStatus);
		this.activate.setToolTipText(rb.getString("ToolTipGroupStatus"));
		activate.setSelected(this.group.isVisible());

		// delete group button
		initDeleteButton();

		upperPanel.setMinimumSize(MINDIM);
		upperPanel.setMaximumSize(MAXDIM);

		JPanel upperButtons = new JPanel();
		upperButtons.setLayout(new FlowLayout(FlowLayout.LEADING));

		upperPanel.add(this.groupName, BorderLayout.LINE_START);
		upperButtons.add(activate);
		upperButtons.add(deleteButton);
		upperPanel.add(upperButtons, BorderLayout.LINE_END);
		this.add(upperPanel);

	}

	/**
	 * Initiates the panel where the buttons for this groups are.
	 */
	private void initDownPanel() {
		this.downPanel = new JPanel();
		this.downPanel.setLayout(new BoxLayout(this.downPanel, BoxLayout.PAGE_AXIS));

		this.add = new JButton(this.newConstraintAction);

		try {
			this.add.setLayout(new BorderLayout());
			JLabel plusLabel = new JLabel(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/plus_small.png"))));
			JLabel textLabel = new JLabel(" " + rb.getString("Constraint"));
			this.add.add(plusLabel, BorderLayout.LINE_START);
			this.add.add(textLabel, BorderLayout.CENTER);
		} catch (IOException e) {
			this.add.setText("+ " + rb.getString("Constraint"));
		}

		this.add.setToolTipText(rb.getString("ToolTipAddDynamicConstraint"));

		this.addSelection = new JButton(this.addSelectionAction);
		try {
			this.addSelection.setLayout(new BorderLayout());
			JLabel plusLabel = new JLabel(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/check_16x13.png"))));
			JLabel textLabel = new JLabel(" " + rb.getString("Selection"));
			this.addSelection.add(plusLabel, BorderLayout.LINE_START);
			this.addSelection.add(textLabel, BorderLayout.CENTER);
		} catch (IOException e) {
			this.addSelection.setText("+ " + rb.getString("Selection"));
		}
		this.addSelection.setToolTipText(rb.getString("ToolTipAddStaticConstraint"));

		// the color button
		this.colorButton = new JButton(this.changeColorAction);
		this.groupColor = new Color(this.group.getColor(), true);

		float alpha = this.groupColor.getAlpha() / 255.0f;
		int r = this.groupColor.getRed();
		int g = this.groupColor.getGreen();
		int b = this.groupColor.getBlue();
		Color nc = new Color((int) ((1 - alpha) * 255 + alpha * r), (int) ((1 - alpha) * 255 + alpha * g),
				(int) ((1 - alpha) * 255 + alpha * b));
		this.colorButton.setBackground(nc);

		this.colorButton.setText(rb.getString("GroupColor"));

		this.notesButton = new JButton(this.showNotesAction);
		this.notesButton.setText(rb.getString("GroupNotes"));

		JPanel addButtons = new JPanel();
		addButtons.setLayout(new FlowLayout(FlowLayout.LEADING));

		JPanel otherFunctions = new JPanel();
		otherFunctions.setLayout(new FlowLayout(FlowLayout.LEADING));

		addButtons.setMaximumSize(MAXDIM);
		otherFunctions.setMaximumSize(MAXDIM);

		addButtons.add(add);
		addButtons.add(addSelection);
		otherFunctions.add(colorButton);
		otherFunctions.add(notesButton);

		this.downPanel.add(addButtons);
		this.downPanel.add(otherFunctions);

		this.add(this.downPanel);
		this.groupsPanel.validate();
	}

	/**
	 * Initializes the panel where the constraints are shown.
	 */
	private void initInnerPanel() {
		this.innerPanel = new JPanel();
		this.innerPanel.setLayout(new BoxLayout(this.innerPanel, BoxLayout.Y_AXIS));
		this.add(this.innerPanel);
		this.groupsPanel.validate();
	}

	/**
	 * Initiates the actions for this class.
	 */
	private void initActions() {
		this.newConstraintAction = new NewConstraintAction();
		this.deleteGroupAction = new DeleteGroupAction();
		this.changeGroupStatus = new ChangeGroupStatus();
		this.changeColorAction = new ChangeColorAction();
		this.showNotesAction = new ShowNotesAction();
		this.addSelectionAction = new AddSelectionAction();
		this.okColorAction = new OkColorAction();
		this.cancelColorAction = new CancelColorAction();
	}

	/**
	 * Adds a dynamic constraint to this group.
	 */
	private void addDynamicConstraint() {
		SingleConstraintPanel sc = new SingleConstraintPanel(this.subspaceController, group, null, this);
		this.innerPanel.add(sc);
		this.groupsPanel.revalidate();
	}

	/**
	 * Adds a static constraint to this group.
	 */
	private void addStaticConstraint() {
		try {
			StaticConstraint staticConstraint = this.group.createStaticConstraint(this.selectionController
					.getSelection());
			this.selectionController.reset();
			SingleStaticConstraint c = new SingleStaticConstraint(this.selectionController, this.group,
					staticConstraint, this);
			this.innerPanel.add(c);
			this.groupsPanel.validate();
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Updates the status of this group(enabled/disabled).
	 */
	private void updateGroupStatus() {
		try {
			this.group.setVisible(this.activate.isSelected());
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void openChangeColorDialog() {
		this.groupColorChooser = new GroupColorChooser();
		groupColorChooser.setVisible(true);
	}

	/**
	 * Updates the color of a group. The change is visible in the GroupsPanel and the current view.
	 *
	 * @param newColor
	 *            the new Color.
	 * @param feature
	 *            feature for the color calculation, null if color is static.
	 */
	private void updateGroupColor(Color newColor, Feature feature) {
		try {
			this.group.setColorFeature(feature);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		this.groupColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
		if (this.groupColor == null) {
			this.groupColor = new Color(this.group.getColor());
		} else {
			try {
				this.group.setColor(this.groupColor.getRGB());

				float alpha = this.groupColor.getAlpha() / 255.0f;
				int r = this.groupColor.getRed();
				int g = this.groupColor.getGreen();
				int b = this.groupColor.getBlue();
				Color nc = new Color((int) ((1 - alpha) * 255 + alpha * r), (int) ((1 - alpha) * 255 + alpha * g),
						(int) ((1 - alpha) * 255 + alpha * b));
				this.colorButton.setBackground(nc);
			} catch (DatabaseAccessException e) {
				JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		this.validate();
	}

	private void showNotes() {
		NotesDialog notesDialog = new NotesDialog();
		notesDialog.setVisible(true);
	}

	/**
	 * Defines the new constraint action.
	 */
	class NewConstraintAction extends AbstractAction {

		private static final long serialVersionUID = 399593959102282608L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			addDynamicConstraint();
		}
	}

	private void deleteThis() {
		try {
			this.groupController.removeGroup(this.group);
			this.groupsPanel.deleteGroupFromPanel(this);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Actions when gaining and losing focus on the group name text field.
	 */
	class NameFocusListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent arg0) {
			groupName.selectAll();
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			groupName.setText(group.getName());
			groupName.setEditable(false);
			groupName.validate();
		}

	}

	/**
	 * Defines the delete group action.
	 */
	class DeleteGroupAction extends AbstractAction {

		private static final long serialVersionUID = -2266577405297720469L;

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
	 * Defines the set active/inactive action.
	 */
	class ChangeGroupStatus extends AbstractAction {

		private static final long serialVersionUID = 7658485992873813226L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			updateGroupStatus();
		}
	}

	/**
	 * Defines the change color action.
	 */
	class ChangeColorAction extends AbstractAction {

		private static final long serialVersionUID = -1527621835370361560L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			openChangeColorDialog();
		}
	}

	/**
	 * Defines the show notes action.
	 */
	class ShowNotesAction extends AbstractAction {

		private static final long serialVersionUID = 407124450574538829L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			showNotes();
		}
	}

	/**
	 * Defines the add static constraint action.
	 */
	class AddSelectionAction extends AbstractAction {

		private static final long serialVersionUID = -7037015268811724816L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectionController.getSelectedCount() > 0) {
				addStaticConstraint();
			} else {
				JOptionPane.showMessageDialog(null, rb.getString("NoSelectionError"), rb.getString("error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Defines the ok color action.
	 */
	class OkColorAction extends AbstractAction {

		private static final long serialVersionUID = 5702893363836531558L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			updateGroupColor(groupColorChooser.getColor(), groupColorChooser.getCalcWay());
			groupColorChooser.setVisible(false);
		}
	}

	/**
	 * Defines the cancel color action.
	 */
	class CancelColorAction extends AbstractAction {

		private static final long serialVersionUID = 6968886079077162729L;

		/**
		 * Defines what happens when this action is triggered.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			groupColorChooser.setVisible(false);
		}
	}

	/**
	 * Color chooser of a single group.
	 *
	 */
	class GroupColorChooser extends JDialog implements ChangeListener {

		private static final long serialVersionUID = 2674457929282791102L;

		private static final int ALFA_MIN = 0;
		private static final int ALFA_MAX = 255;

		private final JColorChooser colorChooser;
		private JPanel calcWay;
		private JComboBox features;
		private JButton okColor;
		private JButton cancelColor;
		private JPanel buttonsPanel;
		private JPanel sliderPanel;
		private JSlider slider;
		private final Color startColor;

		/**
		 * Constructor.
		 */
		public GroupColorChooser() {
			super();
			this.setLayout(new BorderLayout());
			this.setModal(true);
			this.setSize(460, 450);
			this.setResizable(false);
			this.setLocationRelativeTo(colorButton);
			this.startColor = new Color(group.getColor(), true);

			colorChooser = new JColorChooser(startColor);
			colorChooser.getSelectionModel().addChangeListener(this);

			AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
			for (int i = 1; i < panels.length; i++) {
				colorChooser.removeChooserPanel(panels[i]);
			}

			initCalcWay();

			initScroller();
			initButtonsPanel();

			JPanel upperColorPanel = new JPanel(new BorderLayout());
			upperColorPanel.add(calcWay, BorderLayout.PAGE_START);
			upperColorPanel.add(this.sliderPanel, BorderLayout.PAGE_END);

			this.add(buttonsPanel, BorderLayout.PAGE_END);
			this.add(upperColorPanel, BorderLayout.PAGE_START);
			this.add(colorChooser, BorderLayout.CENTER);
		}

		private void initScroller() {
			this.sliderPanel = new JPanel();
			this.sliderPanel.setLayout(new BorderLayout());
			this.sliderPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("ColorTransparency")));
			this.slider = new BSVSlider(JSlider.HORIZONTAL, ALFA_MIN, ALFA_MAX, startColor.getAlpha());
			this.slider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();

					Color c = colorChooser.getColor();
					colorChooser.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), source.getValue()));
				}
			});
			this.slider.setValue(255);
			this.slider.setValue(startColor.getAlpha());
			this.sliderPanel.add(slider, BorderLayout.CENTER);
		}

		private void initButtonsPanel() {
			okColor = new JButton(okColorAction);
			cancelColor = new JButton(cancelColorAction);
			okColor.setText(rb.getString("okButton"));
			cancelColor.setText(rb.getString("cancelButton"));
			Dimension buttonDim = new Dimension(120, 25);
			okColor.setMinimumSize(buttonDim);
			cancelColor.setMaximumSize(buttonDim);
			okColor.setMaximumSize(buttonDim);
			cancelColor.setMaximumSize(buttonDim);
			buttonsPanel = new JPanel();

			buttonsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

			buttonsPanel.add(okColor);
			buttonsPanel.add(cancelColor);
		}

		private void initCalcWay() {
			try {
				// calcWay
				calcWay = new JPanel();
				calcWay.setLayout(new BorderLayout());
				calcWay.setBorder(BorderFactory.createTitledBorder(rb.getString("ColorCalcText")));
				Feature[] f = subspaceController.getActiveSubspace().getFeatures();
				features = new BSVComboBox(f);
				features.insertItemAt(rb.getString("calcWay"), 0);
				if (group.getColorFeature() == null) {
					features.setSelectedIndex(0);
				} else {
					features.setSelectedItem(group.getColorFeature());
				}
				calcWay.add(features, BorderLayout.CENTER);

			} catch (DatabaseAccessException e) {
				JOptionPane.showMessageDialog(null, rb.getString("databaseSubspaceReadFailed"),
						rb.getString("databaseExceptionRead"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		/**
		 * Returns the chosen color.
		 *
		 * @return the color
		 */
		protected Color getColor() {
			return colorChooser.getColor();
		}

		/**
		 * Returns the chosen calculation way.
		 *
		 * @return the calculation way
		 */
		protected Feature getCalcWay() {
			if (features.getSelectedIndex() == 0) {
				return null;
			}
			return (Feature) features.getSelectedItem();
		}

		/**
		 * New Color picked.
		 *
		 * @param e
		 *            the event
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			Color c = colorChooser.getColor();
			colorChooser.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), this.slider.getValue()));
		}
	}

	/**
	 * Notes dialog.
	 *
	 */
	class NotesDialog extends JDialog {

		private static final long serialVersionUID = -4147285885193353852L;

		/**
		 * Constructor.
		 */
		public NotesDialog() {

			init();
		}

		/**
		 * Initializes the notes dialog of a group.
		 */
		public final void init() {

			setLayout(new BorderLayout());

			JLabel label = new JLabel(rb.getString("GroupDescription"));
			add(label, BorderLayout.PAGE_START);

			final JTextArea text = new JTextArea(group.getDescription());
			text.setAlignmentX(0.5f);
			text.setLineWrap(true);
			text.setWrapStyleWord(true);

			JScrollPane scp = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(scp, BorderLayout.CENTER);

			JButton close = new JButton(rb.getString("Cancel"));
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});

			JButton save = new JButton(rb.getString("Save"));
			save.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					try {
						group.setDescription(text.getText());
					} catch (DatabaseAccessException e) {
						e.printStackTrace();
					}
					dispose();
				}
			});
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			buttonsPanel.add(save);
			buttonsPanel.add(close);
			add(buttonsPanel, BorderLayout.PAGE_END);

			setTitle(rb.getString("NotesDialogTitle"));
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocationRelativeTo(groupsPanel);
			setSize(400, 400);
			this.setResizable(false);
		}
	}

	/**
	 * Used to restrain the number of symbols in a text field.
	 *
	 */
	class TextDoc extends PlainDocument {

		private static final long serialVersionUID = -12772649569020456L;

		private final int maxLength;

		/**
		 * Sets the maximal length of a TextDoc.
		 *
		 * @param maxLength
		 *            The maximal length.
		 */
		public TextDoc(int maxLength) {
			this.maxLength = maxLength;
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str.length() == 0) {
				return;
			}
			if (getLength() + str.length() < maxLength) {
				super.insertString(offs, str, a);
			}
		}
	}

}
