package gui.bsvComponents;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Allows drawing of colored cells in a JTabel.
 */
public class ColorRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -4046227263627540722L;

	/**
	 * Light Border for unselected cells.
	 */
	private Border unselectedLightBorder = null;

	/**
	 * Dark Border for unselected cells.
	 */
	private Border unselectedDarkBorder = null;

	/**
	 * Border for selected cells.
	 */
	private Border selectedBorder = null;

	/**
	 * Constructs a new color renderer.
	 * 
	 * @param table
	 *            the table that will use the renderer
	 */
	public ColorRenderer(JTable table) {
		setOpaque(true);

		unselectedDarkBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, Color.LIGHT_GRAY);
		unselectedLightBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, Color.WHITE);

		selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
	}

	/**
	 * Returns the component for the colored cell.
	 * 
	 * @param table
	 *            the table for that the cells are rendered
	 * @param color
	 *            the color of the rendered cell
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
	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
			int row, int column) {

		setBackground((Color) color);

		if (isSelected) {
			setBorder(selectedBorder);
		} else {
			if ((row % 2) == 0) {
				setBorder(unselectedDarkBorder);
			} else {
				setBorder(unselectedLightBorder);
			}
		}

		return this;
	}
}