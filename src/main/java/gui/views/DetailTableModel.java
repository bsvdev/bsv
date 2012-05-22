package gui.views;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import controller.Feature;

/**
 * DetailTableModel prepares the selection details to be visualized by a JTable.
 */
public class DetailTableModel implements TableModel {

	/**
	 * Actual data.
	 */
	private final float[][] floatData;

	/**
	 * Currently active features.
	 */
	private final Feature[] features;

	/**
	 * Column names.
	 */
	private final String[] columnNames;

	/**
	 * Validity flag of the values.
	 */
	private final boolean validValues;

	/**
	 * Constructs a new detail table model.
	 * 
	 * @param features
	 *            list of features.
	 * @param floatData
	 *            the calculated detail data.
	 * @param columnNames
	 *            column names.
	 * @param validValues
	 *            true if data is valid.
	 */
	public DetailTableModel(Feature[] features, float[][] floatData, String[] columnNames, boolean validValues) {
		this.floatData = floatData;
		this.features = features;
		this.columnNames = columnNames;
		this.validValues = validValues;
	}

	@Override
	public void addTableModelListener(TableModelListener arg0) {
		// not needed
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getRowCount() {
		return floatData.length;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return features[row].getName();
		}

		if (!validValues) {
			return "-";
		}

		return floatData[row][column - 1];
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// No editable cells
	}
}
