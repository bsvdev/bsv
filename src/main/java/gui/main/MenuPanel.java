package gui.main;

import gui.settings.Settings;
import gui.settings.SettingsDialog;
import importexport.ExportLogic;
import importexport.ImportLogic;
import importexport.util.InvalidFileException;
import importexport.util.UnsupportedFileExtensionException;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * MenuPanel class holds the most basic functions of the program. Can be used to change current workspace, data file,
 * options. Enables the user to export/import data and to see information about the program.
 */
public class MenuPanel extends JPanel {

	private static final long serialVersionUID = 2913009660116831977L;

	/**
	 * Reference to MainWindow.
	 */
	private final MainWindow mainWindow;

	/**
	 * Button for changing the workspace.
	 */
	private JButton newButton;

	/**
	 * Button for importing a file.
	 */
	private JButton importButton;

	/**
	 * Button for exporting data in a file.
	 */
	private JButton exportButton;

	/**
	 * Button showing information about the program.
	 */
	private JButton infoButton;

	/**
	 * Button showing the settings dialog
	 */
	private JButton settingsButton;

	/**
	 * ResourceBundle for the Strings.
	 */
	private final ResourceBundle rb;

	/**
	 * Defines what is done when changing the workspace.
	 */
	private NewWSAction newWS;

	/**
	 * Defines what is done when importing.
	 */
	private ImportAction importAction;

	/**
	 * Defines what is done when viewing information.
	 */
	private InfoAction infoAction;

	/**
	 * Defines what is done when exporting.
	 */
	private ExportAction exportAction;

	private SettingsAction settingsAction;

	private String path;

	private boolean flag;

	/**
	 * Info dialog
	 */
	private final InfoDialog idialog;

	/**
	 * Constructor.
	 * 
	 * @param mainWindow
	 *            Reference to the MainWindow.
	 */
	public MenuPanel(MainWindow mainWindow) {
		super();
		if (mainWindow == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 8, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.mainWindow = mainWindow;
		this.rb = Settings.getInstance().getResourceBundle();
		flag = true;
		initActions();
		initButtons();
		this.idialog = new InfoDialog(EventController.getInstance().getRootFrame());
		this.add(Box.createHorizontalStrut(6));
		this.add(newButton);
		this.add(importButton);
		this.add(exportButton);
		this.add(settingsButton);
		this.add(infoButton);
	}

	/**
	 * Create new workspace.
	 */
	public void newWorkspace() {
		this.newButton.doClick();
	}

	/**
	 * Loads a new database, starts the import logic if the database is empty and finally loads the project UI.
	 * 
	 * @param path
	 *            the path for the database
	 */
	public void loadDatabase(String path) {
		Database db = null;

		try {
			db = new Database(path);
			MainWindow.curBSVFile = path;
		} catch (InvalidDriverException e2) {
			JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseBadDriver"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
		} catch (IncompatibleVersionException e2) {
			JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseBadVersion"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
		} catch (DatabaseAccessException e2) {
			JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseNoAccess"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
		}

		// start the import logic
		if (db != null && db.isEmpty()) {
			ImportLogic.init(db);
			flag = true;
			importButton.doClick();
		}

		// reload the UI
		if (db != null && !db.isEmpty()) {
			mainWindow.loadProject(db);
			flag = false;
		} else {
			JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseNoData"), rb.getString("error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Initializes all actions used in this class.
	 */
	private void initActions() {
		newWS = new NewWSAction();
		importAction = new ImportAction();
		infoAction = new InfoAction();
		exportAction = new ExportAction();
		settingsAction = new SettingsAction();

		// Register all action for shortcuts
		EventController.getInstance().setAction(newWS, "eventSetNewWorkspace");
		EventController.getInstance().setAction(importAction, "eventImportNewData");
		EventController.getInstance().setAction(infoAction, "eventShowInfo");
		EventController.getInstance().setAction(settingsAction, "eventOpenSettings");
		EventController.getInstance().setAction(exportAction, "eventExportData");
	}

	/**
	 * Initializes all Buttons used in this class.
	 */
	private void initButtons() {
		try {
			Dimension buttonSize = new Dimension(28, 28);

			newButton = new JButton(this.newWS);
			newButton.setIcon(new ImageIcon(ImageIO.read(MenuPanel.class.getResourceAsStream("/new.png"))));
			newButton.setToolTipText(rb.getString("newButton"));
			newButton.setPreferredSize(buttonSize);
			newButton.setMaximumSize(buttonSize);

			importButton = new JButton(this.importAction);
			importButton.setIcon(new ImageIcon(ImageIO.read(MenuPanel.class.getResourceAsStream("/import.png"))));
			importButton.setToolTipText(rb.getString("importButton"));
			importButton.setPreferredSize(buttonSize);
			importButton.setMaximumSize(buttonSize);

			exportButton = new JButton(this.exportAction);
			exportButton.setIcon(new ImageIcon(ImageIO.read(MenuPanel.class.getResourceAsStream("/export.png"))));
			exportButton.setToolTipText(rb.getString("exportButton"));
			exportButton.setPreferredSize(buttonSize);
			exportButton.setMaximumSize(buttonSize);

			infoButton = new JButton(this.infoAction);
			infoButton.setIcon(new ImageIcon(ImageIO.read(MenuPanel.class.getResourceAsStream("/info.png"))));
			infoButton.setToolTipText(rb.getString("infoButton"));
			infoButton.setPreferredSize(buttonSize);
			infoButton.setMaximumSize(buttonSize);

			settingsButton = new JButton(this.settingsAction);
			settingsButton.setIcon(new ImageIcon(ImageIO.read(MenuPanel.class.getResourceAsStream("/settings.png"))));
			settingsButton.setToolTipText(rb.getString("settings"));
			settingsButton.setPreferredSize(buttonSize);
			settingsButton.setMaximumSize(buttonSize);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Defines the new workspace action.
	 * 
	 */
	class NewWSAction extends AbstractAction {

		private static final long serialVersionUID = 6925671419464405501L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			fc.setDialogTitle(rb.getString("workspaceDialog"));
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".bsv", "bsv");
			fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(filter);

			int ret = fc.showOpenDialog(EventController.getInstance().getRootFrame());
			if (ret == JFileChooser.APPROVE_OPTION) {
				mainWindow.removeComponents();
				path = fc.getSelectedFile().getAbsolutePath();
				String extension = ".bsv";
				if (!path.endsWith(extension)) {
					path = path + extension;
				}

				boolean restart = MainWindow.curBSVFile != null && !mainWindow.equals(path);

				if (restart) {
					try {
						mainWindow.restartApplication(path);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				} else {
					loadDatabase(path);
				}

			}
		}
	}

	/**
	 * Defines the import action.
	 * 
	 */
	class ImportAction extends AbstractAction {

		private static final long serialVersionUID = -6848965362765706037L;

		/**
		 * Import dialog window.
		 */
		private JDialog importDialog;

		/**
		 * Describes the first path.
		 */
		private JLabel path1Label;

		/**
		 * Describes the second path.
		 */
		private JLabel path2Label;

		/**
		 * Displays the first path.
		 */
		private JTextField path1;

		/**
		 * Displays the second path.
		 */
		private JTextField path2;

		/**
		 * Opens a file chooser for the first path.
		 */
		private JButton first;

		/**
		 * Opens a file chooser for the second path.
		 */
		private JButton second;

		/**
		 * Imports the selected files
		 */
		private JButton ok;

		/**
		 * Cancels the import.
		 */
		private JButton cancel;

		/*
		 * Files
		 */
		private File file1;
		private File file2;

		/**
		 * Defines what happens when choosing the first file in the import dialog.
		 */
		private FirstAction firstAction;

		/**
		 * Defines what happens when choosing the second file in the import dialog.
		 */
		private SecondAction secondAction;

		/**
		 * Defines what happens when clicked on the import button in the import dialog.
		 */
		private ImportButtonAction importButtonAction;

		/**
		 * Defines what happens when clicked on the cancel button in the import dialog.
		 */
		private CancelButtonAction cancelButtonAction;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (flag) {
				importDialog = new JDialog(mainWindow, true);
				importDialog.setLayout(null);
				importDialog.setSize(500, 270);
				importDialog.setPreferredSize(importDialog.getSize());
				importDialog.setResizable(false);
				importDialog.setTitle(rb.getString("importDialog"));
				importDialog.setLocationRelativeTo(mainWindow);

				initImportDialogActions();

				path1Label = new JLabel();
				path1Label.setText(rb.getString("importText1"));
				path1Label.setBounds(20, 20, 250, 25);
				path1 = new JTextField();
				path1.setBounds(20, 50, 400, 25);

				path2Label = new JLabel();
				path2Label.setText(rb.getString("importText2"));
				path2Label.setBounds(20, 100, 250, 25);
				path2 = new JTextField();
				path2.setBounds(20, 130, 400, 25);

				first = new JButton(this.firstAction);
				first.setText(rb.getString("fileChooserButton"));
				first.setBounds(420, 50, 50, 25);
				second = new JButton(this.secondAction);
				second.setText(rb.getString("fileChooserButton"));
				second.setBounds(420, 130, 50, 25);

				ok = new JButton(this.importButtonAction);
				ok.setText(rb.getString("importButton"));
				ok.setBounds(270, 200, 100, 25);

				cancel = new JButton(this.cancelButtonAction);
				cancel.setText(rb.getString("abortButton"));
				cancel.setBounds(380, 200, 100, 25);

				importDialog.add(path1Label);
				importDialog.add(path1);
				importDialog.add(path2Label);
				importDialog.add(path2);
				importDialog.add(first);
				importDialog.add(second);
				importDialog.add(ok);
				importDialog.add(cancel);

				importDialog.pack();
				importDialog.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseAlreadyLoaded"), rb.getString("error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}

		private void initImportDialogActions() {
			this.firstAction = new FirstAction();
			this.secondAction = new SecondAction();
			this.cancelButtonAction = new CancelButtonAction();
			this.importButtonAction = new ImportButtonAction();
		}

		private void closeImportDialog() {
			importDialog.setVisible(false);
		}

		/**
		 * Action done when choosing the first file to import.
		 * 
		 */
		class FirstAction extends AbstractAction {
			
			private static final long serialVersionUID = -5928081343404964680L;

			/**
			 * Defines what happens when this action is triggered.
			 * 
			 * @param e
			 *            - action event.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
				if (path2.getText() != null) {
					fc.setCurrentDirectory(new File(path2.getText()));
				}
				if (path1.getText() != null) {
					fc.setCurrentDirectory(new File(path1.getText()));
				}
				fc.setDialogTitle(rb.getString("importDialog1"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".arff and .csv only", "arff", "csv");
				fc.setAcceptAllFileFilterUsed(false);
				fc.addChoosableFileFilter(filter);
				int ret = fc.showOpenDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					file1 = fc.getSelectedFile();
					path1.setText(file1.toString());
				}
			}
		}

		/**
		 * Action done when choosing the second file to import.
		 * 
		 */
		class SecondAction extends AbstractAction {
			
			private static final long serialVersionUID = -8983474343923949214L;

			/**
			 * Defines what happens when this action is triggered.
			 * 
			 * @param e
			 *            - action event.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
				fc.setDialogTitle(rb.getString("importDialog2"));
				if (path1.getText() != null) {
					fc.setCurrentDirectory(new File(path1.getText()));
				}
				if (path2.getText() != null) {
					fc.setCurrentDirectory(new File(path2.getText()));
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".ssd", "ssd");
				fc.setAcceptAllFileFilterUsed(false);
				fc.addChoosableFileFilter(filter);
				int ret = fc.showOpenDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					file2 = fc.getSelectedFile();
					path2.setText(file2.toString());
				} 
			}
		}

		/**
		 * Action done when clicking the import button in the import dialog.
		 * 
		 */
		class ImportButtonAction extends AbstractAction {

			private static final long serialVersionUID = 9131387291577584095L;

			/**
			 * Defines what happens when this action is triggered.
			 * 
			 * @param e
			 *            - action event.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ImportLogic.getInstance() != null && flag) {
					
					// the 4 if statements check if the given files have valid extensions and exist.
					if (!path1.getText().endsWith(".arff") && !path1.getText().endsWith(".csv")) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importUnsupportedFileExtension1"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (!path2.getText().endsWith(".ssd")) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importUnsupportedFileExtension2"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					if(new File(path1.getText()).exists() == false) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importNonExistingFile1"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}
					if(new File(path2.getText()).exists() == false) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importNonExistingFile2"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					try {
						file1 = new File(path1.getText());
						file2 = new File(path2.getText());
						ImportLogic.getInstance().importFile(file1, file2);
					} catch (UnsupportedFileExtensionException ex) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importNotValidExtension"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						ex.printStackTrace();
					} catch (InvalidFileException ex) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("importCorruptedFile"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						ex.printStackTrace();
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("SystemFailure"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						ex.printStackTrace();
					} catch (DatabaseAccessException ex) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("DatabaseNoAccess"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						ex.printStackTrace();
					} catch (InterruptedException ex) {
						JOptionPane.showMessageDialog(mainWindow, rb.getString("SystemFailure"),
								rb.getString("warning"), JOptionPane.WARNING_MESSAGE);
						ex.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(null, rb.getString("DatabaseAlreadyLoaded"), rb.getString("error"),
							JOptionPane.ERROR_MESSAGE);
				}

				closeImportDialog();
			}
		}

		/**
		 * Action done when clicking the cancel button in the import dialog.
		 * 
		 */
		class CancelButtonAction extends AbstractAction {

			private static final long serialVersionUID = -5094716053672516965L;

			/**
			 * Defines what happens when this action is triggered.
			 * 
			 * @param e
			 *            - action event.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				closeImportDialog();
			}
		}
	}

	/**
	 * Defines the info action.
	 * 
	 */
	class InfoAction extends AbstractAction {

		private static final long serialVersionUID = 3582128685945841012L;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			idialog.setVisible(true);
		}
	}

	/**
	 * Defines the export action.
	 * 
	 */
	class ExportAction extends AbstractAction {


		private static final long serialVersionUID = -6498753362256775129L;
		
		JDialog dialog;
		ExportLogic exLogic;
		JButton fcButton;
		JButton export;
		JButton abort;
		JTextField path;
		JLabel pathText;
		Checkbox check;
		JLabel checkLbl;

		FCAction fcAction;
		AbortAction abortAction;
		ExportButtonAction exportButtonAction;
		File f;

		/**
		 * Defines what happens when this action is triggered.
		 * 
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			initExportDialogActions();

			dialog = new JDialog(mainWindow, true);
			dialog.setTitle(rb.getString("exportDialog"));
			fcButton = new JButton(rb.getString("fileChooserButton"));
			export = new JButton(rb.getString("exportButton"));
			abort = new JButton(rb.getString("abortButton"));
			pathText = new JLabel(rb.getString("exportText"));
			check = new Checkbox();
			checkLbl = new JLabel(rb.getString("ExportGenSSD"));

			dialog.setLayout(null);
			dialog.setSize(500, 220);
			dialog.setPreferredSize(dialog.getSize());
			dialog.setLocationRelativeTo(mainWindow);

			pathText.setBounds(20, 20, 150, 25);
			path = new JTextField();
			path.setBounds(20, 50, 400, 25);

			fcButton = new JButton(this.fcAction);
			fcButton.setText(rb.getString("fileChooserButton"));
			fcButton.setBounds(420, 50, 50, 25);

			export = new JButton(this.exportButtonAction);
			export.setText(rb.getString("exportButton"));
			export.setBounds(260, 130, 100, 25);

			abort = new JButton(this.abortAction);
			abort.setText(rb.getString("abortButton"));
			abort.setBounds(370, 130, 100, 25);

			checkLbl.setBounds(20, 84, 220, 25);
			check.setBounds(250, 85, 24, 24);

			dialog.add(pathText);
			dialog.add(path);
			dialog.add(fcButton);
			dialog.add(export);
			dialog.add(abort);
			dialog.add(checkLbl);
			dialog.add(check);

			dialog.setVisible(true);
		}

		private void initExportDialogActions() {
			this.fcAction = new FCAction();
			this.abortAction = new AbortAction();
			this.exportButtonAction = new ExportButtonAction();
		}

		private void closeExportDialog() {
			dialog.setVisible(false);
		}

		/**
		 * Action done when choosing the first file to import.
		 */
		class FCAction extends AbstractAction {


			private static final long serialVersionUID = -4812857764306737393L;

			/**
			 * Defines what happens when this action is triggered.
			 * 
			 * @param e
			 *            - action event.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
				if (path.getText() != null) {
					fc.setCurrentDirectory(new File(path.getText()));
				}
				fc.setDialogTitle(rb.getString("exportDialog"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);

				String[] extensions = ExportLogic.getInstance().getExportFormats();
				for (String s : extensions) {
					fc.addChoosableFileFilter(new FileNameExtensionFilter("." + s + " files", s));
				}

				int ret = fc.showSaveDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					f = fc.getSelectedFile();
					boolean validExtension = true;
					String[] usedExtensions = ((FileNameExtensionFilter) fc.getFileFilter()).getExtensions();

					for (String s : usedExtensions) {
						validExtension &= f.getName().endsWith(s);
					}

					if (!validExtension) {
						String path = f.getAbsolutePath() + "." + usedExtensions[0];
						f = new File(path);
					}

					path.setText(f.toString());
				} else {
					JOptionPane.showMessageDialog(null, rb.getString("badFileType"), rb.getString("warning"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		/**
		 * Action that is performed to abort the export dialog
		 */
		class AbortAction extends AbstractAction {
			private static final long serialVersionUID = -7051496563140565735L;

			@Override
			public void actionPerformed(ActionEvent e) {
				closeExportDialog();
			}
		}

		/**
		 * Action that is performed to export the data finally
		 */
		class ExportButtonAction extends AbstractAction {
			private static final long serialVersionUID = -7832468903808405788L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ExportLogic el = ExportLogic.getInstance();
					el.exportFile(f, check.getState());
				} catch (IOException ex) {
					Logger.getLogger(MenuPanel.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvalidFileException ex) {
					Logger.getLogger(MenuPanel.class.getName()).log(Level.SEVERE, null, ex);
				} catch (DatabaseAccessException ex) {
					Logger.getLogger(MenuPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
				closeExportDialog();
			}
		}
	}

	private class SettingsAction extends AbstractAction {
		private static final long serialVersionUID = 9106668603221120596L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsDialog sd = new SettingsDialog(mainWindow);
			sd.setVisible(true);
		}

	}
}
