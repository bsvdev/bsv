package gui.main;

import java.awt.AWTKeyStroke;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * The class {@code EventController} manages all global Events like Shortcuts.
 * 
 */
public final class EventController {

	/**
	 * Stores a list of all events that can be used
	 */
	private final ArrayList<String> events = new ArrayList<String>();

	/**
	 * Holds a reference of the root frame
	 */
	private JFrame rootFrame;

	/**
	 * The component is used to hold the Input and Action maps and handle all shortcuts
	 */
	private final JPanel keyBindingComponent;

	/**
	 * Static object for singleton pattern
	 */
	private static EventController ec;

	/**
	 * Constructs a new EventController that has registered shortcuts
	 */
	private EventController() {
		this.keyBindingComponent = new JPanel();

		// deactivate ctrl + TAB as default traversal key, in order to switch views
		FocusManager fm = FocusManager.getCurrentManager();
		Set<AWTKeyStroke> newKeys = new HashSet<AWTKeyStroke>(
				fm.getDefaultFocusTraversalKeys(FocusManager.FORWARD_TRAVERSAL_KEYS));
		newKeys.remove(KeyStroke.getKeyStroke("ctrl TAB"));
		fm.setDefaultFocusTraversalKeys(FocusManager.FORWARD_TRAVERSAL_KEYS, newKeys);
		newKeys = new HashSet<AWTKeyStroke>(fm.getDefaultFocusTraversalKeys(FocusManager.BACKWARD_TRAVERSAL_KEYS));
		newKeys.remove(KeyStroke.getKeyStroke("ctrl shift TAB"));
		fm.setDefaultFocusTraversalKeys(FocusManager.BACKWARD_TRAVERSAL_KEYS, newKeys);

		// register all shortcuts
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl N"), "eventSetNewWorkspace");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl I"), "eventImportNewData");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl E"), "eventExportData");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl G"), "eventExportGraphics");
		this.registerShortCut(KeyStroke.getKeyStroke("F1"), "eventShowInfo");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl 1"), "eventShowTable");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl 4"), "eventShowHisto");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl 3"), "eventShowScatter");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl 2"), "eventShowIndicator");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl TAB"), "eventIterateViews");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl shift TAB"), "eventAntiIterateViews");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl SPACE"), "eventOpenSubspaceChooser");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl shift F"), "eventOpenFeatureSubspaceDialog");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl shift G"), "eventAddNewGroup");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl R"), "eventResetSelection");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl S"), "eventSetSelection");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl A"), "eventAllSelection");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl D"), "eventUnsetSelection");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl shift S"), "eventOpenSettings");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl J"), "eventModeZoomdrag");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl K"), "eventModeBox");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl L"), "eventModeLasso");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl COMMA"), "eventShowHideGroupsPanel");
		this.registerShortCut(KeyStroke.getKeyStroke("ctrl PERIOD"), "eventShowHideDetailView");
	}

	/**
	 * Returns an instance of EventController
	 * 
	 * @return a EventController instance
	 */
	public static EventController getInstance() {
		if (ec == null) {
			ec = new EventController();
		}

		return ec;
	}

	/**
	 * Returns a list of all usable events
	 * 
	 * @return a list of all events
	 */
	public String[] getEvents() {
		return this.events.toArray(new String[this.events.size()]);
	}

	/**
	 * Sets an action for a specific event. The old action for the event will be overwritten.
	 * 
	 * @param action
	 *            the action
	 * @param event
	 *            the event
	 */
	public void setAction(Action action, String event) {
		this.keyBindingComponent.getActionMap().put(event, action);
	}

	/**
	 * Remove an action for the event.
	 * 
	 * @param event
	 *            the event
	 */
	public void removeAction(String event) {
		this.keyBindingComponent.getActionMap().remove(event);
	}

	/**
	 * Register a new target, that catches key actions.
	 * 
	 * @param target
	 *            the target
	 */
	public void registerKeyTarget(JComponent target) {
		target.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,
				this.keyBindingComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));
		target.setActionMap(this.keyBindingComponent.getActionMap());
	}

	/**
	 * Sets root frame. It can be used as ancestor for dialogs or file choosers later.
	 * 
	 * @param rootFrame
	 *            new root frame
	 */
	public void setRootFrame(JFrame rootFrame) {
		this.rootFrame = rootFrame;
	}

	/**
	 * Gets root frame. It can be used as ancestor for dialogs or file choosers.
	 * 
	 * @return root frame
	 */
	public JFrame getRootFrame() {
		return this.rootFrame;
	}

	/**
	 * Register a new shortcut
	 * 
	 * @param key
	 *            the key for the shortcut
	 * @param event
	 *            the resulting event of the shortcut
	 */
	private void registerShortCut(KeyStroke key, String event) {
		this.keyBindingComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, event);
		this.events.add(event);
	}
}
