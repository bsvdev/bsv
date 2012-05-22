package gui.main;

import gui.settings.Settings;
import gui.views.ViewPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Provides the option to switch between different view perspectives. Table view is the default perspective.
 *
 */
public class ViewChooserPanel extends JPanel {
	
	private static final long serialVersionUID = -4768826929645750496L;

	// Buttons
	/**
	 * Button for the table view.
	 */
	private JToggleButton tableButton;

	/**
	 * Button for the scatter plot.
	 */
	private JToggleButton scatterButton;

	/**
	 * Button for the feature indicator.
	 */
	private JToggleButton indicatorButton;

	/**
	 * Button for the histogram.
	 */
	private JToggleButton histoButton;

	// Actions
	/**
	 * Actions when changing to table view.
	 */
	private ToTable toTable;

	/**
	 * Actions when changing to scatter plot view.
	 */
	private ToScatter toScatter;

	/**
	 * Actions when changing to feature indicator view.
	 */
	private ToIndicator toIndicator;

	/**
	 * Actions when changing to histogram view.
	 */
	private ToHisto toHisto;

	/**
	 * Contains the different views(perspectives)
	 */
	private ViewPanel[] viewPanels;

	/**
	 * Reference to the MainWindow
	 */
	private final MainWindow mainWindow;

	/**
	 * The index of the currently visible view
	 */
	private int currentlyVisibleView;

	/**
	 * Constructor.
	 *
	 * @param vP
	 *            - the different views.
	 * @param mainWindow
	 *            - reference to the MainWindow.
	 */
	public ViewChooserPanel(ViewPanel[] vP, MainWindow mainWindow) {
		super();
		// this.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		this.mainWindow = mainWindow;

		this.currentlyVisibleView = 0;

		initActions();
		initButtons();

		this.add(this.tableButton);
		this.add(this.indicatorButton);
		this.add(this.scatterButton);
		this.add(this.histoButton);

		// start with table button selected
		this.tableButton.setSelected(true);

		this.viewPanels = vP;

		// speciies which view is visible at the start
		// setVisiblePanel(1);

		setVisiblePanel(0);
	}

	/**
	 * Initialises all actions for this class.
	 */
	private void initActions() {
		toTable = new ToTable();
		toHisto = new ToHisto();
		toScatter = new ToScatter();
		toIndicator = new ToIndicator();
		NextView nextView = new NextView();
		PreviousView previousView = new PreviousView();

		EventController.getInstance().setAction(new AbstractAction() {

			private static final long serialVersionUID = 5740916532711277586L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				tableButton.doClick();
			}
		}, "eventShowTable");
		EventController.getInstance().setAction(new AbstractAction() {
			
			private static final long serialVersionUID = -9039873398212238115L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				histoButton.doClick();
			}
		}, "eventShowHisto");
		EventController.getInstance().setAction(new AbstractAction() {

			private static final long serialVersionUID = -5362340960142007703L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				scatterButton.doClick();
			}
		}, "eventShowScatter");
		EventController.getInstance().setAction(new AbstractAction() {
			private static final long serialVersionUID = -4337976978113581574L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				indicatorButton.doClick();
			}
		}, "eventShowIndicator");
		EventController.getInstance().setAction(nextView, "eventIterateViews");
		EventController.getInstance().setAction(previousView, "eventAntiIterateViews");
	}

	/**
	 * Initialises all buttons used in this class.
	 */
	private void initButtons() {
		ResourceBundle rb = Settings.getInstance().getResourceBundle();

		// only one view is visible at any time
		ButtonGroup group = new ButtonGroup();

		// we want the buttons to look like tabs
		Dimension buttonMinSize = new Dimension(150, 28);
		Dimension buttonMaxSize = new Dimension(220, 28);

		tableButton = new JToggleButton(this.toTable);
		scatterButton = new JToggleButton(this.toScatter);
		indicatorButton = new JToggleButton(this.toIndicator);
		histoButton = new JToggleButton(this.toHisto);

		// image tooltips
		tableButton.setToolTipText(rb.getString("toTable"));
		scatterButton.setToolTipText(rb.getString("scatterplot"));
		indicatorButton.setToolTipText(rb.getString("indicatorplot"));
		histoButton.setToolTipText(rb.getString("histplot"));

		// button consists of icon+text
		tableButton.setText(rb.getString("toTable"));
		scatterButton.setText(rb.getString("scatterplot"));
		indicatorButton.setText(rb.getString("indicatorplot"));
		histoButton.setText(rb.getString("histplot"));

		// align icon and text left, to let the buttons look like tabs
		tableButton.setHorizontalAlignment(SwingConstants.LEFT);
		scatterButton.setHorizontalAlignment(SwingConstants.LEFT);
		indicatorButton.setHorizontalAlignment(SwingConstants.LEFT);
		histoButton.setHorizontalAlignment(SwingConstants.LEFT);

		// load images or fallback to text only
		try {
			tableButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/view_table.png"))));
			scatterButton
					.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/view_scatter.png"))));
			indicatorButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/view_indicator.png"))));
			histoButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/view_hist.png"))));

		} catch (IOException e) {
			// now you don't have icons.. so what
		}

		// size of buttons
		tableButton.setMinimumSize(buttonMinSize);
		tableButton.setSize(buttonMinSize);
		tableButton.setMaximumSize(buttonMaxSize);

		scatterButton.setMinimumSize(buttonMinSize);
		scatterButton.setSize(buttonMinSize);
		scatterButton.setMaximumSize(buttonMaxSize);

		indicatorButton.setMinimumSize(buttonMinSize);
		indicatorButton.setSize(buttonMinSize);
		indicatorButton.setMaximumSize(buttonMaxSize);

		histoButton.setMinimumSize(buttonMinSize);
		histoButton.setSize(buttonMinSize);
		histoButton.setMaximumSize(buttonMaxSize);

		// add them to the group in order to achieve the radio button behavior
		group.add(tableButton);
		group.add(scatterButton);
		group.add(indicatorButton);
		group.add(histoButton);
	}

	/**
	 * Sets visible the n-th view in the viewPanels array while making all other view invisible(inactive).
	 *
	 * @param n
	 *            the index of the view.
	 */
	protected void setVisiblePanel(int n) {
		if (n < 0 || n > viewPanels.length - 1) {
			throw new IllegalArgumentException("index n does not exist");
		}
		for (int i = 0; i < viewPanels.length; i++) {
			if (viewPanels[i] != null) {
				if (i != n) {
					viewPanels[i].setVisible(false);
				}
			}
		}
		// check first if we are already selected, if not set flag
		if (!viewPanels[n].isVisible()) {
			viewPanels[n].setVisible(true);
			mainWindow.setCentralView(viewPanels[n]);
		}
		this.currentlyVisibleView = n;
	}

	/**
	 * Specifies the action switching from another view to scatter plot view.
	 */
	class ToScatter extends AbstractAction {

		private static final long serialVersionUID = 3164876765895725245L;

		/**
		 * Called if clicked on the scatter plot button.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisiblePanel(2);
		}
	}

	/**
	 * Specifies the action switching from another view to feature indicator view.
	 */
	class ToIndicator extends AbstractAction {

		private static final long serialVersionUID = -6527832693748741170L;

		/**
		 * Called if clicked on the feature indicator button.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisiblePanel(1);
		}
	}

	/**
	 * Specifies the action switching from another view to table view.
	 */
	class ToTable extends AbstractAction {

		private static final long serialVersionUID = 9062178519296650108L;

		/**
		 * Called if clicked on the table view button.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisiblePanel(0);
		}
	}

	/**
	 * Specifies the action switching from another view to histogram view.
	 */
	class ToHisto extends AbstractAction {

		private static final long serialVersionUID = 8345712262356007817L;

		/**
		 * Called if clicked on the histogram button.
		 *
		 * @param e
		 *            - action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisiblePanel(3);

		}
	}

	/**
	 * The class is used to iterate the existing views and set the next visible
	 */
	class NextView extends AbstractAction {

		private static final long serialVersionUID = -8069341339953034017L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			switch ((currentlyVisibleView + 1) % viewPanels.length) {
			case 0:
				tableButton.doClick();
				break;
			case 1:
				indicatorButton.doClick();
				break;
			case 2:
				scatterButton.doClick();
				break;
			case 3:
				histoButton.doClick();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * The class is used to iterate the existing views and set the next visible
	 */
	class PreviousView extends AbstractAction {
		
		private static final long serialVersionUID = 4912852605751303228L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int i = currentlyVisibleView - 1;
			if (i < 0) {
				i = viewPanels.length - 1;
			}
			switch (i) {
			case 0:
				tableButton.doClick();
				break;
			case 1:
				indicatorButton.doClick();
				break;
			case 2:
				scatterButton.doClick();
				break;
			case 3:
				histoButton.doClick();
				break;
			default:
				break;
			}
		}
	}

}
