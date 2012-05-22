package gui.views;

import java.util.Observer;

import javax.swing.JPanel;

import controller.DataHub;
import controller.SelectionController;
import controller.SubspaceController;

/**
 * This class implements an abstract view.
 */
public abstract class ViewPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 2676550591035042084L;

	/**
	 * Holds a reference to the preinitialized DataHub.
	 */
	protected DataHub dataHub;

	/**
	 * Holds a reference to the preinitialized SelectionController.
	 */
	protected SelectionController selectionController;

	/**
	 * Holds a reference to the preinitialized SubspaceController.
	 */
	protected SubspaceController subspaceController;

	/**
	 * Returns the localized name of this view.
	 * 
	 * @return localized name of this view.
	 */
	@Override
	public abstract String getName();

	/**
	 * Constructs a view using a {@link DataHub} and a {@link SelectionController}.
	 * 
	 * @param dataHub
	 *            the preinitialized DataHub.
	 * @param selectionController
	 *            the preinitialized SelectionController.
	 * @param subspaceController
	 *            the preinitialized SubspaceController.
	 */
	public ViewPanel(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		if (dataHub == null || selectionController == null || subspaceController == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}

		this.dataHub = dataHub;
		this.selectionController = selectionController;
		this.subspaceController = subspaceController;

		this.dataHub.addObserver(this);
		this.selectionController.addObserver(this);
		this.subspaceController.addObserver(this);
	}
}
