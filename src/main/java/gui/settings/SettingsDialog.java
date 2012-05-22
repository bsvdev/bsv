package gui.settings;

import gui.bsvComponents.BSVComboBox;
import gui.main.MainWindow;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialog for arbitrary user settings (e.g. language).
 */
public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 2627555500399041879L;

	/**
	 * Settings instance.
	 */
	private static final Settings SETTINGS = Settings.getInstance();

	/**
	 * Instance of the resource bundle.
	 */
	private static final ResourceBundle RB = SETTINGS.getResourceBundle();

	/**
	 * Default dimension of the dialog.
	 */
	private static final Dimension DEFAULT_DIM = new Dimension(408, 170);

	/**
	 * Size of the button panel.
	 */
	private static final Dimension BTN_PNL_DIM = new Dimension(378, 76);

	/**
	 * Default size of the dialog buttons.
	 */
	private static final Dimension BUTTON_DIM = new Dimension(108, 28);

	/**
	 * Max size of the dialog buttons.
	 */
	private static final Dimension BUTTON_MAXDIM = new Dimension(128, 28);

	/**
	 * Default size of used combo boxes in this dialog.
	 */
	private static final Dimension DEFAULT_COMBO_SIZE = new Dimension(50, 28);

	/**
	 * Instance of this SettingsDialog.
	 */
	private static SettingsDialog INSTANCE;

	/**
	 * Panel which is used to group all dialog buttons.
	 */
	private final JPanel pnlButtons;

	/**
	 * Label for languages.
	 */
	private JLabel lblLang;

	/**
	 * Combo box which contains all possible languages.
	 */
	private BSVComboBox boxLang;

	/**
	 * Ok button, stores changes and closes after pressing.
	 */
	private JButton btnOk;

	/**
	 * Cancel button, aborts changes and closes dialog after pressing.
	 */
	private JButton btnCncl;

	/**
	 * Reset button, resets changes after pressing.
	 */
	private JButton btnRst;

	/**
	 * Instance of the mainWindow.
	 */
	private final MainWindow mw;

	/**
	 * Constructs a new settings dialog.
	 * 
	 * @param mainWindow
	 *            instance of MainWindow
	 */
	public SettingsDialog(final MainWindow mainWindow) {
		super(mainWindow, true);

		this.setTitle(RB.getString("settings"));
		this.setSize(DEFAULT_DIM);
		this.setPreferredSize(DEFAULT_DIM);
		this.setLayout(null);

		this.initBtns();
		this.initComps();

		this.pnlButtons = new JPanel();
		this.pnlButtons.setSize(BTN_PNL_DIM);
		this.pnlButtons.setPreferredSize(BTN_PNL_DIM);
		this.pnlButtons.add(this.btnOk);
		this.pnlButtons.add(this.btnCncl);
		this.pnlButtons.add(this.btnRst);
		this.pnlButtons.setLocation(14, this.getHeight() - BTN_PNL_DIM.height - 5);

		this.add(pnlButtons);
		this.add(lblLang);
		this.add(boxLang);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(mainWindow);

		this.mw = mainWindow;
		INSTANCE = this;
	}

	/**
	 * Initializes all buttons.
	 */
	private void initBtns() {
		this.btnOk = new JButton(RB.getString("Save"));
		this.btnOk.setSize(BUTTON_DIM);
		this.btnOk.setPreferredSize(BUTTON_DIM);
		this.btnOk.setMaximumSize(BUTTON_MAXDIM);

		this.btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					SETTINGS.setLanguage((Locale) boxLang.getSelectedItem());
					SETTINGS.store(null);
					mw.restartApplication();
				} catch (IOException ex) {
					Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		this.btnCncl = new JButton(RB.getString("Cancel"));
		this.btnCncl.setSize(BUTTON_DIM);
		this.btnCncl.setPreferredSize(BUTTON_DIM);
		this.btnCncl.setMaximumSize(BUTTON_MAXDIM);

		this.btnCncl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				INSTANCE.resetDialog();
				INSTANCE.dispose();
			}
		});

		this.btnRst = new JButton(RB.getString("Reset"));
		this.btnRst.setSize(BUTTON_DIM);
		this.btnRst.setPreferredSize(BUTTON_MAXDIM);
		this.btnRst.setMaximumSize(BUTTON_MAXDIM);

		this.btnRst.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				INSTANCE.resetDialog();
				SETTINGS.reset();
			}
		});
	}

	/**
	 * Initializes all components besides buttons.
	 */
	private void initComps() {
		this.lblLang = new JLabel(RB.getString("Language"));
		this.lblLang.setSize(120, 28);
		this.lblLang.setLocation(20, 14);
		this.boxLang = new BSVComboBox(SETTINGS.getSupportedLanguages());
		this.boxLang.setSelectedItem(SETTINGS.getLanguage());
		this.boxLang.setSize(DEFAULT_COMBO_SIZE);
		this.boxLang.setPreferredSize(DEFAULT_COMBO_SIZE);
		this.boxLang.setLocation(this.getWidth() - this.boxLang.getWidth() - 29, 14);
	}

	/**
	 * Resets all selections.
	 */
	private void resetDialog() {
		this.boxLang.setSelectedItem(SETTINGS.getLanguage());
	}
}
