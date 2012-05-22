package gui.views;

import gui.settings.Settings;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import controller.ElementData;
import controller.Feature;
import controller.SelectionController;

/**
 * This class provides the interface to build a table directly out of an ElementData array and a Feature array.
 */
public class DataTableModel implements TableModel {

	/**
	 * IsSelected column.
	 */
	public static final int IS_SELECTED_COLUMN = 0;

	/**
	 * Color column.
	 */
	public static final int COLOR_COLUMN = 1;

	/**
	 * Id column.
	 */
	public static final int ID_COLUMN = 2;

	/**
	 * First feature column.
	 */
	public static final int FIRST_FEATURE_COLUMN = 3;

	/**
	 * Row data for table.
	 */
	private final ElementData[] elementData;

	/**
	 * Column data for table.
	 */
	private final Feature[] features;

	/**
	 * Reference to the selectionController.
	 */
	private final SelectionController selectionController;

	/**
	 * The TableView that shows the table that uses this model.
	 */
	private final TableView tableView;

	/**
	 * The offset for the column count.
	 * 
	 * +1 for id column<br>
	 * +1 for color column<br>
	 * +1 for isSelected column
	 */
	private final int offset = 3;

	/**
	 * Constructs a new data table model.
	 * 
	 * @param features
	 *            An array of Features, which will be used as columns.
	 * @param elementData
	 *            An array of elementData. Every ElementData provides the data for one row.
	 * @param selectionController
	 *            Instance of the selctionController to allow selections in the table.
	 * @param tableView
	 *            the tableView that is using this Model.
	 */
	public DataTableModel(Feature[] features, ElementData[] elementData, SelectionController selectionController,
			TableView tableView) {

		if (features == null || elementData == null || selectionController == null || tableView == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}

		this.elementData = elementData;
		this.features = features;
		this.selectionController = selectionController;
		this.tableView = tableView;
	}

	@Override
	public int getColumnCount() {
		return this.features.length + offset;
	}

	@Override
	public String getColumnName(int feature) {
		if (feature < 0 || feature >= features.length + offset) {
			return null;
		}

		String columnName;

		switch (feature) {
		case IS_SELECTED_COLUMN:
			columnName = "";
			break;
		case COLOR_COLUMN:
			columnName = Settings.getInstance().getResourceBundle().getString("tableColorColumn");
			break;
		case ID_COLUMN:
			columnName = Settings.getInstance().getResourceBundle().getString("tableIdColumn");
			break;
		default:
			columnName = features[feature - FIRST_FEATURE_COLUMN].getName();
			break;
		}

		return columnName;
	}

	@Override
	public int getRowCount() {
		return elementData.length;
	}

	@Override
	public Object getValueAt(int element, int feature) {
		if (element < 0 || element >= this.elementData.length || feature < 0 || feature >= features.length + offset) {
			throw new IllegalArgumentException("Argument cannot be null");
		}

		switch (feature) {
		case IS_SELECTED_COLUMN:
			return selectionController.isSelected(elementData[element].getId());
		case COLOR_COLUMN:
			return ViewUtils.calcColor(elementData[element]);
		case ID_COLUMN:
			return elementData[element].getId();
		default:
			return elementData[element].getValue(features[feature - FIRST_FEATURE_COLUMN]);
		}
	}

	@Override
	public boolean isCellEditable(int element, int feature) {
		if (feature == IS_SELECTED_COLUMN) {
			return true;
		}

		return false;
	}

	@Override
	public void setValueAt(Object value, int element, int feature) {
		if (feature == IS_SELECTED_COLUMN) {
			boolean newValue = (Boolean) value;

			int[] changedID = new int[1];
			changedID[0] = (Integer) getValueAt(element, ID_COLUMN);

			selectionController.deleteObserver(tableView);

			if (newValue) {
				selectionController.select(changedID);
			} else {
				selectionController.unselect(changedID);
			}

			selectionController.addObserver(tableView);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// Not implemented because not needed
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(1, columnIndex).getClass();
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// Not implemented because not needed
	}
}