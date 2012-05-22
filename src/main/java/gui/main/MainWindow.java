package gui.main;

import gui.groupsPanel.ShowGroups;
import gui.settings.Settings;
import gui.subspacePanel.SubspacePanel;
import gui.views.DetailView;
import gui.views.TableView;
import gui.views.ViewPanel;
import gui.views.plots.HistPlot;
import gui.views.plots.IndicatorPlot;
import gui.views.plots.ScatterPlot;
import importexport.ExportLogic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import javax.media.opengl.GLProfile;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import controller.DataHub;
import controller.GroupController;
import controller.SelectionController;
import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;

/**
 * MainWindow is the main frame of the User Interface(UI). All UI Elements are directly or indirectly attached to it.
 * The main method of the program is in this class.
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = -8803557720487887941L;

	// set some global configs before start
	static {
		GLProfile.initSingleton();
		System.setProperty("sun.awt.noerasebackground", "true");
	}

	/**
	 * Stores the instance of MainWindow.
	 */
	private static MainWindow mW;

	/**
	 * Stores the instance of MenuPanel.
	 */
	private MenuPanel menuPanel;

	/**
	 * Stores the instance of ViewCHooserPanel.
	 */
	private ViewChooserPanel viewChooserPanel;

	/**
	 * Stores the instance of GroupsPanel.
	 */
	private ShowGroups showGroups;

	/**
	 * Stores the instance of DetailView.
	 */
	private DetailView detailView;

	/**
	 * Stores the instance of ViewPanels.
	 */
	private ViewPanel[] viewPanels;

	/**
	 * Stores the instance of SubspacePanel.
	 */
	private SubspacePanel subspacePanel;

	/**
	 * Stores the instance of GroupController.
	 */
	private GroupController groupController;

	/**
	 * Stores the instance of DataHub
	 */
	private DataHub dataHub;

	/**
	 * Stores the instance of SelectionController.
	 */
	private SelectionController selectionController;

	/**
	 * Stores the instance of SubspaceController.
	 */
	private SubspaceController subspaceController;

	/**
	 * Stores the instance of Database.
	 */
	private Database database;

	private static ArrayList<String> executeParameters = null;
	protected static String curBSVFile;

	/**
	 * Constructor. The main window and the menu panel are the only UI components initialized here. In order to
	 * continue, the user must choose a valid workspace.
	 */
	public MainWindow() {
		super("Black Sheep Vision");

		// set icon
		ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/icon.png"));
		Image image = imageIcon.getImage();
		this.setIconImage(image);

		// enable event system
		EventController.getInstance().setRootFrame(this);

		// load settings
		try {
			Settings.getInstance().load(null);
		} catch (IOException e) {
			// do nothing (normal when we are new here)
			e.getMessage();
		}

		// set global java locale
		Locale.setDefault(Settings.getInstance().getLanguage());
		JComponent.setDefaultLocale(Settings.getInstance().getLanguage());

		// setup settings store on shutdown
		// guarantee cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Settings.getInstance().store(null);
				} catch (IOException e) {
					// because this is a shutdown hook, we can't warn the user
					// so we simply print stack trace
					e.printStackTrace();
				}
			}
		}));

		this.setBackground(Color.BLACK);
		this.setLayout(new BorderLayout(5, 5));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setMinimumSize(new Dimension(800, 600));
		this.setVisible(true);
	}

	/**
	 * Main method of the program.
	 * 
	 * @param args
	 *            String.
	 */
	public static void main(String[] args) {

		// stores the orig jvm cmdline.
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

		File currentJar = null;
		try {
			currentJar = new File(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			// the format was incorrect
			e.printStackTrace();
		}

		/* is it a jar file? */
		if (currentJar == null || !currentJar.getName().endsWith(".jar")) {
			return;
		}

		/* Build command: java -jar application.jar */
		executeParameters = new ArrayList<String>();
		executeParameters.add(javaBin);
		executeParameters.add("-jar");
		executeParameters.add(currentJar.getPath());

		setLookAndFeel();

		if (args != null && args.length > 0 && args[0] != null && args[0].endsWith(".bsv")) {
			curBSVFile = args[0];
		} else {
			curBSVFile = null;
		}

		mW = new MainWindow();
		mW.start();
	}

	/**
	 * Sets look an feel of the entire application. Should be called on startup.
	 */
	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException ex) {
			return;
		} catch (InstantiationException ex) {
			return;
		} catch (IllegalAccessException ex) {
			return;
		} catch (UnsupportedLookAndFeelException ex) {
			return;
		}
	}

	/**
	 * Initializes the MenuPanel and ask the user to choose a valid workspace.
	 */
	private void start() {
		menuPanel = new MenuPanel(this);

		this.add(menuPanel, BorderLayout.PAGE_START);
		EventController.getInstance().registerKeyTarget(menuPanel);
		this.validate();
		if (curBSVFile == null) {
			this.menuPanel.newWorkspace();
		} else {
			this.menuPanel.loadDatabase(curBSVFile);
		}
	}

	/**
	 * Used when changing workspace.
	 * 
	 * @param db
	 *            database
	 */
	public void loadProject(Database db) {
		this.database = db;
		this.setTitle(this.database.getPath() + " - Black Sheep Vision");
		initControl();
		initUI();
	}

	/**
	 * Used for loading a new file with data.
	 * 
	 * @param path1
	 *            - path to input file.
	 * @param path2
	 *            - path to algorithm output file.
	 */
	public void importData(String path1, String path2) {
		initUI();
	}

	/**
	 * Initializes all controllers used by the program.
	 */
	private void initControl() {

		this.selectionController = new SelectionController();
		try {
			this.subspaceController = new SubspaceController(this.database);
			this.groupController = new GroupController(this.database, this.subspaceController);
		} catch (DatabaseAccessException e) {
			JOptionPane.showMessageDialog(null,
					Settings.getInstance().getResourceBundle().getString("databaseGroupReadFailed"), Settings
							.getInstance().getResourceBundle().getString("error"), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		this.dataHub = new DataHub(this.database, this.groupController, this.subspaceController);
		ExportLogic.init(this.dataHub, this.selectionController, this.subspaceController);
	}

	/**
	 * Initializes all UI elements(except MenuPanel).
	 */
	private void initUI() {
		// removes UI components if any
		removeComponents();

		// initializes new UI components
		this.showGroups = new ShowGroups(this.groupController, this.selectionController, this.subspaceController);
		this.viewPanels = new ViewPanel[4];
		viewPanels[0] = new TableView(this.dataHub, this.selectionController, this.subspaceController);
		viewPanels[0].setVisible(false);
		viewPanels[1] = new IndicatorPlot(this.dataHub, this.selectionController, this.subspaceController);
		viewPanels[1].setVisible(false);
		viewPanels[2] = new ScatterPlot(this.dataHub, this.selectionController, this.subspaceController);
		viewPanels[2].setVisible(false);
		viewPanels[3] = new HistPlot(this.dataHub, this.selectionController, this.subspaceController);
		viewPanels[3].setVisible(false);
		this.viewChooserPanel = new ViewChooserPanel(this.viewPanels, mW);
		this.detailView = new DetailView(this.dataHub, this.selectionController, this.subspaceController);
		this.subspacePanel = new SubspacePanel(this.subspaceController);

		// Adding all components to the main window
		addUIComponents();
	}

	/**
	 * Removes the previous components if any from the main window.
	 */
	protected void removeComponents() {
		if (this.viewChooserPanel != null) {
			this.viewChooserPanel.setVisible(false);
			this.remove(this.viewChooserPanel);
		}
		if (this.showGroups != null) {
			this.showGroups.setVisible(false);
			this.remove(this.showGroups);
		}
		if (this.subspacePanel != null) {
			this.subspacePanel.setVisible(false);
			this.remove(this.subspacePanel);
		}
	}

	/**
	 * Changes the current view in the middle of the layout.
	 * 
	 * @param view
	 *            - the new view.
	 */
	protected void setCentralView(ViewPanel view) {
		mW.add(view, BorderLayout.CENTER);
		EventController.getInstance().registerKeyTarget(view);
	}

	/**
	 * Adds the UI Elements to the main window frame.
	 */
	private void addUIComponents() {
		mW.add(this.showGroups, BorderLayout.LINE_START);
		EventController.getInstance().registerKeyTarget(this.viewChooserPanel);
		this.menuPanel.add(this.viewChooserPanel);

		mW.add(this.detailView, BorderLayout.LINE_END);
		mW.add(this.subspacePanel, BorderLayout.PAGE_END);
		EventController.getInstance().registerKeyTarget(showGroups);
		EventController.getInstance().registerKeyTarget(detailView);
		EventController.getInstance().registerKeyTarget(subspacePanel);
		mW.validate();
	}

	/**
	 * Restarts the application.
	 * 
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public void restartApplication() throws IOException {
		restartApplication(curBSVFile);
	}

	/**
	 * Restarts the application.
	 * 
	 * @param dbPath
	 *            the path to the database file
	 * 
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public void restartApplication(String dbPath) throws IOException {
		if (dbPath != null) {
			MainWindow.executeParameters.add(dbPath);
		}
		final ProcessBuilder builder = new ProcessBuilder(MainWindow.executeParameters);
		builder.start();
		System.exit(0);
	}
}
