package gui.views;

import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

/**
 * DetailPanel is used to visualize the details of the current selection in two tables.
 */
public class DetailPanel extends JPanel {
	private static final long serialVersionUID = 582991112823447379L;

	/**
	 * The detailView that is showing this DetailPanel.
	 */
	private final DetailView detailView;

	/**
	 * Contains a menu to select calculated values.
	 */
	private final JPanel detailMenu;

	/**
	 * Contains the two tables with the actual data.
	 */
	private final JPanel splitPanePanel;

	/**
	 * Contains the info about involved groups.
	 */
	private final JPanel groupInfoPanel;

	/**
	 * Split pane for both tables.
	 */
	private final JSplitPane splitPane;

	/**
	 * Tables with the actual data.
	 */
	private final JTable featureTable;
	private final JTable outlierTable;

	/**
	 * ScrollPanes for the different parts of the detailPanel.
	 */
	private final JScrollPane featurePane;
	private final JScrollPane outlierPane;
	private JScrollPane groupPane;

	/**
	 * List of the involved groups.
	 */
	private final String[] groups;

	/**
	 * Number of currently selected objects.
	 */
	private final int selectedCount;

	/**
	 * Constructs a new detail panel.
	 *
	 * @param detailView
	 *            the DetailView that is showing this DetailPanel.
	 * @param featureTable
	 *            the JTable containing the info on normal features.
	 * @param outlierTable
	 *            the JTable containing the info on outlier features.
	 * @param selectedCount
	 *            the number of currently selected objects.
	 * @param groups
	 *            a list of the groups that are involved in the current selection.
	 */
	public DetailPanel(DetailView detailView, JTable featureTable, JTable outlierTable, int selectedCount,
			String[] groups) {
		super();

		if (detailView == null || featureTable == null || outlierTable == null || groups == null) {
			this.add(new JLabel(Settings.getInstance().getResourceBundle().getString("detailErrorLabel")));

			this.detailView = null;
			this.featureTable = null;
			this.outlierTable = null;
			this.groups = null;
			this.selectedCount = 0;

			this.splitPane = null;
			this.featurePane = null;
			this.outlierPane = null;

			this.detailMenu = null;
			this.splitPanePanel = null;
			this.groupInfoPanel = null;
		} else {
			this.detailView = detailView;
			this.featureTable = featureTable;
			this.outlierTable = outlierTable;
			this.groups = groups;
			this.selectedCount = selectedCount;

			this.splitPane = new JSplitPane();
			this.featurePane = new JScrollPane(featureTable);
			this.outlierPane = new JScrollPane(outlierTable);

			this.detailMenu = new JPanel(new BorderLayout());
			this.splitPanePanel = new JPanel(new BorderLayout());
			this.groupInfoPanel = new JPanel(new BorderLayout());

			this.setUpDetailPanel();
		}
	}

	/**
	 * Initializes all parts of the detail panel.
	 */
	private void setUpDetailPanel() {
		this.setMinimumSize(new Dimension(300, 2000));
		this.setMaximumSize(new Dimension(300, 2000));
		this.setPreferredSize(new Dimension(300, 2000));

		this.setLayout(new BorderLayout());

		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), Settings.getInstance()
				.getResourceBundle().getString("detailLabelTitle")
				+ " ("
				+ selectedCount
				+ " "
				+ Settings.getInstance().getResourceBundle().getString("detailLabelObjects") + ")"));

		this.featureTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		this.outlierTable.getColumnModel().getColumn(0).setPreferredWidth(100);

		this.featureTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		this.outlierTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		setUpDetailMenu();
		setUpSplitPanePanel();
		setUpGroupInfoPanel();

		this.add(detailMenu, BorderLayout.NORTH);
		this.add(splitPanePanel, BorderLayout.CENTER);
		this.add(groupInfoPanel, BorderLayout.SOUTH);
	}

	/**
	 * Initializes all parts of the DetailMenu.
	 */
	private void setUpDetailMenu() {
		this.detailMenu.setLayout(new GridLayout(0, 2));

		JCheckBox minBox = new JCheckBox(Settings.getInstance().getResourceBundle().getString("detailLabelMin"),
				detailView.isCalcMin());
		minBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcMin(checkBox.isSelected());
			}
		});

		JCheckBox maxBox = new JCheckBox(Settings.getInstance().getResourceBundle().getString("detailLabelMax"),
				detailView.isCalcMax());
		maxBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcMax(checkBox.isSelected());
			}
		});

		JCheckBox averageBox = new JCheckBox(
				Settings.getInstance().getResourceBundle().getString("detailLabelAverage"), detailView.isCalcAverage());
		averageBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcAverage(checkBox.isSelected());
			}
		});

		JCheckBox varianceBox = new JCheckBox(Settings.getInstance().getResourceBundle().getString(
				"detailLabelVariance"), detailView.isCalcVariance());
		varianceBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcVariance(checkBox.isSelected());
			}
		});

		JCheckBox standardDeviationBox = new JCheckBox(Settings.getInstance().getResourceBundle().getString(
				"detailLabelStandardDeviation"), detailView.isCalcStandardDeviation());
		standardDeviationBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcStandardDeviation(checkBox.isSelected());
			}
		});

		JCheckBox medianBox = new JCheckBox(Settings.getInstance().getResourceBundle().getString("detailLabelMedian"),
				detailView.isCalcMedian());
		medianBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JCheckBox checkBox = (JCheckBox) event.getSource();
				detailView.setCalcMedian(checkBox.isSelected());
			}
		});

		this.detailMenu.add(minBox);
		this.detailMenu.add(maxBox);
		this.detailMenu.add(averageBox);
		this.detailMenu.add(varianceBox);
		this.detailMenu.add(standardDeviationBox);
		this.detailMenu.add(medianBox);

		detailMenu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("detailLabelCharacteristics")));
	}

	/**
	 * Initializes all parts of the SplitPanePanel.
	 */
	private void setUpSplitPanePanel() {
		this.splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		this.splitPane.setTopComponent(featurePane);
		this.splitPane.setBottomComponent(outlierPane);

		this.featurePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("detailLabelFeatures")));
		this.outlierPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("detailLabelOutlierness")));

		splitPanePanel.add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * Initializes all parts of the GroupInfoPanel.
	 */
	private void setUpGroupInfoPanel() {
		JList groupList = new JList(this.groups);
		groupList.setMinimumSize(new Dimension(150, 100));

		this.groupPane = new JScrollPane(groupList);
		this.groupPane.setMinimumSize(new Dimension(150, 100));
		this.groupPane.setMaximumSize(new Dimension(150, 100));
		this.groupPane.setPreferredSize(new Dimension(150, 100));

		groupInfoPanel.add(groupPane, BorderLayout.CENTER);

		groupInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Settings
				.getInstance().getResourceBundle().getString("detailLabelInvolvedGroups")));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (detailMenu != null) {
			detailView.update(null, null);
		}
	}
}
