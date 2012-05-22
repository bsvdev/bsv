package gui.views.plots;

import gui.bsvComponents.BSVComboBox;
import gui.bsvComponents.BSVSpinner;
import gui.settings.Settings;
import gui.views.ViewUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang3.ArrayUtils;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * This class implements a histogram plot. It uses OpenGL for fast rendering.
 */
public class HistPlot extends GLPlot {
	private static final long serialVersionUID = -8863105505703128706L;

	/**
	 * Sets the size of the white borders around the plot.
	 */
	private static final int BORDER_SIZE = 60;

	/**
	 * Sets the maximum of steps, that the user can choose.
	 */
	private static final int MAX_STEPS = 500;

	/**
	 * Controls alpha of non selected elements.
	 */
	private static final float NON_SELECTED_ALPHA_FACTOR = 0.2f;

	/**
	 * Combo box for features selection.
	 */
	private JComboBox featureCombo;

	/**
	 * Spinner for bar count.
	 */
	private JSpinner stepCountSpinner;

	/**
	 * Stores the frequency values.
	 */
	private float[] values;

	/**
	 * Stores red color component.
	 */
	private float[] red;

	/**
	 * Stores green color component.
	 */
	private float[] green;

	/**
	 * Stores blue color component.
	 */
	private float[] blue;

	/**
	 * Stores alpha.
	 */
	private float[] alpha;

	/**
	 * For this feature the histogram will be plotted.
	 */
	private Feature activeFeature;

	/**
	 * Sets the number of steps/bars in the histogram.
	 */
	private int stepCount;

	/**
	 * Stores the name of the active feature.
	 */
	private String featureName;

	/**
	 * Stores the minimum value of the feature.
	 */
	private float min;

	/**
	 * Stores the feature range.
	 */
	private float range;

	/**
	 * Stores the maximum frequency that should fit the view.
	 */
	private float maxFrequency;

	/**
	 * Constructs a histogram plot using a DataHub, a SelectionController and a SubspaceController.
	 * 
	 * @param dataHub
	 *            a preinitialized DataHub.
	 * @param selectionController
	 *            a preinitialized SelectionController.
	 * @param subspaceController
	 *            a preinitialized SubspaceController.
	 */
	public HistPlot(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		super(dataHub, selectionController, subspaceController);
	}

	@Override
	protected void createUI() {
		this.stepCount = 10;

		this.featureCombo = new BSVComboBox(new Feature[0]);

		featureCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				activeFeature = (Feature) e.getItem();
				update(null, null);
			}
		});

		JPanel dataPanel = new JPanel(new GridLayout(0, 1));
		dataPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"histogramFeature")));
		dataPanel.add(this.featureCombo);

		this.stepCountSpinner = new BSVSpinner(new SpinnerNumberModel(this.stepCount, 1, MAX_STEPS, 1));
		((JSpinner.DefaultEditor) this.stepCountSpinner.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					SpinnerNumberModel model = (SpinnerNumberModel) stepCountSpinner.getModel();
					int n = (Integer) model.getNumber();
					stepCount = Math.max(1, Math.min(n, MAX_STEPS));
					update(null, null);
				}
			}
		});

		for (Component child : this.stepCountSpinner.getComponents()) {
			if ("Spinner.nextButton".equals(child.getName())) {
				((JButton) child).addActionListener(new AbstractAction() {
					private static final long serialVersionUID = 7393143016298737890L;

					@Override
					public void actionPerformed(ActionEvent e) {
						SpinnerNumberModel model = (SpinnerNumberModel) stepCountSpinner.getModel();
						int n = (Integer) model.getNumber();
						stepCount = Math.max(1, Math.min(n + 1, MAX_STEPS));
						update(null, null);
					}
				});
			}

			if ("Spinner.previousButton".equals(child.getName())) {
				((JButton) child).addActionListener(new AbstractAction() {
					private static final long serialVersionUID = 2393557689211586904L;

					@Override
					public void actionPerformed(ActionEvent e) {
						SpinnerNumberModel model = (SpinnerNumberModel) stepCountSpinner.getModel();
						int n = (Integer) model.getNumber();
						stepCount = Math.max(1, Math.min(n - 1, MAX_STEPS));
						update(null, null);
					}
				});
			}
		}

		JPanel visualPanel = new JPanel(new GridLayout(0, 1));
		visualPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"histogramBars")));
		visualPanel.add(stepCountSpinner);

		this.addToSidebar(dataPanel);
		this.addToSidebar(visualPanel);
	}

	@Override
	protected void setFeatures(Feature[] features) {
		this.featureCombo.setModel(new DefaultComboBoxModel(features));

		this.activeFeature = (Feature) featureCombo.getSelectedItem();
	}

	@Override
	protected void init(GL2 gl) {
		// ignore
	}

	@Override
	protected void rescale(GL2 gl) {
		// ignore
	}

	@Override
	protected void draw(GL2 gl) {
		// calc some things
		float width = this.getShapeWidth() - 2 * BORDER_SIZE;
		float barWidth = width / stepCount;
		float height = this.getShapeHeight() - 2 * BORDER_SIZE;

		// draw bars
		gl.glPushMatrix();
		gl.glTranslatef(BORDER_SIZE, BORDER_SIZE, 0.f);

		for (int i = 0; i < stepCount; i++) {
			gl.glBegin(GL2.GL_POLYGON);
			gl.glColor4f(this.red[i], this.green[i], this.blue[i], this.alpha[i]);
			gl.glVertex2f(i * barWidth, 0.f);
			gl.glVertex2f(i * barWidth, height * this.values[i] / this.maxFrequency);
			gl.glVertex2f((i + 1) * barWidth, height * this.values[i] / this.maxFrequency);
			gl.glVertex2f((i + 1) * barWidth, 0.f);
			gl.glEnd();
		}

		gl.glPopMatrix();
		gl.glColor4f(0.f, 0.f, 0.f, 1.f);

		// draw axis
		this.drawAxis(gl, BORDER_SIZE, BORDER_SIZE, height, Settings.getInstance().getResourceBundle().getString(
				"histogramFrequency"), 0.f, this.maxFrequency, false);
		this.drawAxis(gl, BORDER_SIZE, BORDER_SIZE, width, this.featureName, this.min, this.min + this.range, true);
	}

	@Override
	protected void dispose(GL2 gl) {
		// ignore
	}

	@Override
	public String getName() {
		return Settings.getInstance().getResourceBundle().getString("histplot");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected synchronized void processData() throws DatabaseAccessException, InterruptedException {
		int steps = this.stepCount;
		Feature feature = this.activeFeature;
		boolean selection = this.selectionController.isSomethingSelected();
		this.values = new float[steps];
		this.red = new float[steps];
		this.green = new float[steps];
		this.blue = new float[steps];
		this.alpha = new float[steps];
		int[] count = new int[steps];
		this.min = feature.getMinValue();
		this.range = feature.getMaxValue() - this.min;
		this.featureName = feature.getName();

		ArrayList<Float>[] redVars = new ArrayList[steps];
		ArrayList<Float>[] greenVars = new ArrayList[steps];
		ArrayList<Float>[] blueVars = new ArrayList[steps];
		ArrayList<Float>[] alphaVars = new ArrayList[steps];

		for (int i = 0; i < steps; i++) {
			redVars[i] = new ArrayList<Float>();
			greenVars[i] = new ArrayList<Float>();
			blueVars[i] = new ArrayList<Float>();
			alphaVars[i] = new ArrayList<Float>();
		}

		ElementData[] data = this.dataHub.getData();
		float summand = 1.f / data.length;

		for (int i = 0; i < data.length; i++) {
			float value = data[i].getValue(feature);

			if (!Float.isNaN(value)) {
				int index = (int) Math.floor((value - this.min) / this.range * steps);

				if (index >= steps) {
					index = steps - 1;
				}

				if (index < 0) {
					index = 0;
				}

				this.values[index] += summand;

				float alphaFactor = 1.f;

				if (selection) {
					alphaFactor = this.selectionController.isSelected(data[i].getId()) ? 1.f
							: NON_SELECTED_ALPHA_FACTOR;
				}

				// get color
				Color color = ViewUtils.calcColor(data[i]);
				redVars[index].add(color.getRed() / 255.f);
				greenVars[index].add(color.getGreen() / 255.f);
				blueVars[index].add(color.getBlue() / 255.f);
				alphaVars[index].add(color.getAlpha() / 255.f * alphaFactor);

				count[index]++;
			}
		}

		// calc color
		for (int i = 0; i < steps; i++) {
			Float[] redVarsTmp = new Float[redVars[i].size()];
			redVars[i].toArray(redVarsTmp);
			float[] redVarsNative = ArrayUtils.toPrimitive(redVarsTmp);

			Float[] greenVarsTmp = new Float[greenVars[i].size()];
			greenVars[i].toArray(greenVarsTmp);
			float[] greenVarsNative = ArrayUtils.toPrimitive(greenVarsTmp);

			Float[] blueVarsTmp = new Float[blueVars[i].size()];
			blueVars[i].toArray(blueVarsTmp);
			float[] blueVarsNative = ArrayUtils.toPrimitive(blueVarsTmp);

			Float[] alphaVarsTmp = new Float[alphaVars[i].size()];
			alphaVars[i].toArray(alphaVarsTmp);
			float[] alphaVarsNative = ArrayUtils.toPrimitive(alphaVarsTmp);

			this.red[i] = ViewUtils.combineComponent(redVarsNative, alphaVarsNative, count[i]);
			this.green[i] = ViewUtils.combineComponent(greenVarsNative, alphaVarsNative, count[i]);
			this.blue[i] = ViewUtils.combineComponent(blueVarsNative, alphaVarsNative, count[i]);
			this.alpha[i] = ViewUtils.combineAlpha(alphaVarsNative, count[i]);
		}

		// scale up
		this.updateMaxFrequency(steps);
	}

	@Override
	protected synchronized void uploadData(GL2 gl) {
		// do nothing
	}

	@Override
	protected void draw(Graphics2D g2d) {
		// calc some things
		float width = this.getShapeWidth() - 2 * BORDER_SIZE;
		float barWidth = width / stepCount;
		float height = this.getShapeHeight() - 2 * BORDER_SIZE;

		// draw bars
		for (int i = 0; i < stepCount; i++) {
			g2d.setColor(new Color(this.red[i], this.green[i], this.blue[i], this.alpha[i]));
			g2d.fillRect(Math.round(i * barWidth + BORDER_SIZE), BORDER_SIZE, Math.round(barWidth), Math.round(height
					* this.values[i] / this.maxFrequency));
		}
		g2d.setColor(Color.BLACK);

		// draw axis
		this.drawAxis(g2d, BORDER_SIZE, BORDER_SIZE, height, Settings.getInstance().getResourceBundle().getString(
				"histogramFrequency"), 0.f, this.maxFrequency, false);
		this.drawAxis(g2d, BORDER_SIZE, BORDER_SIZE, width, this.featureName, this.min, this.min + this.range, true);
	}

	/**
	 * Updates the maxFrequency value.
	 */
	private synchronized void updateMaxFrequency(int steps) {
		this.maxFrequency = 0.f;

		for (int i = 0; i < steps; i++) {
			this.maxFrequency = Math.max(this.maxFrequency, this.values[i]);
		}
	}

	@Override
	protected void registerShortcuts() {
		// ignore
	}

	@Override
	protected void unregisterShortcuts() {
		// ignore
	}
}
