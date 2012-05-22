package gui.bsvComponents;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Allows drawing of colored cells in a JTabel.
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 2718894045515882607L;

	/**
	 * Format to define the look of rendered numbers.
	 */
	private final DecimalFormat format;

	/**
	 * Constructs a new number cell renderer.
	 * 
	 * @param decimalPlaces
	 *            the number of decimal places that will appear in the rendered cell
	 */
	public NumberCellRenderer(int decimalPlaces) {
		setOpaque(true);

		StringBuilder formatString = new StringBuilder();
		formatString.append("###############0");

		if (decimalPlaces > 0) {
			formatString.append(".");
		}

		for (int i = 0; i < decimalPlaces; i++) {
			formatString.append("0");
		}

		format = new DecimalFormat(formatString.toString());
	}

	/**
	 * Returns the component for the number cell.
	 * 
	 * @param table
	 *            the table for that the cells are rendered
	 * @param floatValue
	 *            the value of the rendered cell
	 * @param isSelected
	 *            tells if the cell that will be rendered is selected
	 * @param hasFocus
	 *            tells if the cell that will be rendered has focus
	 * @param row
	 *            tells which row the cell is in
	 * @param column
	 *            tells which column the cell is in
	 * 
	 * @return the rendered cell as component
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object floatValue, boolean isSelected,
			boolean hasFocus, int row, int column) {

		if ((row % 2) == 0) {
			setBackground(Color.LIGHT_GRAY);
		} else {
			setBackground(Color.WHITE);
		}

		if (isSelected) {
			setBackground(table.getSelectionBackground());
		}

		float value = new Float(floatValue.toString());

		setText(format.format(value));
		setHorizontalAlignment(JLabel.CENTER);

		return this;
	}
}