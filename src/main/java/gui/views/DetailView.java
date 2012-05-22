package gui.views;

import gui.main.EventController;
import gui.settings.Settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.Group;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * DetailView offers the possibility to hide and show the selection details. It also calculates the data for the
 * selection and initializes all other panels used to visualize the details.
 */
public class DetailView extends ViewPanel {
	private static final long serialVersionUID = -2765739962910385615L;

	/**
	 * The detailPanel that visualizes the actual data.
	 */
	private DetailPanel detailPanel;

	/**
	 * The hide/show button.
	 */
	private final JButton showButton;

	/**
	 * Arrow icons for the showButton.
	 */
	private ImageIcon arrowRight;
	private ImageIcon arrowLeft;

	/**
	 * Array holding the calculated feature data.
	 */
	private float[][] featureData;

	/**
	 * Array holding the calculated subspace data.
	 */
	private float[][] subspaceData;

	/**
	 * Array holding the raw data from dataHub.
	 */
	private ElementData[] elementData;

	/**
	 * List of the features.
	 */
	private Feature[] featureFeatures;

	/**
	 * List of the outlier features.
	 */
	private Feature[] outlierFeatures;

	/**
	 * Number of currently selected objects.
	 */
	private int selectedCount;

	/**
	 * List of involved groups.
	 */
	private String[] groups;

	/**
	 * List of the columnNames.
	 */
	private String[] detailTableHeaderStrings;

	/**
	 * Calculate the minimum.
	 */
	private boolean calcMin = true;

	/**
	 * Calculate maximum.
	 */
	private boolean calcMax = true;

	/**
	 * Calculate the average.
	 */
	private boolean calcAverage = true;

	/**
	 * Calculate the variance.
	 */
	private boolean calcVariance = false;

	/**
	 * Calculate the standard deviation.
	 */
	private boolean calcStandardDeviation = false;

	/**
	 * Calculate the median.
	 */
	private boolean calcMedian = false;

	/**
	 * Minimum column.
	 */
	private int minColumn;

	/**
	 * Maximum column.
	 */
	private int maxColumn;

	/**
	 * Average column.
	 */
	private int averageColumn;

	/**
	 * Variance column.
	 */
	private int varianceColumn;

	/**
	 * Standard deviation column.
	 */
	private int standardDeviationColumn;

	/**
	 * Median column.
	 */
	private int medianColumn;

	/**
	 * Number of shown columns.
	 */
	private int columnCount;

	/**
	 * Error flag.
	 */
	private boolean errorOccured;

	/**
	 * Constructs a new detail view.
	 *
	 * @param dataHub
	 *            instance of the DataHub.
	 * @param selectionController
	 *            instance of the SelectionController.
	 * @param subspaceController
	 *            instance of the SubspaceController.
	 */
	public DetailView(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		super(dataHub, selectionController, subspaceController);

		//Add shortcut
		EventController.getInstance().registerKeyTarget(this);
		EventController.getInstance().setAction(new ShowHideAction(), "eventShowHideDetailView");

		this.errorOccured = false;

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

		try {
			arrowRight = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/arrow_right.png")));
			arrowLeft = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/arrow_left.png")));
		} catch (IOException e) {
			// no icons
		}

		update(null, null);
		detailPanel.setVisible(false);

		this.showButton = new JButton(new ShowHideAction());
		this.showButton.setIcon(arrowLeft);

		this.showButton.setMinimumSize(new Dimension(20, 30));
		this.showButton.setMaximumSize(new Dimension(20, 30));
		this.showButton.setPreferredSize(new Dimension(20, 30));



		this.add(showButton, BorderLayout.EAST);
	}

	private class ShowHideAction extends AbstractAction {

		private static final long serialVersionUID = -8846800701591409867L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (detailPanel.isVisible()) {
				detailPanel.setVisible(false);
				showButton.setIcon(arrowLeft);
			} else {
				detailPanel.setVisible(true);
				showButton.setIcon(arrowRight);
			}
		}
	}

	/**
	 * Updates the detailPanel if something has changed and visualizes it.
	 */
	private void updateDetailPanel() {
		validateDetailColumnInfo();

		featureData = calcData(featureFeatures);
		subspaceData = calcData(outlierFeatures);

		groups = calcGroupNames();

		selectedCount = selectionController.getSelectedCount();

		elementData = null;

		DetailTableModel detm1 = new DetailTableModel(featureFeatures, featureData, detailTableHeaderStrings,
				selectionController.isSomethingSelected());
		DetailTableModel detm2 = new DetailTableModel(outlierFeatures, subspaceData, detailTableHeaderStrings,
				selectionController.isSomethingSelected());

		JTable featureTable = new JTable(detm1);
		JTable subspaceTable = new JTable(detm2);

		if (detailPanel != null) {
			this.remove(detailPanel);
		}

		detailPanel = new DetailPanel(this, featureTable, subspaceTable, selectedCount, groups);

		this.add(detailPanel, BorderLayout.CENTER);
		this.validate();
	}

	/**
	 * Calculates which groups are involved.
	 */
	private String[] calcGroupNames() {
		HashSet<Group> result = new HashSet<Group>();

		for (ElementData element : elementData) {
			if (selectionController.isSelected(element.getId())) {
				for (Group group : element.getGroups()) {
					result.add(group);
				}
			}
		}

		String[] stringArray = new String[result.size()];
		Object[] resultArray = result.toArray();

		for (int i = 0; i < result.size(); i++) {
			stringArray[i] = resultArray[i].toString();
		}

		return stringArray;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (detailPanel == null || detailPanel.isVisible()) {
			this.errorOccured = false;

			getData();
			getFeatures();

			if (this.errorOccured) {
				this.showErrorMessage();
			} else {
				this.updateDetailPanel();
			}
		}
	}

	@Override
	public String getName() {
		return Settings.getInstance().getResourceBundle().getString("detailName");
	}

	/**
	 * Calculates the data.
	 */
	private float[][] calcData(Feature[] features) {
		float[][] calculatedData = new float[features.length][columnCount];

		float min;
		float max;
		float average;

		int featureCount = 0;
		float value = 0;
		float[] featureValues = new float[selectionController.getSelectedCount()];
		int selectedCount = 0;

		if (selectionController.isSomethingSelected()) {
			for (Feature feature : features) {
				min = Float.MAX_VALUE;
				max = -Float.MAX_VALUE;
				average = 0;
				selectedCount = 0;

				for (ElementData element : elementData) {
					if (selectionController.isSelected(element.getId())) {
						value = element.getValue(feature);

						featureValues[selectedCount] = value;
						selectedCount++;

						// test min
						if (calcMin && value < min) {
							min = value;
						}

						// test max
						if (calcMax && value > max) {
							max = value;
						}

						// calc average
						if (calcAverage || calcVariance || calcStandardDeviation) {
							average += value;
						}
					}
				}

				if (calcMin) {
					calculatedData[featureCount][minColumn] = min;
				}

				if (calcMax) {
					calculatedData[featureCount][maxColumn] = max;
				}

				if (calcAverage || calcVariance || calcStandardDeviation) {
					average /= selectionController.getSelectedCount();
				}

				float variance = 0;
				float tmpValue = 0;

				for (ElementData element : elementData) {
					if (selectionController.isSelected(element.getId())) {
						tmpValue = element.getValue(feature) - average;
						variance += tmpValue * tmpValue;
					}
				}

				if (calcAverage) {
					calculatedData[featureCount][averageColumn] = average;
				}

				if (calcVariance) {
					calculatedData[featureCount][varianceColumn] = variance / (selectionController.getSelectedCount());
				}

				if (calcStandardDeviation) {
					calculatedData[featureCount][standardDeviationColumn] = (float) Math.sqrt(variance
							/ (selectionController.getSelectedCount()));
				}

				float median;

				if (calcMedian) {
					java.util.Arrays.sort(featureValues);

					if ((featureValues.length % 2) == 0) {
						median = (featureValues[(featureValues.length / 2) - 1] + featureValues[(featureValues.length / 2)]) / 2;
					} else {
						median = featureValues[(featureValues.length / 2)];
					}

					calculatedData[featureCount][medianColumn] = median;
				}

				featureCount++;
			}
		}

		return calculatedData;
	}

	/**
	 * Fetches the new data.
	 */
	private void getData() {
		try {
			elementData = dataHub.getData();
		} catch (DatabaseAccessException e) {
			this.errorOccured = true;
		}
	}

	/**
	 * Fetches the features and splits them in normal features and outlier features.
	 */
	private void getFeatures() {
		Feature[] features = null;

		int nrFeatures = 0;
		int nrOutlier = 0;

		try {
			features = subspaceController.getActiveSubspace().getFeatures();
		} catch (DatabaseAccessException e) {
			this.errorOccured = false;
		}

		for (Feature x : features) {
			if (x.isOutlier()) {
				nrOutlier++;
			} else {
				nrFeatures++;
			}
		}

		featureFeatures = new Feature[nrFeatures];
		outlierFeatures = new Feature[nrOutlier];

		nrFeatures = 0;
		nrOutlier = 0;

		for (Feature x : features) {
			if (x.isOutlier()) {
				outlierFeatures[nrOutlier] = x;
				nrOutlier++;
			} else {
				featureFeatures[nrFeatures] = x;
				nrFeatures++;
			}
		}
	}

	/**
	 * Evaluates which data is calculated an sets the header titles.
	 */
	private void validateDetailColumnInfo() {
		columnCount = 0;

		if (calcAverage) {
			averageColumn = columnCount;
			columnCount++;
		} else {
			averageColumn = -1;
		}

		if (calcMin) {
			minColumn = columnCount;
			columnCount++;
		} else {
			minColumn = -1;
		}

		if (calcMax) {
			maxColumn = columnCount;
			columnCount++;
		} else {
			maxColumn = -1;
		}

		if (calcVariance) {
			varianceColumn = columnCount;
			columnCount++;
		} else {
			varianceColumn = -1;
		}

		if (calcStandardDeviation) {
			standardDeviationColumn = columnCount;
			columnCount++;
		} else {
			standardDeviationColumn = -1;
		}

		if (calcMedian) {
			medianColumn = columnCount;
			columnCount++;
		} else {
			medianColumn = -1;
		}

		this.detailTableHeaderStrings = new String[columnCount + 1];
		this.detailTableHeaderStrings[0] = Settings.getInstance().getResourceBundle().getString("detailLabelFeature");

		if (calcAverage) {
			this.detailTableHeaderStrings[averageColumn + 1] = Settings.getInstance().getResourceBundle().getString(
					"detailLabelAverage");
		}

		if (calcMin) {
			this.detailTableHeaderStrings[minColumn + 1] = Settings.getInstance().getResourceBundle().getString(
					"detailLabelMin");
		}

		if (calcMax) {
			this.detailTableHeaderStrings[maxColumn + 1] = Settings.getInstance().getResourceBundle().getString(
					"detailLabelMax");
		}

		if (calcVariance) {
			this.detailTableHeaderStrings[varianceColumn + 1] = Settings.getInstance().getResourceBundle().getString(
					"detailLabelVariance");
		}

		if (calcStandardDeviation) {
			this.detailTableHeaderStrings[standardDeviationColumn + 1] = Settings.getInstance().getResourceBundle()
					.getString("detailLabelStandardDeviation");
		}

		if (calcMedian) {
			this.detailTableHeaderStrings[medianColumn + 1] = Settings.getInstance().getResourceBundle().getString(
					"detailLabelMedian");
		}
	}

	/**
	 * Set if the minimum will be calculated.
	 *
	 * @param calcMin
	 *            new calcMin value.
	 */
	public void setCalcMin(boolean calcMin) {
		this.calcMin = calcMin;
		this.update(null, null);
	}

	/**
	 * Returns true if minimum is calculated.
	 *
	 * @return true if minimum is calculated.
	 */
	public boolean isCalcMin() {
		return this.calcMin;
	}

	/**
	 * Set if the maximum will be calculated.
	 *
	 * @param calcMax
	 *            new calcMax value.
	 */
	public void setCalcMax(boolean calcMax) {
		this.calcMax = calcMax;
		this.update(null, null);
	}

	/**
	 * Returns true if the maximum is calculated.
	 *
	 * @return true if the maximum is calculated.
	 */
	public boolean isCalcMax() {
		return this.calcMax;
	}

	/**
	 * Set if the average is calculated.
	 *
	 * @param calcAverage
	 *            new calcAverage value.
	 */
	public void setCalcAverage(boolean calcAverage) {
		this.calcAverage = calcAverage;
		this.update(null, null);
	}

	/**
	 * Returns true if average is calculated.
	 *
	 * @return true if average is calculated.
	 */
	public boolean isCalcAverage() {
		return this.calcAverage;
	}

	/**
	 * Set if the variance is calculated.
	 *
	 * @param calcVariance
	 *            new calcVariance value.
	 */
	public void setCalcVariance(boolean calcVariance) {
		this.calcVariance = calcVariance;
		this.update(null, null);
	}

	/**
	 * Returns true if variance is calculated.
	 *
	 * @return true if variance is calculated.
	 */
	public boolean isCalcVariance() {
		return this.calcVariance;
	}

	/**
	 * Set if the standard deviation is calculated.
	 *
	 * @param calcStandardDeviation
	 *            new calcStandardDeviation value.
	 */
	public void setCalcStandardDeviation(boolean calcStandardDeviation) {
		this.calcStandardDeviation = calcStandardDeviation;
		this.update(null, null);
	}

	/**
	 * Returns true if standard deviation is calculated.
	 *
	 * @return true if standard deviation is calculated.
	 */
	public boolean isCalcStandardDeviation() {
		return this.calcStandardDeviation;
	}

	/**
	 * Set if median is calculated.
	 *
	 * @param calcMedian
	 *            new calcMedian value.
	 */
	public void setCalcMedian(boolean calcMedian) {
		this.calcMedian = calcMedian;
		this.update(null, null);
	}

	/**
	 * Returns true if median is calculated.
	 *
	 * @return true if median is calculated.
	 */
	public boolean isCalcMedian() {
		return this.calcMedian;
	}

	/**
	 * Shows error message if an exception occurred.
	 */
	private void showErrorMessage() {
		JOptionPane.showMessageDialog(this, Settings.getInstance().getResourceBundle().getString("detailError"));
		this.detailPanel = new DetailPanel(this, null, null, 0, null);
	}
}
