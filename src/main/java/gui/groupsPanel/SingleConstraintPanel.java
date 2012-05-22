package gui.groupsPanel;

import gui.bsvComponents.BSVComboBox;
import gui.settings.Settings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import util.Operator;
import controller.Constraint;
import controller.DynamicConstraint;
import controller.Feature;
import controller.Group;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * Implements a single static constraint panel. Gives the user the ability to
 * change, enable, disable or delete the constraint.
 */
public class SingleConstraintPanel extends JPanel {

	private static final long serialVersionUID = 860191389614348893L;

	/*
	 * Swing components used in this class.
	 */
	private JTextField value;
	private JComboBox featureChoice;
	private JComboBox operatorChoice;
	private JCheckBox status;
	private JButton deleteButton;

	/**
	 * The dynamic constraint.
	 */
	private DynamicConstraint constraint;

	/**
	 * Reference to the SubspaceController.
	 */
	private final SubspaceController subspaceController;

	/**
	 * Reference to the group.
	 */
	private final Group group;

	/*
	 * Actions
	 */
	private DeleteConstraintAction deleteConstraintAction;
	private ChangeConstraintStatus changeConstraintStatus;
	private FeatureActionListener featureChanged;
	private OperatorActionListener operatorChanged;
	private ValueActionListener valueChanged;

	/**
	 * Reference to the SingleGroup
	 */
	private final SingleGroup singleGroup;

	/**
	 * Dimension constants.
	 */
	private static final Dimension MAXDIM = new Dimension(1200, 40);
	private static final Dimension MINDIM = new Dimension(190, 40);

	/**
	 * ResourceBundle for the Strings.
	 */
	private final ResourceBundle rb;

	/**
	 * Formatter for parsing float values
	 */
	private final NumberFormat formatter;

	/**
	 * Constructor
	 * 
	 * @param subspaceController
	 *            reference to the SubspaceController.
	 * @param group
	 *            the Group.
	 * @param dc
	 *            reference to the DynamicConstraint.
	 * 
	 * @param sg
	 *            reference to the SingleGroup.
	 */
	public SingleConstraintPanel(SubspaceController subspaceController,
			Group group, Constraint dc, SingleGroup sg) {

		if (subspaceController == null || group == null || sg == null) {
			throw new IllegalArgumentException("Controller cannot be null");
		}
		this.group = group;
		this.singleGroup = sg;
		this.constraint = (DynamicConstraint) dc;
		this.subspaceController = subspaceController;
		this.rb = Settings.getInstance().getResourceBundle();
		this.setLayout(new FlowLayout(FlowLayout.LEADING));

		this.setMinimumSize(MINDIM);
		this.setMaximumSize(MAXDIM);
		this.formatter = NumberFormat.getNumberInstance(Settings.getInstance()
				.getLanguage());

		initConstraint();
		synchronize();
	}

	/**
	 * If constraint was not null, initializes the values of the corresponding
	 * fields.
	 */
	private void synchronize() {
		if (this.constraint != null) {
			this.featureChoice.setSelectedItem(this.constraint.getFeature());

			this.operatorChoice.setSelectedItem(this
					.operatorToString(this.constraint.getOperator()));
			this.status.setSelected(this.constraint.isActive());
			this.value
					.setText(this.formatter.format(this.constraint.getValue()));
		}
	}

	/**
	 * Provides the Strings for the operator list.
	 * 
	 * @param op
	 *            the operator.
	 * @return the corresponding String.
	 */
	private String operatorToString(Operator op) {
		String s;
		switch (op) {
		case EQUAL:
			s = "=";
			break;
		case NOT_EQUAL:
			s = "!=";
			break;
		case LESS:
			s = "<";
			break;
		case LESS_OR_EQUAL:
			s = "<=";
			break;
		case GREATER:
			s = ">";
			break;
		case GREATER_OR_EQUAL:
			s = ">=";
			break;
		default:
			s = "invalid";
			break;
		}
		return s;
	}

	private void initButtons() {
		Dimension deleteButtonDim = new Dimension(20, 20);
		this.deleteButton = new JButton(deleteConstraintAction);

		try {
			this.deleteButton.setIcon(new ImageIcon(ImageIO.read(this
					.getClass().getResourceAsStream("/delete_small.png"))));
		} catch (IOException e) {
			this.deleteButton.setText("x");
		}

		this.deleteButton.setSize(deleteButtonDim);
		this.deleteButton.setMinimumSize(deleteButtonDim);
		this.deleteButton.setMaximumSize(deleteButtonDim);
		this.deleteButton.setPreferredSize(deleteButtonDim);
		this.deleteButton.validate();
	}

	private void initConstraint() {
		try {
			initListeners();
			initActions();
			initButtons();

			Feature[] features = this.subspaceController.getSubspaces()[0].getFeatures();
			
			int countNotVirtual = 0;
			for(int i = 0; i < features.length; i++) {
				if (!features[i].isVirtual()) {
					countNotVirtual++;
				}
			}
			Feature[] featuresWithoutVirtuals = new Feature[countNotVirtual];
			for(int i = 0, counter = 0; i < features.length; i++) {
				if (!features[i].isVirtual()) {
					featuresWithoutVirtuals[counter++] = features[i];
				}
			}
			
			this.featureChoice = new BSVComboBox(featuresWithoutVirtuals);
			this.featureChoice.addActionListener(this.featureChanged);

			String[] s = new String[Operator.values().length];
			int i = 0;
			for (Operator current : Operator.values()) {
				s[i++] = this.operatorToString(current);
			}

			this.operatorChoice = new BSVComboBox(s);
			this.operatorChoice.addActionListener(this.operatorChanged);

			this.value = new JTextField();
			this.value.setText("");
			value.setDocument(new TextDoc(10));
			this.value.setMinimumSize(new Dimension(85, 25));
			this.value.setPreferredSize(new Dimension(85, 25));
			this.value.addActionListener(this.valueChanged);

			this.status = new JCheckBox(this.changeConstraintStatus);
			if (this.constraint != null) {
				this.status.setSelected(this.constraint.isActive());
			} else {
				this.status.setSelected(false);
				this.status.setEnabled(false);
			}
			addTooltips();

			addComponents();

		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null, rb.getString("databaseExceptionRead"),
					rb.getString("databaseSubspaceReadFailed"), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void addTooltips() {
		this.status
				.setToolTipText(this.rb.getString("ToolTipConstraintStatus"));
		this.deleteButton.setToolTipText(this.rb
				.getString("ToolTipDeleteConstraint"));
	}

	private void initActions() {
		this.deleteConstraintAction = new DeleteConstraintAction();
		this.changeConstraintStatus = new ChangeConstraintStatus();
	}

	private void addComponents() {
		this.add(this.featureChoice);
		this.add(this.operatorChoice);
		this.add(this.value);
		this.add(this.status);
		this.add(this.deleteButton);
		this.validate();
	}

	/**
	 * Updates the properties of a constraint.
	 * 
	 * @param action
	 *            indicates what is being updated.
	 */
	private void updateConstraint(char action) {
		float val = this.getSelectedValue();
		this.value.setText(this.formatter.format(val));

		if (this.constraint == null) {
			try {
				this.constraint = this.group.createDynamicConstraint(
						this.getSelectedFeature(), this.getSelectedOperator(),
						val);
				this.status.setEnabled(true);
				this.status.setSelected(this.constraint.isActive());
			} catch (DatabaseAccessException e) {
				JOptionPane.showMessageDialog(null,
						rb.getString("DatabaseWriteError"),
						rb.getString("error"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		} else {
			try {
				if (action == 'f') {
					this.constraint.setFeature(this.getSelectedFeature());
				}
				if (action == 'o') {
					this.constraint.setOperator(this.getSelectedOperator());
				}
				if (action == 'v') {
					this.constraint.setValue(val);
				}
				this.value.setFocusable(false);
				this.value.setFocusable(true);
			} catch (DatabaseAccessException e) {
				JOptionPane.showMessageDialog(null,
						rb.getString("DatabaseWriteError"),
						rb.getString("error"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

	}

	private void initListeners() {
		this.featureChanged = new FeatureActionListener();
		this.operatorChanged = new OperatorActionListener();
		this.valueChanged = new ValueActionListener();
	}

	private void deleteThis() {
		try {
			if (this.constraint != null) {
				this.group.removeConstraint(this.constraint);
			}
			this.singleGroup.deleteConstraint(this);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null,
					rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Returns the selected feature.
	 * 
	 * @return a feature.
	 */
	private Feature getSelectedFeature() {
		return (Feature) featureChoice.getSelectedItem();
	}

	/**
	 * Returns the selected Operator.
	 * 
	 * @return an operator.
	 */
	private Operator getSelectedOperator() {
		for (Operator current : Operator.values()) {
			if (this.operatorToString(current).equals(
					operatorChoice.getSelectedItem())) {
				return current;
			}
		}
		return null;
	}

	/**
	 * Returns the selected Value.
	 * 
	 * @return a float representing the value.
	 */
	private float getSelectedValue() {
		try {
			return this.formatter.parse(value.getText()).floatValue();
		} catch (ParseException e) {
			return (float) 0.0;
		}
	}

	private void changeStatus() {
		try {
			if (this.constraint != null) {
				this.constraint.setActive(this.status.isSelected());
			}
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null,
					rb.getString("DatabaseWriteError"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Feature changed.
	 * 
	 */
	class FeatureActionListener implements ActionListener {

		/**
		 * Action triggered.
		 * 
		 * @param evt
		 *            the event
		 */
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (value.getText().length() > 0) {
				updateConstraint('f');
			}
		}
	}

	/**
	 * Operator changed.
	 * 
	 */
	class OperatorActionListener implements ActionListener {

		/**
		 * Action triggered.
		 * 
		 * @param evt
		 *            the event
		 */
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (value.getText().length() > 0) {
				updateConstraint('o');
			}
		}
	}

	/**
	 * Value changed.
	 * 
	 */
	class ValueActionListener implements ActionListener {

		/**
		 * Action triggered.
		 * 
		 * @param evt
		 *            the event
		 */
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (value.getText().length() > 0) {
				updateConstraint('v');
			}
		}
	}

	/**
	 * Defines the delete constraint action.
	 */
	class DeleteConstraintAction extends AbstractAction {

		private static final long serialVersionUID = 8295888203795540876L;

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
	 * Used to restrain the number of symbols in a text field.
	 * 
	 */
	class TextDoc extends PlainDocument {

		private static final long serialVersionUID = 3532684139324670212L;
		private final int maxLength;

		/**
		 * Constructor
		 * 
		 * @param maxLength
		 *            the max string length for the text
		 */
		public TextDoc(int maxLength) {
			this.maxLength = maxLength;
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			if (str.length() == 0) {
				return;
			}
			if (getLength() + str.length() < maxLength) {
				super.insertString(offs, str, a);
			}
		}
	}

	/**
	 * Defines the change status action.
	 */
	class ChangeConstraintStatus extends AbstractAction {

		private static final long serialVersionUID = -2651079710854433407L;

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