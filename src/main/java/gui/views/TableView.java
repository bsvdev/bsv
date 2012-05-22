package gui.views;

import gui.bsvComponents.BSVSpinner;
import gui.bsvComponents.ColorRenderer;
import gui.bsvComponents.NumberCellRenderer;
import gui.main.EventController;
import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.DataHub;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * Provides a table view of the current data. Allows the user to sort it according to various parameters and do
 * operations on it.
 */
public class TableView extends ViewPanel {
	private static final long serialVersionUID = 281259919304409364L;

	/**
	 * The actual table.
	 */
	private JTable table;

	/**
	 * The TableModel that supplies the table with data.
	 */
	private DataTableModel dtm;

	/**
	 * JScrollPane to allow scrolling.
	 */
	private JScrollPane scrollPane;

	/**
	 * Sidebar for table options.
	 */
	private JPanel sidebar;

	/**
	 * SelectAction for Button and Shortcut.
	 */
	private final AbstractAction selectAction;

	/**
	 * DeselectAction for Button and Shortcut.
	 */
	private final AbstractAction deselectAction;

	/**
	 * SelectAllAction for Button and Shortcut.
	 */
	private final AbstractAction selectAllAction;

	/**
	 * DeselectAllAction for Button and Shortcut.
	 */
	private final AbstractAction deselectAllAction;

	/**
	 * Sets the standard feature column width.
	 */
	private int featureColumnWidth = 100;

	/**
	 * Sets the standard number of decimal places.
	 */
	private int decimalPlaces = 2;

	/**
	 * The width of the id column.
	 */
	private static final int ID_COLUMN_WIDTH = 60;

	/**
	 * The width of the color column.
	 */
	private static final int COLOR_COLUMN_WIDTH = 50;

	/**
	 * The width of the selected column.
	 */
	private static final int IS_SELECTED_WIDTH = 20;

	/**
	 * The minimum allowed decimal places.
	 */
	private static final int MIN_DECIMAL_PLACES = 0;

	/**
	 * The maximum allowed decimal places.
	 */
	private static final int MAX_DECIMAL_PLACES = 9;

	/**
	 * The minimum allowed widths of the feature column.
	 */
	private static final int MIN_FEATURE_WIDTH = 40;

	/**
	 * The maximum allowed width of the feature column.
	 */
	private static final int MAX_FEATURE_WIDTH = 200;

	/**
	 * Sets stepping for feature width spinner.
	 */
	private static final int FEATURE_WIDTH_STEP = 5;

	/**
	 * Constructs a new table view.
	 *
	 * @param dataHub
	 *            reference to DataHub.
	 * @param selectionController
	 *            reference to SelectionController.
	 * @param subspaceController
	 *            the preinitialized SubspaceController.
	 */
	public TableView(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		super(dataHub, selectionController, subspaceController);

		this.selectAction = new SelectAction();
		this.deselectAction = new DeselectAction();
		this.selectAllAction = new SelectAllAction();
		this.deselectAllAction = new DeselectAllAction();

		this.setBackground(Color.WHITE);

		super.setLayout(new BorderLayout());

		boolean valid;

		valid = createNewTable();

		if (valid) {
			this.scrollPane = new JScrollPane(table);

			customizeCellRenderer();
			customizeColumns();

			this.add(scrollPane, BorderLayout.CENTER);
			buildSidebar();
		} else {
			scrollPane = null;
			showErrorMessage();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (this.isVisible()) {
			boolean valid;

			if (scrollPane != null) {
				this.remove(scrollPane);
			}

			valid = createNewTable();

			if (valid) {
				customizeCellRenderer();
				customizeColumns();

				scrollPane = new JScrollPane(table);

				// Create the scroll pane and add the table to it.
				this.add(scrollPane);
				this.validate();
			} else {
				scrollPane = null;
				showErrorMessage();
			}
		}
	}

	@Override
	public String getName() {
		return Settings.getInstance().getResourceBundle().getString("tableName");
	}

	private void buildSidebar() {
		this.sidebar = new JPanel();
		this.sidebar.setLayout(new BoxLayout(this.sidebar, BoxLayout.Y_AXIS));
		this.add(this.sidebar, BorderLayout.EAST);

		JPanel selectPanel = buildSelectPanel();
		JPanel tableOptionPanel = buildTableOptionPanel();

		JPanel extender = new JPanel();
		extender.setMaximumSize(new Dimension(150, 20));
		extender.setMinimumSize(new Dimension(150, 0));
		extender.setPreferredSize(new Dimension(150, 0));

		selectPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("tableLabelSelection")));

		tableOptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("tableLabelOptions")));

		this.addToSidebar(selectPanel);
		this.sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
		this.addToSidebar(tableOptionPanel);
		this.addToSidebar(extender);

	}

	private JPanel buildSelectPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

		// create and add "Select" Button
		JButton selectButton = new JButton(new SelectAction());
		selectButton.setText(Settings.getInstance().getResourceBundle().getString("tableButtonSelect"));

		JPanel selectPanel = new JPanel(new BorderLayout());
		selectPanel.add(selectButton);
		result.add(selectPanel);

		// create and add "Deselect" Button
		JButton deselectButton = new JButton(new DeselectAction());
		deselectButton.setText(Settings.getInstance().getResourceBundle().getString("tableButtonDeselect"));

		JPanel deselectPanel = new JPanel(new BorderLayout());
		deselectPanel.add(deselectButton);
		result.add(deselectPanel);

		// create and add "Select All" Button
		JButton selectAllButton = new JButton(new SelectAllAction());
		selectAllButton.setText(Settings.getInstance().getResourceBundle().getString("tableButtonSelectAll"));

		JPanel selectAllPanel = new JPanel(new BorderLayout());
		selectAllPanel.add(selectAllButton);
		result.add(selectAllPanel);

		// create and add "Deselect All" Button
		JButton deselectAllButton = new JButton(new DeselectAllAction());
		deselectAllButton.setText(Settings.getInstance().getResourceBundle().getString("tableButtonDeselectAll"));

		JPanel deselectAllPanel = new JPanel(new BorderLayout());
		deselectAllPanel.add(deselectAllButton);
		result.add(deselectAllPanel);

		return result;
	}

	private JPanel buildTableOptionPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

		// create and add Label and Spinner to change number of decimal places
		JLabel decimalPlacesLabel = new JLabel(Settings.getInstance().getResourceBundle().getString(
				"tableLabelDecimalPlaces"));
		JSpinner decimalSpinner = new BSVSpinner(new SpinnerNumberModel(decimalPlaces, MIN_DECIMAL_PLACES,
				MAX_DECIMAL_PLACES, 1));
		decimalSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JSpinner source = (JSpinner) arg0.getSource();
				decimalPlaces = (Integer) source.getValue();
				customizeCellRenderer();
				table.repaint();
			}
		});

		JPanel decimalSpinnerPanel = new JPanel(new BorderLayout());
		decimalSpinnerPanel.add(decimalPlacesLabel, BorderLayout.NORTH);
		decimalSpinnerPanel.add(decimalSpinner, BorderLayout.CENTER);
		result.add(decimalSpinnerPanel);

		// create and add Label and Spinner to change width of feature columns
		JLabel columnWidth = new JLabel(Settings.getInstance().getResourceBundle().getString("tableLabelColumnWidth"));
		JSpinner widthSpinner = new BSVSpinner(new SpinnerNumberModel(featureColumnWidth, MIN_FEATURE_WIDTH,
				MAX_FEATURE_WIDTH, FEATURE_WIDTH_STEP));
		widthSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JSpinner source = (JSpinner) arg0.getSource();
				featureColumnWidth = (Integer) source.getValue();
				customizeColumns();
				table.repaint();
			}
		});

		JPanel columnWidthPanel = new JPanel(new BorderLayout());
		columnWidthPanel.add(columnWidth, BorderLayout.NORTH);
		columnWidthPanel.add(widthSpinner, BorderLayout.CENTER);
		result.add(columnWidthPanel);

		return result;

	}

	/**
	 * Adds component to sidebar, used for tools and so forth.
	 *
	 * @param component
	 *            component that should be added.
	 */
	protected void addToSidebar(JComponent component) {
		component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(), (int) component
				.getPreferredSize().getHeight()));
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.sidebar.add(component);
	}

	/**
	 * Gets the currently highlighted rows in the table and tells the SelectionController to select them.
	 */
	private void selectMarkedCells() {
		int[] selectedRows = table.getSelectedRows();
		int[] newSelection = new int[selectedRows.length];

		for (int i = 0; i < newSelection.length; i++) {
			newSelection[i] = new Integer(table.getValueAt(selectedRows[i], DataTableModel.ID_COLUMN).toString());
		}

		selectionController.deleteObserver(this);
		selectionController.select(newSelection);
		selectionController.addObserver(this);
		table.clearSelection();

		this.validate();
	}

	/**
	 * Gets the currently highlighted rows in the table and tells the SelectionController to deselect them.
	 */
	private void deselectMarkedCells() {
		int[] selectedRows = table.getSelectedRows();
		int[] newSelection = new int[selectedRows.length];

		for (int i = 0; i < newSelection.length; i++) {
			newSelection[i] = new Integer(table.getValueAt(selectedRows[i], DataTableModel.ID_COLUMN).toString());
		}
		selectionController.deleteObserver(this);
		selectionController.unselect(newSelection);
		selectionController.addObserver(this);
		table.clearSelection();

		this.validate();
	}

	/**
	 * Notifies the SelectionController to select every row.
	 */
	private void selectAll() {
		int[] selectedIds = new int[table.getRowCount()];

		for (int i = 0; i < selectedIds.length; i++) {
			selectedIds[i] = new Integer(table.getValueAt(i, DataTableModel.ID_COLUMN).toString());
		}
		selectionController.deleteObserver(this);
		selectionController.select(selectedIds);
		selectionController.addObserver(this);

		this.table.repaint();
	}

	/**
	 * Notifies the SelectionController to reset selection.
	 */
	private void deselectAll() {
		selectionController.deleteObserver(this);
		selectionController.reset();
		selectionController.addObserver(this);
		this.table.repaint();
	}

	/**
	 * Creates a new table with the latest data and stores it in a table member.
	 *
	 * @return true if result is valid.
	 */
	private boolean createNewTable() {
		try {
			dtm = new DataTableModel(this.subspaceController.getActiveSubspace().getFeatures(), this.dataHub.getData(),
					selectionController, this);
		} catch (DatabaseAccessException e) {
			return false;
		}

		table = new JTable(dtm);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setColumnSelectionAllowed(false);
		table.setAutoCreateRowSorter(true);
		table.setAutoCreateColumnsFromModel(true);

		return true;
	}

	/**
	 * Sets the accurate cell renderers for the table columns.
	 */
	private void customizeCellRenderer() {
		table.getColumnModel().getColumn(DataTableModel.COLOR_COLUMN).setCellRenderer(new ColorRenderer(table));

		table.getColumnModel().getColumn(DataTableModel.IS_SELECTED_COLUMN).setCellRenderer(
				table.getDefaultRenderer(Boolean.class));

		table.getColumnModel().getColumn(DataTableModel.ID_COLUMN).setCellRenderer(new NumberCellRenderer(0));

		// set feature columns width
		for (int i = DataTableModel.FIRST_FEATURE_COLUMN; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(new NumberCellRenderer(decimalPlaces));
		}
	}

	/**
	 * Sets the accurate column widths.
	 */
	private void customizeColumns() {
		// set isSelected column width
		table.getColumnModel().getColumn(DataTableModel.IS_SELECTED_COLUMN).setMinWidth(IS_SELECTED_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.IS_SELECTED_COLUMN).setMaxWidth(IS_SELECTED_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.IS_SELECTED_COLUMN).setPreferredWidth(IS_SELECTED_WIDTH);

		// set color column width
		table.getColumnModel().getColumn(DataTableModel.COLOR_COLUMN).setMinWidth(COLOR_COLUMN_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.COLOR_COLUMN).setMaxWidth(COLOR_COLUMN_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.COLOR_COLUMN).setPreferredWidth(COLOR_COLUMN_WIDTH);

		// set id column width
		table.getColumnModel().getColumn(DataTableModel.ID_COLUMN).setMinWidth(ID_COLUMN_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.ID_COLUMN).setMaxWidth(ID_COLUMN_WIDTH);
		table.getColumnModel().getColumn(DataTableModel.ID_COLUMN).setPreferredWidth(ID_COLUMN_WIDTH);

		// set feature columns width
		for (int i = DataTableModel.FIRST_FEATURE_COLUMN; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setMinWidth(featureColumnWidth);
			table.getColumnModel().getColumn(i).setMaxWidth(featureColumnWidth);
			table.getColumnModel().getColumn(i).setPreferredWidth(featureColumnWidth);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			super.setVisible(visible);
			this.update(null, null);

			EventController.getInstance().registerKeyTarget(this);
			EventController.getInstance().setAction(selectAction, "eventSetSelection");
			EventController.getInstance().setAction(deselectAction, "eventUnsetSelection");
			EventController.getInstance().setAction(selectAllAction, "eventAllSelection");
			EventController.getInstance().setAction(deselectAllAction, "eventResetSelection");
		} else {
			EventController.getInstance().removeAction("eventSetSelection");
			EventController.getInstance().removeAction("eventUnsetSelection");
			EventController.getInstance().removeAction("eventAllSelection");
			EventController.getInstance().removeAction("eventResetSelection");
			super.setVisible(visible);
		}
	}

	private class SelectAction extends AbstractAction {
		private static final long serialVersionUID = 517346861972620697L;

		@Override
		public void actionPerformed(ActionEvent e) {
			selectMarkedCells();
		}
	}

	private class SelectAllAction extends AbstractAction {
		private static final long serialVersionUID = 3299566724985145546L;

		@Override
		public void actionPerformed(ActionEvent e) {
			selectAll();
		}
	}

	private class DeselectAction extends AbstractAction {
		private static final long serialVersionUID = -5893751844205324627L;

		@Override
		public void actionPerformed(ActionEvent e) {
			deselectMarkedCells();
		}
	}

	private class DeselectAllAction extends AbstractAction {
		private static final long serialVersionUID = 6191139922770259940L;

		@Override
		public void actionPerformed(ActionEvent e) {
			deselectAll();
		}
	}

	private void showErrorMessage() {
		JOptionPane.showMessageDialog(this, Settings.getInstance().getResourceBundle().getString("tableError"));
	}
}
