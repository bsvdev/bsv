package gui.subspacePanel;

import gui.bsvComponents.BSVComboBox;
import gui.main.EventController;
import gui.settings.Settings;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import controller.effectiveoutlierness.Calculation;
import controller.Subspace;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * This class creates a component where the user can see the subspaces and change the current one.
 */
public class SubspacePanel extends JPanel implements Observer {
	private static final long serialVersionUID = 19454764367419915L;

	/**
	 * Reference to the SubspaceController.
	 */
	private final SubspaceController subspaceController;

	/**
	 * A list with all subspaces.
	 */
	private Subspace[] subspaces;

	/**
	 * Button to show the detail dialog for features and subspaces.
	 */
	private JButton btnShowFeatureSubspaceDialog;

	/**
	 * Button to show the subspace dialog.
	 */
	private JButton btnSubspaceSelectDialog;

	/**
	 * The dialog to show detailed information to the Features and Subspaces.
	 */
	private JDialog featureSubspaceDialog;

	/**
	 * The dialog frame to choose a new subspace.
	 */
	private JDialog subspaceFrame;

	/**
	 * The actual dialog to select a new subspace.
	 */
	private SubspaceChooseDialog chooseDialog;

	/**
	 * The section to show the active subspace.
	 */
	private final JTextField lblActiveSubspace;

	/**
	 * Constructs a new subspace panel.
	 *
	 * @param subspaceController
	 *            Reference to the SubspaceController.
	 */
	public SubspacePanel(SubspaceController subspaceController) {
		if (subspaceController == null) {
			throw new IllegalArgumentException("SubspaceController was null");
		}
		// init the subspace panel
		this.subspaceController = subspaceController;
		this.subspaces = new Subspace[0];
		this.subspaceController.addObserver(this);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		this.featureSubspaceDialog = null;

		try {
			this.featureSubspaceDialog = new FeatureSubspaceDialog(subspaceController.getSubspaces());
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseSubspaceReadFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
		}

		initOpenFeatureSubspaceDialogButton();
		initOpenSubspaceDialogButton();

		// init the active subspace label
		lblActiveSubspace = new JTextField();
		lblActiveSubspace.setEditable(false);
		lblActiveSubspace.setText(subspaceController.getActiveSubspace().getName());
		lblActiveSubspace.setToolTipText(lblActiveSubspace.getText());
		this.add(lblActiveSubspace);

		updateSubspaces();
		initSubspaceDialog();

		initEffectiveOutliernessCalculation();

		EventController.getInstance().registerKeyTarget(chooseDialog);
	}

	/**
	 * Initializes the combo box to select the effective outlierness calculation.
	 */
	private void initEffectiveOutliernessCalculation() {
		Calculation[] calculations = subspaceController.getAllCalculations();
		BSVComboBox effectiveOutlierness = new BSVComboBox(calculations);
		effectiveOutlierness.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// select a new calculation
				subspaceController.setCalculateEffectiveOutliernessBy((Calculation) e.getItem());
			}

		});

		this.add(effectiveOutlierness);
	}

	/**
	 * Initialize the button to open the dialog.
	 */
	private void initOpenFeatureSubspaceDialogButton() {
		this.btnShowFeatureSubspaceDialog = new JButton();

		Dimension buttonMinSize = new Dimension(56, 28);
		Dimension buttonMaxSize = new Dimension(80, 28);

		// tooltip
		this.btnShowFeatureSubspaceDialog.setToolTipText(Settings.getInstance().getResourceBundle()
				.getString("ToolTipShowFeatureSubspaceDetail"));

		// alignment
		this.btnShowFeatureSubspaceDialog.setHorizontalAlignment(SwingConstants.CENTER);

		// load image or fallback to text
		try {
			this.btnShowFeatureSubspaceDialog.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/feature_subspace_dialog.png"))));

		} catch (IOException e) {
			// now you don't have icons
		}

		// size of button
		this.btnShowFeatureSubspaceDialog.setPreferredSize(buttonMinSize);
		this.btnShowFeatureSubspaceDialog.setMaximumSize(buttonMaxSize);

		Action openFeatureSubspaceDialogAction = new OpenFeatureSubspaceDialog();
		this.btnShowFeatureSubspaceDialog.addActionListener(openFeatureSubspaceDialogAction);

		EventController.getInstance().setAction(openFeatureSubspaceDialogAction, "eventOpenFeatureSubspaceDialog");
		this.add(btnShowFeatureSubspaceDialog);
	}

	/**
	 * Initialize the button to open the dialog.
	 */
	private void initOpenSubspaceDialogButton() {
		this.btnSubspaceSelectDialog = new JButton();

		Dimension buttonMinSize = new Dimension(170, 28);
		Dimension buttonMaxSize = new Dimension(200, 28);

		// tooltip
		this.btnSubspaceSelectDialog.setToolTipText(Settings.getInstance().getResourceBundle()
				.getString("changeSubspace"));

		// text
		this.btnSubspaceSelectDialog.setText(Settings.getInstance().getResourceBundle().getString("changeSubspace"));

		// alignment
		this.btnSubspaceSelectDialog.setHorizontalAlignment(SwingConstants.LEFT);

		// load image or fallback to text
		try {
			this.btnSubspaceSelectDialog.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/subspace_change.png"))));

		} catch (IOException e) {
			// now you don't have icons
		}

		// size of button
		this.btnSubspaceSelectDialog.setPreferredSize(buttonMinSize);
		this.btnSubspaceSelectDialog.setMaximumSize(buttonMaxSize);

		Action openSubspaceDialogAction = new OpenSubspaceDialogAction();
		this.btnSubspaceSelectDialog.addActionListener(openSubspaceDialogAction);

		EventController.getInstance().setAction(openSubspaceDialogAction, "eventOpenSubspaceChooser");
		this.add(btnSubspaceSelectDialog);
	}

	/**
	 * Initialize the dialog frame to choose the subspace.
	 */
	private void initSubspaceDialog() {
		this.subspaceFrame = new JDialog(EventController.getInstance().getRootFrame());
		this.subspaceFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.subspaceFrame.setUndecorated(true);
		this.subspaceFrame.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent arg0) {
				if (subspaceFrame.isVisible()) {
					btnSubspaceSelectDialog.doClick();
				}
			}

			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				// nothing to do
			}
		});

		this.chooseDialog = new SubspaceChooseDialog(this.subspaceFrame, this.subspaceController);
		chooseDialog.updateView(subspaces);
		this.subspaceFrame.getContentPane().add(this.chooseDialog);
	}

	/**
	 * Action performed to show the choose dialog.
	 */
	private void openDialog() {
		// resize the frame
		subspaceFrame.setMinimumSize(new Dimension(300, 300));
		subspaceFrame.setMaximumSize(new Dimension(EventController.getInstance().getRootFrame().getWidth() - 80,
				EventController.getInstance().getRootFrame().getHeight() - 80));
		// update the view and pack the components
		chooseDialog.updateView(subspaces);
		subspaceFrame.pack();
		subspaceFrame.setLocation(EventController.getInstance().getRootFrame().getX() + 40, EventController
				.getInstance().getRootFrame().getY()
				+ (EventController.getInstance().getRootFrame().getHeight() - subspaceFrame.getHeight() - 40));
		subspaceFrame.setVisible(true);
	}

	/**
	 * Method to update the list of subspaces and the view.
	 */
	private void updateSubspaces() {
		try {
			this.subspaces = subspaceController.getSubspaces();
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseSubspaceReadFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		lblActiveSubspace.setText(subspaceController.getActiveSubspace().getName());
		lblActiveSubspace.setToolTipText(lblActiveSubspace.getText());
	}

	@Override
	public void update(Observable o, Object arg) {
		updateSubspaces();
	}

	/**
	 * The class is used to handle the action of opening the subspace choose dialog.
	 */
	class OpenSubspaceDialogAction extends AbstractAction {
		private static final long serialVersionUID = -2020176651017529313L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (subspaceFrame.isVisible()) {
				subspaceFrame.setVisible(false);
			} else {
				openDialog();
			}
		}
	}

	/**
	 * The class is used to handle the action of opening the feature and subspace dialog.
	 */
	class OpenFeatureSubspaceDialog extends AbstractAction {
		private static final long serialVersionUID = -7376989465703546580L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			featureSubspaceDialog.validate();
			featureSubspaceDialog.setVisible(true);
		}
	}
}
