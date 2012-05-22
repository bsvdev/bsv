package gui.views.plots;

import gui.bsvComponents.BSVSlider;
import gui.bsvComponents.DragDropList;
import gui.main.EventController;
import gui.settings.Settings;
import gui.views.ViewUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.common.nio.Buffers;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * This class implements the indicator plot. It uses OpenGL for fast rendering.
 */
public class IndicatorPlot extends GLPlot {
	private static final long serialVersionUID = -7905616496981074075L;

	/**
	 * Sets the size of the white borders around the plot.
	 */
	private static final int BORDER_SIZE = 60;

	/**
	 * Sets the width of the lines indicating the feature values.
	 */
	private static final float LINES_WIDTH = 2.f;

	/**
	 * Sets steps of alpha slider.
	 */
	private static final int ALPHA_SLIDER_STEPS = 50;

	/**
	 * Controls calculation of alpha slider.
	 */
	private static final float ALPHA_SLIDER_BASE = 1.08f;

	/**
	 * Sets influence of number of elements to alpha slider.
	 */
	private static final float ALPHA_SLIDER_ELEMENT_BASE = 500.f;

	/**
	 * Controls alpha of non selected objects.
	 */
	private static final float NON_SELECTED_ALPHA = 0.1f;

	/**
	 * Controls alpha of non selected elements depending on number of shown elements.
	 */
	private static final float NON_SELECTED_ALPHA_BASE = 4.f;

	/**
	 * Sets initial count of features.
	 */
	private static final int INITIAL_FEATURE_COUNT = 10;

	/**
	 * Controls alpha of selection bars.
	 */
	private static final float SELECTION_ALPHA = 0.35f;

	/**
	 * Controls alpha of active selection bar.
	 */
	private static final float SELECTION_ALPHA_ACTIVE = 0.7f;

	/**
	 * Controls width of selection bar width.
	 */
	private static final float SELECTION_WIDTH = 20.f;

	/**
	 * Action that updates view after resorting features.
	 */
	private AbstractAction updateSortingAction;

	/**
	 * Action that does autosort.
	 */
	private AbstractAction autoSortAction;

	/**
	 * Action that does clear selection.
	 */
	private AbstractAction clearSelectionAction;

	/**
	 * Vertex buffer that stores the location of the points.
	 *
	 * Is used for fast redrawing and represents a buffer on graphics card memory. It is not used global but is a member
	 * variable to avoid garbage collection and buffer freeing.
	 */
	private FloatBuffer vbuffer;

	/**
	 * Color buffer that stores the color and transparency of points. See {@link #vbuffer} for more details.
	 */
	private FloatBuffer cbuffer;

	/**
	 * Buffer that stores ids.
	 */
	private IntBuffer ibuffer;

	/**
	 * Stores the number of elements to draw.
	 */
	private int elementCount;

	/**
	 * Stores the number of features to draw.
	 */
	private int featureCount;

	/**
	 * Stores the names of the features.
	 */
	private String[] featureNames;

	/**
	 * Stores the minimum value of every feature.
	 */
	private float[] min;

	/**
	 * Stores the maximum value of every feature.
	 */
	private float[] range;

	/**
	 * Stores the alpha for the lines.
	 */
	private float lineAlpha;

	/**
	 * Stores OpenGL id of our shader program.
	 */
	private int shaderProgram;

	/**
	 * Stores pointer for shader alpha parameter.
	 */
	private int shaderUniformAlpha;

	/**
	 * Drag and drop list used for sorting features.
	 */
	private DragDropList sortList;

	/**
	 * Scroll pane for drag and drop list.
	 */
	private JScrollPane sortListScrollPane;

	/**
	 * Button to update shown features.
	 */
	private JButton sortButton;

	/**
	 * Stores features to draw.
	 */
	private Feature[] features;

	/**
	 * Controller for user interaction.
	 */
	private InteractionController icontroller;

	/**
	 * Constructs a indicator plot using a DataHub, a SelectionController and a SubspaceController.
	 *
	 * @param dataHub
	 *            a preinitialized DataHub.
	 * @param selectionController
	 *            a preinitialized SelectionController.
	 * @param subspaceController
	 *            a preinitialized SubspaceController.
	 */
	public IndicatorPlot(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		super(dataHub, selectionController, subspaceController);
	}

	@Override
	protected void createUI() {
		// init actions
		this.updateSortingAction = new UpdateSortingAction();
		this.autoSortAction = new AutoSortAction();
		this.clearSelectionAction = new ClearSelectionAction();

		this.icontroller = new InteractionController();
		this.setInteractionHandler(this.icontroller);

		this.lineAlpha = 1.f;

		JSlider alphaSlider = new BSVSlider(0, ALPHA_SLIDER_STEPS, ALPHA_SLIDER_STEPS);
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting()) {
					// scale exponential
					if (source.getValue() > 0) {
						float elementFactor = (float) Math.max(1.f, Math.log(elementCount)
								/ Math.log(ALPHA_SLIDER_ELEMENT_BASE));
						lineAlpha = (float) (Math.pow(ALPHA_SLIDER_BASE, (source.getValue() - ALPHA_SLIDER_STEPS)
								* elementFactor));
					} else {
						lineAlpha = 0.f;
					}

					rerender();
				}
			}
		});

		JPanel alphaPanel = new JPanel(new GridLayout(0, 1));
		alphaPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"indicatorLineAlpha")));
		alphaPanel.setPreferredSize(new Dimension(100, (int) alphaSlider.getPreferredSize().getHeight()));
		alphaPanel.add(alphaSlider);

		JPanel sortPanel = new JPanel(new GridLayout(0, 1));
		sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.Y_AXIS));
		sortPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"indicatorFeaturePanel")));
		this.sortList = new DragDropList(new Feature[0]);
		this.sortListScrollPane = new JScrollPane(this.sortList);
		this.sortListScrollPane.setPreferredSize(new Dimension(160, (int) this.sortListScrollPane.getMaximumSize()
				.getHeight()));
		sortPanel.add(this.sortListScrollPane);

		Dimension buttonSize = new Dimension(28, 28);

		this.sortButton = new JButton(this.updateSortingAction);
		this.sortButton.setToolTipText(Settings.getInstance().getResourceBundle().getString("indicatorSortUpdate"));

		// load image or fall back to text
		try {
			this.sortButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/indicator_update.png"))));

		} catch (IOException e) {
			this.sortButton.setText(Settings.getInstance().getResourceBundle().getString("indicatorSortUpdate"));
		}

		// size of button
		this.sortButton.setPreferredSize(buttonSize);
		this.sortButton.setMaximumSize(buttonSize);

		JButton autoButton = new JButton(this.autoSortAction);
		autoButton.setText(Settings.getInstance().getResourceBundle().getString("indicatorAutosort"));
		JPanel updatePanel = new JPanel(new GridLayout(0, 1));
		updatePanel.setBackground(Color.WHITE);
		updatePanel.add(this.sortButton);
		updatePanel.add(autoButton);
		sortPanel.add(updatePanel);

		JPanel controlPanel = new JPanel(new GridLayout(0, 1));
		controlPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"indicatorControlPanel")));
		JButton clearselectionButton = new JButton(this.clearSelectionAction);
		clearselectionButton.setText(Settings.getInstance().getResourceBundle().getString("indicatorClearselection"));
		controlPanel.add(clearselectionButton);

		this.addToSidebar(alphaPanel);
		this.addToSidebar(sortPanel);
		this.addToSidebar(controlPanel);
	}

	@Override
	protected void registerShortcuts() {
		EventController.getInstance().setAction(this.clearSelectionAction, "eventResetSelection");
	}

	@Override
	protected void unregisterShortcuts() {
		EventController.getInstance().removeAction("eventResetSelection");
	}

	@Override
	protected void setFeatures(Feature[] features) {
		this.features = features;

		this.sortList = new DragDropList(this.features);
		this.sortList.addSelectionInterval(0, Math.max(0, Math.min(this.features.length, INITIAL_FEATURE_COUNT) - 1));
		this.sortListScrollPane.setViewportView(this.sortList);

		this.sortButton.doClick();
	}

	@Override
	protected void init(GL2 gl) {
		gl.glEnable(GL2.GL_LINE_SMOOTH);

		int f = this.setupShader(gl, GL2.GL_FRAGMENT_SHADER, "/indicatorplot_fragmet.glsl");

		this.shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		this.shaderUniformAlpha = gl.glGetUniformLocation(this.shaderProgram, "alpha");
	}

	@Override
	protected void rescale(GL2 gl) {
		this.icontroller.setShapeX(this.getShapeX() + BORDER_SIZE);
		this.icontroller.setShapeY(this.getShapeY() + BORDER_SIZE);
		this.icontroller.setShapeWidth(this.getShapeWidth() - 2 * BORDER_SIZE);
		this.icontroller.setShapeHeight(this.getShapeHeight() - 2 * BORDER_SIZE);
	}

	@Override
	protected void draw(GL2 gl) {
		float stepSize = (float) (this.getShapeWidth() - 2 * BORDER_SIZE) / (float) (this.featureCount - 1);
		float axisLength = this.getShapeHeight() - 2 * BORDER_SIZE;

		// draw lines
		if ((this.elementCount > 0) && (this.featureCount > 1)) {
			gl.glUseProgram(this.shaderProgram);
			gl.glUniform1f(this.shaderUniformAlpha, lineAlpha);

			float scaleFactorX = this.getShapeWidth() - 2 * BORDER_SIZE;
			float scaleFactorY = this.getShapeHeight() - 2 * BORDER_SIZE;

			gl.glPushMatrix();
			gl.glTranslatef(BORDER_SIZE, BORDER_SIZE, 0.f);
			gl.glScalef(scaleFactorX, scaleFactorY, 1.f);
			gl.glLineWidth(LINES_WIDTH);
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			gl.glDrawArrays(GL2.GL_LINES, 0, this.elementCount * (this.featureCount - 1) * 2);
			gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
			gl.glLineWidth(1.f);
			gl.glPopMatrix();

			gl.glUseProgram(0);
		}

		// draw selection
		gl.glPushMatrix();
		gl.glTranslatef(BORDER_SIZE, BORDER_SIZE, 0.f);
		for (int i = 0; i < this.featureCount; i++) {
			if (this.icontroller.isSelection(i)) {
				if (this.icontroller.getActiveAxis() == i) {
					gl.glColor4f(0.f, 0.f, 0.f, SELECTION_ALPHA_ACTIVE);
				} else {
					gl.glColor4f(0.f, 0.f, 0.f, SELECTION_ALPHA);
				}
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex2f(-SELECTION_WIDTH / 2.f, this.icontroller.getSelectionMin(i) * axisLength);
				gl.glVertex2f(-SELECTION_WIDTH / 2.f, this.icontroller.getSelectionMax(i) * axisLength);
				gl.glVertex2f(SELECTION_WIDTH / 2.f, this.icontroller.getSelectionMax(i) * axisLength);
				gl.glVertex2f(SELECTION_WIDTH / 2.f, this.icontroller.getSelectionMin(i) * axisLength);
				gl.glEnd();
			}

			gl.glTranslatef(stepSize, 0.f, 0.f);
		}

		gl.glColor4f(0.f, 0.f, 0.f, 1.f);
		gl.glPopMatrix();

		// draw axis
		if (this.featureCount > 1) {
			for (int i = 0; i < this.featureCount; i++) {
				this.drawAxis(gl, i * stepSize + BORDER_SIZE, BORDER_SIZE, Math.round(axisLength),
						this.featureNames[i], this.min[i], this.min[i] + this.range[i], false);
			}
		}
	}

	@Override
	protected void dispose(GL2 gl) {
		this.cleanupVBO(gl);
	}

	@Override
	public String getName() {
		return Settings.getInstance().getResourceBundle().getString("indicatorplot");
	}

	@Override
	protected synchronized void processData() throws DatabaseAccessException, InterruptedException {
		final Feature[] features = this.features;
		final boolean selection = this.selectionController.isSomethingSelected();
		this.featureCount = features.length;
		this.featureNames = new String[this.featureCount];
		this.min = new float[this.featureCount];
		this.range = new float[this.featureCount];

		for (int i = 0; i < this.featureCount; i++) {
			this.featureNames[i] = features[i].getName();
			this.min[i] = features[i].getMinValue();
			this.range[i] = features[i].getMaxValue() - this.min[i];

			if (this.range[i] == 0) {
				this.range[i] = 1;
			}
		}

		ElementData[] data = this.dataHub.getData();
		this.elementCount = data.length;

		if ((this.elementCount > 0) && (this.featureCount > 1)) {
			this.vbuffer = Buffers.newDirectFloatBuffer(this.elementCount * (this.featureCount - 1) * 2 * 2);
			this.cbuffer = Buffers.newDirectFloatBuffer(this.elementCount * (this.featureCount - 1) * 2 * 4);
			this.ibuffer = Buffers.newDirectIntBuffer(this.elementCount);
			final float nonSelectedAlphaFactor = (float) (NON_SELECTED_ALPHA * Math.min(1.f, (Math
					.log(NON_SELECTED_ALPHA_BASE) / Math.log(this.elementCount))));
			final float deltaX = 1.f / (this.featureCount - 1);

			for (int i = 0; i < this.elementCount; i++) {
				float alphaFactor = 1.f;

				if (selection) {
					alphaFactor = this.selectionController.isSelected(data[i].getId()) ? 1.f : nonSelectedAlphaFactor;
				}

				Color color = ViewUtils.calcColor(data[i]);

				final float red = color.getRed() / 255.f;
				final float green = color.getGreen() / 255.f;
				final float blue = color.getBlue() / 255.f;
				final float alpha = color.getAlpha() / 255.f * alphaFactor;

				for (int j = 0; j < this.featureCount - 1; j++) {
					for (int k = 0; k < 2; k++) {
						this.vbuffer.put((j + k) * deltaX);
						this.vbuffer.put((data[i].getValue(features[j + k]) - this.min[j + k]) / this.range[j + k]);

						// color
						this.cbuffer.put(red);
						this.cbuffer.put(green);
						this.cbuffer.put(blue);
						this.cbuffer.put(alpha);
					}
				}

				this.ibuffer.put(data[i].getId());
			}

			this.vbuffer.rewind();
			this.cbuffer.rewind();
			this.ibuffer.rewind();
		}

		this.icontroller.setAxisCount(this.featureCount);
	}

	@Override
	protected synchronized void uploadData(GL2 gl) {
		this.cleanupVBO(gl);

		int nVBO = 2;
		this.vbo = new int[nVBO];
		gl.glGenBuffers(nVBO, vbo, 0);

		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.vbo[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, this.vbuffer.limit() * Buffers.SIZEOF_FLOAT, this.vbuffer,
				GL2.GL_STATIC_DRAW);
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.vbo[1]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, this.cbuffer.limit() * Buffers.SIZEOF_FLOAT, this.cbuffer,
				GL2.GL_STATIC_DRAW);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, 0);

		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}

	@Override
	protected void draw(Graphics2D g2d) {
		float stepSize = (float) (this.getShapeWidth() - 2 * BORDER_SIZE) / (float) (this.featureCount - 1);
		float axisLength = this.getShapeHeight() - 2 * BORDER_SIZE;

		// draw lines
		float scaleFactorX = this.getShapeWidth() - 2 * BORDER_SIZE;
		float scaleFactorY = this.getShapeHeight() - 2 * BORDER_SIZE;
		g2d.setStroke(new BasicStroke(LINES_WIDTH));
		for (int i = 0; i < this.elementCount; i++) {
			g2d.setColor(new Color(this.cbuffer.get(i * (this.featureCount - 1) * 2 * 4), this.cbuffer.get(i
					* (this.featureCount - 1) * 2 * 4 + 1), this.cbuffer.get(i * (this.featureCount - 1) * 2 * 4 + 2),
					this.cbuffer.get(i * (this.featureCount - 1) * 2 * 4 + 3) * this.lineAlpha));

			int coordElementOffset = i * (this.featureCount - 1) * 2 * 2;

			for (int j = 0; j < this.featureCount - 1; j++) {
				int k = coordElementOffset + j * 2 * 2;

				g2d.drawLine(Math.round(this.vbuffer.get(k) * scaleFactorX) + BORDER_SIZE, Math.round(this.vbuffer
						.get(k + 1)
						* scaleFactorY)
						+ BORDER_SIZE, Math.round(this.vbuffer.get(k + 2) * scaleFactorX) + BORDER_SIZE, Math
						.round(this.vbuffer.get(k + 3) * scaleFactorY)
						+ BORDER_SIZE);
			}
		}

		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke());

		// draw selection
		g2d.setColor(new Color(0.f, 0.f, 0.f, SELECTION_ALPHA));

		for (int i = 0; i < this.featureCount; i++) {
			if (this.icontroller.isSelection(i)) {
				g2d.fillRect(Math.round(i * stepSize - SELECTION_WIDTH / 2.f + BORDER_SIZE), Math
						.round(this.icontroller.getSelectionMin(i) * axisLength + BORDER_SIZE), Math
						.round(SELECTION_WIDTH), Math.round((this.icontroller.getSelectionMax(i) - this.icontroller
						.getSelectionMin(i))
						* axisLength));
			}
		}

		g2d.setColor(Color.BLACK);

		// draw axis
		if (this.featureCount > 1) {
			for (int i = 0; i < this.featureCount; i++) {
				this.drawAxis(g2d, i * stepSize + BORDER_SIZE, BORDER_SIZE, Math.round(axisLength),
						this.featureNames[i], this.min[i], this.min[i] + this.range[i], false);
			}
		}
	}

	/**
	 * Cleanup GL memory.
	 *
	 * @param gl
	 *            the render context.
	 */
	private void cleanupVBO(GL2 gl) {
		if (this.vbo != null) {
			gl.glDeleteBuffers(this.vbo.length, this.vbo, 0);
			this.vbo = null;
		}
	}

	/**
	 * Class that handles user interaction with the scatterplot.
	 */
	private class InteractionController extends MouseAdapter implements InteractionHandler {

		/**
		 * Controls minimum selection size.
		 */
		private static final float SELECTION_SIZE_MIN = 0.01f;

		/**
		 * Controls threshold of selection update when moving the mouse.
		 */
		private static final int SELECTION_THRESHOLD = 10;

		/**
		 * Stores if user interaction is paused.
		 */
		private boolean pause = false;

		/**
		 * Stores last used y mouse coordinates.
		 */
		private int lastY;

		/**
		 * Stores begin of active selection.
		 */
		private float selectionBegin;

		/**
		 * Stores axis count.
		 */
		private int axisCount;

		/**
		 * Stores number of active axis, {@code -1} if there is no active axis.
		 */
		private int activeAxis;

		/**
		 * Stores selection minimum of a specified axis.
		 */
		private float[] selectionMin;

		/**
		 * Stores selection maximum of a specified axis.
		 */
		private float[] selectionMax;

		/**
		 * Stores if there is a selection of a specified axis.
		 */
		private boolean[] selection;

		/**
		 * Flags that next selection reset should be ignored.
		 */
		private boolean ignoreNextSelectionReset = false;

		/**
		 * Stores x position of drawing shape.
		 */
		private int shapeX;

		/**
		 * Stores y position of drawing shape.
		 */
		private int shapeY;

		/**
		 * Stores width of drawing shape.
		 */
		private int shapeWidth;

		/**
		 * Stores height of drawing shape.
		 */
		private int shapeHeight;

		/**
		 * Sets x position of drawing shape.
		 *
		 * @param x
		 *            x position of drawing shape.
		 */
		public void setShapeX(int x) {
			this.shapeX = x;
		}

		/**
		 * Sets y position of drawing shape.
		 *
		 * @param y
		 *            y position of drawing shape.
		 */
		public void setShapeY(int y) {
			this.shapeY = y;
		}

		/**
		 * Sets drawing shape width.
		 *
		 * @param width
		 *            width of drawing shape.
		 */
		public void setShapeWidth(int width) {
			this.shapeWidth = width;
		}

		/**
		 * Sets drawing shape height.
		 *
		 * @param height
		 *            height of drawing shape.
		 */
		public void setShapeHeight(int height) {
			this.shapeHeight = height;
		}

		/**
		 * Reset selection of all axis (not the selection controller).
		 */
		public void resetSelection() {
			if (this.ignoreNextSelectionReset) {
				this.ignoreNextSelectionReset = false;
			} else {
				this.selectionMin = new float[this.axisCount];
				this.selectionMax = new float[this.axisCount];
				this.selection = new boolean[this.axisCount];
				this.activeAxis = -1;
			}
		}

		/**
		 * Sets number of axis.
		 *
		 * @param c
		 *            number of axis.
		 */
		public void setAxisCount(int c) {
			this.axisCount = c;
			this.resetSelection();
		}

		/**
		 * Get number of active axis of user interaction.
		 *
		 * @return number of the active axis.
		 */
		public int getActiveAxis() {
			return this.activeAxis;
		}

		/**
		 * Checks if a specified axis has a selection.
		 *
		 * @param axis
		 *            number of axis.
		 * @return {@code true} if there is a selection, {@code false} otherwise.
		 */
		public boolean isSelection(int axis) {
			return this.selection[axis];
		}

		/**
		 * Gets selection minimum for a specified axis.
		 *
		 * @param axis
		 *            number of axis.
		 * @return selection minimum.
		 */
		public float getSelectionMin(int axis) {
			return this.selectionMin[axis];
		}

		/**
		 * Gets selection maximum for a specified axis.
		 *
		 * @param axis
		 *            number of axis.
		 * @return selection maximum.
		 */
		public float getSelectionMax(int axis) {
			return this.selectionMax[axis];
		}

		@Override
		public void pause() {
			this.pause = true;
		}

		@Override
		public void resume() {
			this.pause = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!this.pause) {
				float mX = (float) (e.getX() - this.shapeX) / (float) this.shapeWidth;
				int axis = Math.round(mX * (this.axisCount - 1));

				if ((axis >= 0) && (axis < this.axisCount)) {
					float mY = 1.f - (float) (e.getY() - this.shapeY) / (float) this.shapeHeight;
					this.activeAxis = axis;
					this.selectionBegin = mY;

					this.selection[this.activeAxis] = true;
					this.selectionMin[this.activeAxis] = Math.max(0.f, Math.min(1.f, this.selectionBegin));
					this.selectionMax[this.activeAxis] = Math.max(0.f, Math.min(1.f, this.selectionBegin));

					this.lastY = e.getY();
				}

				rerender();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!this.pause) {
				if (this.activeAxis != -1) {
					if (this.selectionMax[this.activeAxis] - this.selectionMin[this.activeAxis] >= SELECTION_SIZE_MIN) {
						this.updateSelection(e.getY());
					} else {
						this.selection[this.activeAxis] = false;
					}
				}

				this.activeAxis = -1;

				int[] ids = new int[elementCount];
				int idPos = 0;

				for (int i = 0; i < elementCount; i++) {
					boolean inSelection = true;

					for (int j = 0; (j < featureCount) && inSelection; j++) {
						if (this.selection[j]) {
							int offset = i * 2 * (featureCount - 1) * 2;
							int pos = offset + 2 * j * 2 + 1;
							if (j == featureCount - 1) {
								pos -= 2;
							}
							float value = vbuffer.get(pos);
							if (!((value >= this.selectionMin[j]) && (value <= this.selectionMax[j]))) {
								inSelection = false;
							}
						}
					}

					if (inSelection) {
						ids[idPos++] = ibuffer.get(i);
					}
				}

				this.ignoreNextSelectionReset = true;
				selectionController.reselect(ids);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!this.pause && this.activeAxis != -1) {
				if (Math.abs(e.getY() - this.lastY) > SELECTION_THRESHOLD) {
					this.updateSelection(e.getY());
					this.lastY = e.getY();

					rerender();
				}
			}
		}

		/**
		 * Update selection of active bar.
		 *
		 * @param y
		 *            current mouse y coordinate.
		 */
		private void updateSelection(int y) {
			float mY = 1.f - (float) (y - this.shapeY) / (float) this.shapeHeight;
			this.selectionMin[this.activeAxis] = Math.max(0.f, Math.min(1.f, Math.min(this.selectionBegin, mY)));
			this.selectionMax[this.activeAxis] = Math.max(0.f, Math.min(1.f, Math.max(this.selectionBegin, mY)));
		}
	}

	/**
	 * Update plot after resorting.
	 */
	private class UpdateSortingAction extends AbstractAction {
		private static final long serialVersionUID = -2426970417360022433L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Object[] elements = sortList.getElements();
			int n = 0;
			Feature[] tmp = new Feature[elements.length];

			for (int i = 0; i < elements.length; i++) {
				if (sortList.isSelectedIndex(i)) {
					tmp[n] = (Feature) elements[i];
					n++;
				}
			}

			features = new Feature[n];
			System.arraycopy(tmp, 0, features, 0, n);

			update(null, null);
		}

	}

	/**
	 * Do autosort.
	 */
	private class AutoSortAction extends AbstractAction {
		private static final long serialVersionUID = 5140891617940951040L;

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Object[] elements = sortList.getElements();
				LinkedList<Feature> featuresOut = new LinkedList<Feature>();
				LinkedList<Feature> featuresNorm = new LinkedList<Feature>();

				for (int i = 0; i < elements.length; i++) {
					Feature f = (Feature) elements[i];

					if (f.isOutlier()) {
						featuresOut.add(f);
					} else {
						featuresNorm.add(f);
					}
				}

				Feature[] toSort = new Feature[featuresNorm.size()];
				featuresNorm.toArray(toSort);
				toSort = ViewUtils.autoSort(toSort, dataHub);
				featuresNorm = new LinkedList<Feature>(Arrays.asList(toSort));

				LinkedList<Feature> result = new LinkedList<Feature>();

				if (featuresOut.size() == 1) {
					result.addAll(featuresOut);
					result.addAll(featuresNorm);
				} else {
					result.addAll(featuresNorm);
					result.addAll(featuresOut);
				}

				Feature[] resultArray = new Feature[result.size()];
				result.toArray(resultArray);

				setFeatures(resultArray);
			} catch (DatabaseAccessException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Clear selection.
	 */
	private class ClearSelectionAction extends AbstractAction {
		private static final long serialVersionUID = 7398990770316996231L;

		@Override
		public void actionPerformed(ActionEvent e) {
			icontroller.resetSelection();
			selectionController.reset();
		}
	}
}
