package gui.subspacePanel;

import gui.bsvComponents.JTextFieldLimited;
import gui.main.EventController;
import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import controller.Feature;
import controller.Subspace;
import db.DatabaseAccessException;

/**
 * The dialog shows detailed information about all {@link Feature}s and allows the user to rename them.
 */
public class FeatureSubspaceDialog extends JDialog {
	private static final long serialVersionUID = -8762294916340189399L;

	/**
	 * A list with all existing subspaces.
	 */
	private final Subspace[] allSubspaces;

	/**
	 * A list with the shown single feature panels.
	 */
	private final ArrayList<SingleFeature> allFeaturesInUI;

	/**
	 * Button to accept and change the new values.
	 */
	private JButton btnChange;

	/**
	 * Button to dismiss all changes.
	 */
	private JButton btnCancel;

	/**
	 * The constructor of a new feature-subspace detail dialog.
	 * 
	 * @param allSubspaces
	 *            a list with all existing subspaces
	 */
	public FeatureSubspaceDialog(Subspace[] allSubspaces) {
		super(EventController.getInstance().getRootFrame(), true);

		if (allSubspaces == null) {
			throw new IllegalArgumentException("allSubspaces was null");
		}

		// init the dialog
		this.allSubspaces = allSubspaces;
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setResizable(false);
		this.setMinimumSize(new Dimension(400, 600));
		this.getContentPane().setLayout(new BorderLayout());

		// Initialize both panels within the tabs
		this.allFeaturesInUI = new ArrayList<FeatureSubspaceDialog.SingleFeature>();

		// create the panels
		JPanel outlierPanel = initPanel(true);
		JPanel featurePanel = initPanel(false);

		// make them scrollable
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		JScrollPane scrollPaneFeature = new JScrollPane(featurePanel);
		scrollPaneFeature.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollPane scrollPaneOutlier = new JScrollPane(outlierPanel);
		scrollPaneOutlier.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab(Settings.getInstance().getResourceBundle().getString("feature"), scrollPaneFeature);
		tabbedPane.addTab(Settings.getInstance().getResourceBundle().getString("outlierness"), scrollPaneOutlier);

		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		// Initialize the buttons and add a simple key listener
		initButtons();

		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				// nothing to do
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// nothing to do
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					btnChange.doClick();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					btnCancel.doClick();
				}
			}
		});
	}

	/**
	 * Exit and hide the dialog.
	 */
	private void exit() {
		this.setVisible(false);
	}

	/**
	 * The method initializes the panels with all features or outlierness.
	 * 
	 * @param outlier
	 *            true, if you want to build the outlier
	 * @return the new created panel
	 */
	private JPanel initPanel(boolean outlier) {
		JPanel featurePanel = new JPanel();
		featurePanel.setLayout(new BoxLayout(featurePanel, BoxLayout.Y_AXIS));

		Feature[] allFeatures = new Feature[0];

		// get all features
		try {
			allFeatures = allSubspaces[0].getFeatures();
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(this,
					Settings.getInstance().getResourceBundle().getString("databaseFeatureReadFailed"), Settings
							.getInstance().getResourceBundle().getString("databaseExceptionRead"),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		for (int i = 0; i < allFeatures.length; i++) {
			// Add all required features to single panels
			if ((outlier && allFeatures[i].isOutlier() && !allFeatures[i].isVirtual())
					|| (!outlier && !allFeatures[i].isOutlier() && !allFeatures[i].isVirtual())) {

				SingleFeature current = new SingleFeature(allFeatures[i]);
				featurePanel.add(current);
				this.allFeaturesInUI.add(current);
			}
		}

		return featurePanel;
	}

	/**
	 * Initializes the buttons at the bottom of the dialog.
	 */
	private void initButtons() {
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setAlignmentX(SwingConstants.RIGHT);

		// the button change; use the new entries
		this.btnChange = new JButton(Settings.getInstance().getResourceBundle().getString("change"));
		this.btnChange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (SingleFeature current : allFeaturesInUI) {
					current.update();
				}

				exit();
			}
		});
		buttonPanel.add(this.btnChange);

		// the button cancel; dismiss the entries
		this.btnCancel = new JButton(Settings.getInstance().getResourceBundle().getString("cancel"));
		this.btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (SingleFeature current : allFeaturesInUI) {
					current.reset();
				}

				exit();
			}
		});
		buttonPanel.add(this.btnCancel);

		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * This panel represents one {@link Feature} within the UI.
	 */
	class SingleFeature extends JPanel {
		private static final long serialVersionUID = -4538231871119511998L;

		/**
		 * The {@link Feature} referring to this {@code SingleFeature}.
		 */
		private final Feature feature;

		/**
		 * The text field to set the name.
		 */
		private final JTextFieldLimited featureName;

		/**
		 * Flag to indicate, that the name has changed and has to be updated.
		 */
		private boolean hasChanged;

		/**
		 * Constructs a new single feature.
		 * 
		 * @param feature
		 *            the referring {@link Feature}
		 */
		public SingleFeature(final Feature feature) {
			super();

			// init the single feature
			this.feature = feature;
			this.hasChanged = false;
			this.setLayout(new FlowLayout());
			this.add(new JLabel(Settings.getInstance().getResourceBundle().getString("name") + ": "));
			this.featureName = new JTextFieldLimited(feature.maxStringLength());
			this.featureName.setColumns(feature.maxStringLength() / 4 * 3);
			this.featureName.setText(feature.getName());

			this.featureName.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					// nothing to do
				}

				@Override
				public void focusGained(FocusEvent arg0) {
					// flag, to write only the changed features in database
					hasChanged = true;
				}
			});

			this.featureName.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent arg0) {
					// nothing to do
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					String tmp = featureName.getText();

					// check for a valid name
					if (!tmp.matches(".*(\\p{Punct}+|\\p{Space}).*")) {
						featureName.setForeground(Color.black);
						btnChange.setEnabled(true);
					} else {
						featureName.setForeground(Color.red);
						btnChange.setEnabled(false);
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						btnChange.doClick();
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						btnCancel.doClick();
					}
				}
			});

			this.add(this.featureName);
		}

		/**
		 * The method updates the name of this {@link Feature}, if it has changed.
		 */
		public void update() {
			if (hasChanged) {
				setFeatureName(featureName.getText());
			}

			hasChanged = false;
		}

		/**
		 * This method resets the text field to the old name of the feature.
		 */
		public void reset() {
			if (hasChanged) {
				featureName.setText(feature.getName());
			}
		}

		/**
		 * This method updates the name of this feature in the database.
		 * 
		 * @param name
		 *            the new name
		 */
		private void setFeatureName(String name) {
			if (name == null) {
				throw new IllegalArgumentException("name may not be null");
			}

			if (name.length() > 0) {
				try {
					feature.setName(name.trim());
				} catch (DatabaseAccessException e) {
					JOptionPane.showMessageDialog(this,
							Settings.getInstance().getResourceBundle().getString("databaseFeatureNameWriteFailed"),
							Settings.getInstance().getResourceBundle().getString("databaseExceptionWrite"),
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					e.printStackTrace();
				}
			}
		}
	}
}
