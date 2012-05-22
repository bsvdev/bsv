package gui.views.plots;

import gui.bsvComponents.BSVComboBox;
import gui.main.EventController;
import gui.settings.Settings;
import gui.views.ViewUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
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
 * This class implements a scatter plot. It uses OpenGL for accelerated drawing.
 */
public class ScatterPlot extends GLPlot {
	private static final long serialVersionUID = 7401836444479998149L;

	/**
	 * Defines modes of ScatterPlot
	 */
	private static enum MODES {
		SCROLL_ZOOM, BOXZOOM, LASSO
	};

	/**
	 * Sets the size of the surrounding where axis and text is drawing and no points.
	 */
	private static final int AXIS_PART_SIZE = 60;

	/**
	 * Sets the size of the points in pixel.
	 */
	private static final float DOTSIZE = 4.f;

	/**
	 * Controls width of lines for selection tools.
	 */
	private static final float TOOL_LINES_WIDTH = 2.f;

	/**
	 * Controls alpha of non selected objects.
	 */
	private static final float NON_SELECTED_ALPHA = 0.2f;

	/**
	 * Controls alpha of non selected elements depending on number of shown elements.
	 */
	private static final float NON_SELECTED_ALPHA_BASE = 500.f;

	/**
	 * Action that sets mode to scroll and zoom.
	 */
	private AbstractAction setModeScrollzoomAction;

	/**
	 * Action that sets mode to box zoom.
	 */
	private AbstractAction setModeBoxzoomAction;

	/**
	 * Action that sets mode to lasso.
	 */
	private AbstractAction setModeLassoAction;

	/**
	 * Action that resets the view port.
	 */
	private AbstractAction resetViewportAction;

	/**
	 * Action that clears selection.
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
	 * Id buffer that stores ids of points.
	 */
	private IntBuffer ibuffer;

	/**
	 * Stores the number of points that should be drawn.
	 */
	private int n;

	/**
	 * Stores the size in pixel that represents the height and width of the rectangular of the plot.
	 */
	private int size;

	/**
	 * The InteractionController that is used as mouse listener and state storage.
	 */
	private InteractionController icontroller;

	/**
	 * The name of the X axis.
	 */
	private String nameX;

	/**
	 * The name of the Y axis.
	 */
	private String nameY;

	/**
	 * Stores the x-axis feature.
	 */
	private Feature featureX;

	/**
	 * Stores the y-axis feature.
	 */
	private Feature featureY;

	/**
	 * Stores the range of x values.
	 */
	private float rangeX;

	/**
	 * Stores the range of y values.
	 */
	private float rangeY;

	/**
	 * Stores minimum x value.
	 */
	private float minX;

	/**
	 * Stores minimum y value.
	 */
	private float minY;

	/**
	 * Combo box for x feature.
	 */
	private JComboBox comboX;

	/**
	 * Combo box for y feature.
	 */
	private JComboBox comboY;

	/**
	 * Radio button for scroll zoom mode.
	 */
	private JToggleButton scrollZoomButton;

	/**
	 * Radio button for box zoom mode.
	 */
	private JToggleButton boxzoomButton;

	/**
	 * Radio button for lasso mode
	 */
	private JToggleButton lassoButton;

	/**
	 * Constructs a scatterplot using a DataHub, a SelectionController and a SubspaceController.
	 * 
	 * @param dataHub
	 *            a preinitialized DataHub.
	 * @param selController
	 *            a preinitialized SelectionController.
	 * @param subController
	 *            a preinitialized SubspaceController.
	 */
	public ScatterPlot(DataHub dataHub, SelectionController selController, SubspaceController subController) {
		super(dataHub, selController, subController);
	}

	@Override
	protected void createUI() {
		// init actions
		this.setModeScrollzoomAction = new SetModeScrollzoomAction();
		this.setModeBoxzoomAction = new SetModeBoxzoomAction();
		this.setModeLassoAction = new SetModeLassoAction();
		this.resetViewportAction = new ResetViewportAction();
		this.clearSelectionAction = new ClearSelectionAction();

		// init interaction controller
		this.icontroller = new InteractionController();

		// feature selection
		this.comboX = new BSVComboBox(new Feature[0]);
		this.comboY = new BSVComboBox(new Feature[0]);

		JPanel panelX = new JPanel();
		JPanel panelY = new JPanel();

		panelX.add(new JLabel(Settings.getInstance().getResourceBundle().getString("xFeature")));
		panelY.add(new JLabel(Settings.getInstance().getResourceBundle().getString("yFeature")));

		panelX.add(this.comboX);
		panelY.add(this.comboY);

		panelX.setBackground(Color.WHITE);
		panelY.setBackground(Color.WHITE);

		JPanel featurePanel = new JPanel(new GridLayout(0, 1));
		featurePanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"scatterPlotFeaturePanel")));
		featurePanel.add(panelX);
		featurePanel.add(panelY);

		this.addToSidebar(featurePanel);

		this.setInteractionHandler(this.icontroller);

		this.comboX.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				featureX = (Feature) cb.getSelectedItem();
				update(null, null);
			}
		});

		this.comboY.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				featureY = (Feature) cb.getSelectedItem();
				update(null, null);
			}
		});

		// mode selection
		JPanel modePanel = new JPanel(new GridLayout(1, 0));
		modePanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"scatterPlotModePanel")));

		this.scrollZoomButton = new JToggleButton();
		this.boxzoomButton = new JToggleButton();
		this.lassoButton = new JToggleButton();

		// image tool tips
		this.scrollZoomButton.setToolTipText(Settings.getInstance().getResourceBundle().getString(
				"scatterPlotModeScrollZoom"));
		this.boxzoomButton.setToolTipText(Settings.getInstance().getResourceBundle()
				.getString("scatterPlotModeBoxzoom"));
		this.lassoButton.setToolTipText(Settings.getInstance().getResourceBundle().getString("scatterPlotModeLasso"));

		// load images or fall back to text
		try {
			this.scrollZoomButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/scatterplot_move.png"))));
			this.boxzoomButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/scatterplot_box.png"))));
			this.lassoButton.setIcon(new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream(
					"/scatterplot_lasso.png"))));

		} catch (IOException e) {
			this.scrollZoomButton.setText(Settings.getInstance().getResourceBundle().getString(
					"scatterPlotModeScrollZoom"));
			this.boxzoomButton.setText(Settings.getInstance().getResourceBundle().getString("scatterPlotModeBoxzoom"));
			this.lassoButton.setText(Settings.getInstance().getResourceBundle().getString("scatterPlotModeLasso"));
		}

		modePanel.add(this.scrollZoomButton);
		modePanel.add(this.boxzoomButton);
		modePanel.add(this.lassoButton);
		this.addToSidebar(modePanel);

		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(this.scrollZoomButton);
		modeGroup.add(this.boxzoomButton);
		modeGroup.add(this.lassoButton);

		this.scrollZoomButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();

				if (source.isSelected()) {
					icontroller.setMode(MODES.SCROLL_ZOOM);
				}
			}
		});

		this.boxzoomButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();

				if (source.isSelected()) {
					icontroller.setMode(MODES.BOXZOOM);
				}
			}
		});

		this.lassoButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JToggleButton source = (JToggleButton) e.getSource();

				if (source.isSelected()) {
					icontroller.setMode(MODES.LASSO);
				}
			}
		});

		this.resetMode();

		// reset button
		JButton resetButton = new JButton(this.resetViewportAction);
		resetButton.setText(Settings.getInstance().getResourceBundle().getString("plotReset"));

		JButton clearselectionButton = new JButton(this.clearSelectionAction);
		clearselectionButton.setText(Settings.getInstance().getResourceBundle().getString("scatterPlotResetSelection"));

		JPanel controlPanel = new JPanel(new GridLayout(0, 1));
		controlPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle().getString(
				"scatterPlatControlPanel")));
		controlPanel.add(resetButton);
		controlPanel.add(clearselectionButton);
		this.addToSidebar(controlPanel);
	}

	@Override
	protected void registerShortcuts() {
		EventController.getInstance().setAction(this.setModeScrollzoomAction, "eventModeZoomdrag");
		EventController.getInstance().setAction(this.setModeBoxzoomAction, "eventModeBox");
		EventController.getInstance().setAction(this.setModeLassoAction, "eventModeLasso");
		EventController.getInstance().setAction(this.setModeLassoAction, "eventSetSelection");
		EventController.getInstance().setAction(this.clearSelectionAction, "eventResetSelection");
	}

	@Override
	protected void unregisterShortcuts() {
		EventController.getInstance().removeAction("eventModeZoomdrag");
		EventController.getInstance().removeAction("eventModeBox");
		EventController.getInstance().removeAction("eventModeLasso");
		EventController.getInstance().removeAction("eventSetSelection");
		EventController.getInstance().removeAction("eventResetSelection");
	}

	/**
	 * Reset mode
	 */
	private void resetMode() {
		this.scrollZoomButton.setSelected(true);
	}

	@Override
	protected void setFeatures(Feature[] features) {
		this.comboX.setModel(new DefaultComboBoxModel(features));
		this.comboY.setModel(new DefaultComboBoxModel(features));

		this.featureX = (Feature) comboX.getSelectedItem();
		this.featureY = (Feature) comboY.getSelectedItem();
	}

	@Override
	protected void init(GL2 gl) {
		// enable smooth point/line drawing
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
	}

	@Override
	protected void rescale(GL2 gl) {
		this.size = Math.min(this.getShapeWidth(), this.getShapeHeight()) - 2 * AXIS_PART_SIZE;
		this.icontroller.setShapeX(this.getShapeX() + AXIS_PART_SIZE);
		this.icontroller.setShapeY(this.getShapeY());
		this.icontroller.setShapeWidth(this.getShapeWidth() - AXIS_PART_SIZE);
		this.icontroller.setShapeHeight(this.getShapeHeight() - AXIS_PART_SIZE);
	}

	@Override
	protected void draw(GL2 gl) {
		// get values from icontroller
		final float scaleX = this.icontroller.getScaleX();
		final float scaleY = this.icontroller.getScaleY();
		final float dx = this.icontroller.getDX();
		final float dy = this.icontroller.getDY();

		// draw dots
		if (this.n > 0) {
			float scaleFactorX = scaleX * this.size;
			float scaleFactorY = scaleY * this.size;

			gl.glPushMatrix();
			gl.glTranslatef(AXIS_PART_SIZE, AXIS_PART_SIZE, 0.f);
			gl.glTranslatef(dx, dy, 0);
			gl.glScalef(scaleFactorX, scaleFactorY, 1.f);
			gl.glPointSize(DOTSIZE);
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			gl.glDrawArrays(GL2.GL_POINTS, 0, this.n);
			gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
			gl.glPointSize(1.f);
			gl.glPopMatrix();
		}

		// draw box
		if (this.icontroller.isBoxActive()) {
			gl.glLineWidth(TOOL_LINES_WIDTH);
			gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glVertex2f(this.icontroller.getBoxStartX(), this.icontroller.getBoxStartY() + AXIS_PART_SIZE);
			gl.glVertex2f(this.icontroller.getBoxEndX(), this.icontroller.getBoxStartY() + AXIS_PART_SIZE);
			gl.glVertex2f(this.icontroller.getBoxEndX(), this.icontroller.getBoxEndY() + AXIS_PART_SIZE);
			gl.glVertex2f(this.icontroller.getBoxStartX(), this.icontroller.getBoxEndY() + AXIS_PART_SIZE);
			gl.glEnd();
			gl.glLineWidth(1.f);
		}

		// draw lasso
		if (this.icontroller.isLassoActive()) {
			int[] xcoords = this.icontroller.getLassoPointsX();
			int[] ycoords = this.icontroller.getLassoPointsY();

			gl.glColor4f(0.f, 0.f, 0.f, 1.f);
			gl.glLineWidth(TOOL_LINES_WIDTH);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			gl.glBegin(GL2.GL_POLYGON);

			for (int i = 0; (i < xcoords.length) && (i < ycoords.length); i++) {
				gl.glVertex2f(xcoords[i], ycoords[i] + AXIS_PART_SIZE);
			}

			gl.glEnd();
			gl.glColor4f(0.f, 0.f, 0.f, 1.f);
			gl.glLineWidth(1.f);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		}

		// draw white background beyond axis
		gl.glColor3f(1.f, 1.f, 1.f);
		gl.glRectf(0, 0, AXIS_PART_SIZE, this.getShapeHeight());
		gl.glRectf(0, 0, this.getShapeWidth(), AXIS_PART_SIZE);

		// draw axis
		gl.glPushMatrix();
		float drawWidth = this.getShapeWidth() - 2 * AXIS_PART_SIZE;
		float drawHeight = this.getShapeHeight() - 2 * AXIS_PART_SIZE;
		float drawRangeX = this.rangeX * drawWidth / this.size;
		float drawRangeY = this.rangeY * drawHeight / this.size;
		float minLabelY = (0 - dy) / (scaleY * drawHeight) * drawRangeY + this.minY;
		float maxLabelY = (drawHeight - dy) / (scaleY * drawHeight) * drawRangeY + this.minY;
		float minLabelX = (0 - dx) / (scaleX * drawWidth) * drawRangeX + this.minX;
		float maxLabelX = (drawWidth - dx) / (scaleX * drawWidth) * drawRangeX + this.minX;

		if (minLabelX > maxLabelX) {
			minLabelX = maxLabelX;
		}

		if (minLabelY > maxLabelY) {
			minLabelY = maxLabelY;
		}

		this.drawAxis(gl, AXIS_PART_SIZE, AXIS_PART_SIZE, drawHeight, nameY, minLabelY, maxLabelY, false);
		this.drawAxis(gl, AXIS_PART_SIZE, AXIS_PART_SIZE, drawWidth, nameX, minLabelX, maxLabelX, true);

		gl.glPopMatrix();
	}

	@Override
	protected void dispose(GL2 gl) {
		// nothing to do
	}

	@Override
	public String getName() {
		return Settings.getInstance().getResourceBundle().getString("scatterplot");
	}

	@Override
	protected synchronized void processData() throws DatabaseAccessException, InterruptedException {
		Feature featureX = this.featureX;
		Feature featureY = this.featureY;

		this.nameX = featureX.getName();
		this.nameY = featureY.getName();

		this.minX = featureX.getMinValue();
		this.minY = featureY.getMinValue();
		float maxX = featureX.getMaxValue();
		float maxY = featureY.getMaxValue();
		this.rangeX = maxX - this.minX;
		this.rangeY = maxY - this.minY;
		boolean selection = this.selectionController.isSomethingSelected();

		ElementData[] data = this.dataHub.getData();

		this.n = data.length;
		if (this.n > 0) {
			this.vbuffer = Buffers.newDirectFloatBuffer(this.n * 2);
			this.cbuffer = Buffers.newDirectFloatBuffer(this.n * 4);
			this.ibuffer = Buffers.newDirectIntBuffer(this.n);
			float nonSelectedAlphaFactor = (float) (NON_SELECTED_ALPHA * Math.min(1.f, (Math
					.log(NON_SELECTED_ALPHA_BASE) / Math.log(this.n))));

			for (int i = 0; i < this.n; i++) {
				this.vbuffer.put((data[i].getValue(featureX) - this.minX) / this.rangeX);
				this.vbuffer.put((data[i].getValue(featureY) - this.minY) / this.rangeY);

				float alphaFactor = 1.f;

				if (selection) {
					alphaFactor = this.selectionController.isSelected(data[i].getId()) ? 1.f : nonSelectedAlphaFactor;
				}

				// color
				Color color = ViewUtils.calcColor(data[i]);
				this.cbuffer.put(color.getRed() / 255.f);
				this.cbuffer.put(color.getGreen() / 255.f);
				this.cbuffer.put(color.getBlue() / 255.f);
				this.cbuffer.put(color.getAlpha() / 255.f * alphaFactor);

				// store id for later usage
				this.ibuffer.put(data[i].getId());
			}

			this.vbuffer.rewind();
			this.cbuffer.rewind();
			this.ibuffer.rewind();
		}
	}

	@Override
	protected synchronized void uploadData(GL2 gl) {
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, this.vbuffer);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, this.cbuffer);
	}

	@Override
	protected void draw(Graphics2D g2d) {
		// get values from icontroller
		final float scaleX = this.icontroller.getScaleX();
		final float scaleY = this.icontroller.getScaleY();
		final float dx = this.icontroller.getDX();
		final float dy = this.icontroller.getDY();
		int size = Math.min(this.getShapeWidth(), this.getShapeHeight()) - 2 * AXIS_PART_SIZE;

		// draw dots
		for (int i = 0; i < this.n; i++) {
			float x = this.vbuffer.get(i * 2) * scaleX * size + dx;
			float y = this.vbuffer.get(i * 2 + 1) * scaleY * size + dy;

			if ((x >= 0.f) && (x <= this.getShapeWidth() - 2 * AXIS_PART_SIZE) && (y >= 0.f)
					&& (y <= this.getShapeHeight() - 2 * AXIS_PART_SIZE)) {
				g2d.setColor(new Color(this.cbuffer.get(i * 4), this.cbuffer.get(i * 4 + 1), this.cbuffer
						.get(i * 4 + 2), this.cbuffer.get(i * 4 + 3)));
				g2d.fillOval(Math.round(x + AXIS_PART_SIZE - DOTSIZE / 2.f), Math.round(y + AXIS_PART_SIZE - DOTSIZE
						/ 2.f), Math.round(DOTSIZE), Math.round(DOTSIZE));
			}
		}

		g2d.setColor(Color.BLACK);

		// draw axis
		float drawWidth = this.getShapeWidth() - 2 * AXIS_PART_SIZE;
		float drawHeight = this.getShapeHeight() - 2 * AXIS_PART_SIZE;
		float drawRangeX = this.rangeX * drawWidth / size;
		float drawRangeY = this.rangeY * drawHeight / size;
		float minLabelY = (0 - dy) / (scaleY * drawHeight) * drawRangeY + this.minY;
		float maxLabelY = (drawHeight - dy) / (scaleY * drawHeight) * drawRangeY + this.minY;
		float minLabelX = (0 - dx) / (scaleX * drawWidth) * drawRangeX + this.minX;
		float maxLabelX = (drawWidth - dx) / (scaleX * drawWidth) * drawRangeX + this.minX;

		if (minLabelX > maxLabelX) {
			minLabelX = maxLabelX;
		}

		if (minLabelY > maxLabelY) {
			minLabelY = maxLabelY;
		}

		this.drawAxis(g2d, AXIS_PART_SIZE, AXIS_PART_SIZE, drawHeight, nameY, minLabelY, maxLabelY, false);
		this.drawAxis(g2d, AXIS_PART_SIZE, AXIS_PART_SIZE, drawWidth, nameX, minLabelX, maxLabelX, true);
	}

	/**
	 * This class implements a controller that can be used as mouse listener for a scatterplot.
	 * 
	 * It calculates and holds state e.g. axis scale and other results of mouse movement.
	 */
	private class InteractionController extends MouseAdapter implements InteractionHandler {

		/**
		 * Sets the size of a zoom step.
		 */
		private static final float ZOOM_STEP = 1.2f;

		/**
		 * Sets threshold of the size of a box that emits an action.
		 */
		private static final int BOX_THRESHOLD = 10;

		/**
		 * Controls zoom limit.
		 */
		private static final float SCALE_LIMIT_MAX = 50000.f;

		/**
		 * Controls negative zooming.
		 */
		private static final float SCALE_LIMIT_MIN = 0.8f;

		/**
		 * Controls the minimum distance between two lasso points.
		 */
		private static final int LASSO_DISTANCE_THRESHOLD = 10;

		/**
		 * Sets update intervals when using mouse moves.
		 */
		private static final int MOUSE_MOVE_THRESHOLD = 10;

		/**
		 * Stores mode of interaction.
		 */
		private MODES imode;

		/**
		 * Stores the x scroll level of the mouse wheel.
		 */
		private float scrollLevelX;

		/**
		 * Stores the y scroll level of the mouse wheel.
		 */
		private float scrollLevelY;

		/**
		 * Stores the scale of the x axis.
		 */
		private float scaleX;

		/**
		 * Stores the scale of the y axis.
		 */
		private float scaleY;

		/**
		 * Stores the last seen x position of the mouse.
		 */
		private int lastX;

		/**
		 * Stores the last seen y position of the mouse.
		 */
		private int lastY;

		/**
		 * Stores the translation of the x axis.
		 */
		private float dX;

		/**
		 * Stores the translation of the y axis.
		 */
		private float dY;

		/**
		 * Stores x position of render shape.
		 */
		private int shapeX;

		/**
		 * Stores y position of render shape.
		 */
		private int shapeY;

		/**
		 * Stores width of render shape.
		 */
		private int shapeWidth;

		/**
		 * Stores height of render shape.
		 */
		private int shapeHeight;

		/**
		 * Stores pause state.
		 */
		private boolean pause = false;

		/**
		 * Stores box x start.
		 */
		private float boxStartX;

		/**
		 * Stores box y start.
		 */
		private float boxStartY;

		/**
		 * Stores box x end.
		 */
		private float boxEndX;

		/**
		 * Stores box y end.
		 */
		private float boxEndY;

		/**
		 * Is there an active box.
		 */
		private boolean boxActive;

		/**
		 * X coordinates of lasso points.
		 */
		private ArrayList<Integer> lassoX;

		/**
		 * Y coordinates of lasso points.
		 */
		private ArrayList<Integer> lassoY;

		/**
		 * Is there an active lasso.
		 */
		private boolean lassoActive;

		/**
		 * Construct a new interaction controller.
		 */
		public InteractionController() {
			this.reset(false);
		}

		/**
		 * Resets view.
		 * 
		 * @param rerender
		 *            flags if OpenGL view should be rerender after reset.
		 */
		public void reset(boolean rerender) {
			this.scrollLevelX = 0.f;
			this.scrollLevelY = 0.f;
			this.scaleX = 1.f;
			this.scaleY = 1.f;
			this.lastX = 0;
			this.lastY = 0;
			this.dX = 0.f;
			this.dY = 0.f;
			this.boxActive = false;
			this.lassoActive = false;

			if (!this.pause && rerender) {
				rerender();
			}
		}

		/**
		 * Sets interaction mode.
		 * 
		 * @param m
		 *            new interaction mode.
		 */
		public void setMode(MODES m) {
			this.imode = m;
		}

		/**
		 * Returns the translation of the x axis.
		 * 
		 * @return the translation of the x axis.
		 */
		public float getDX() {
			return this.dX;
		}

		/**
		 * Returns the translation of the y axis.
		 * 
		 * @return the translation of the y axis.
		 */
		public float getDY() {
			return this.dY;
		}

		/**
		 * Returns the scale of the x axis.
		 * 
		 * @return the scale of the x axis.
		 */
		public float getScaleX() {
			return this.scaleX;
		}

		/**
		 * Returns the scale of the y axis.
		 * 
		 * @return the scale of the y axis.
		 */
		public float getScaleY() {
			return this.scaleY;
		}

		/**
		 * Set x position of render shape.
		 * 
		 * @param x
		 *            x position in pixel.
		 */
		public void setShapeX(int x) {
			this.shapeX = x;
		}

		/**
		 * set y position of render shape.
		 * 
		 * @param y
		 *            y position in pixel.
		 */
		public void setShapeY(int y) {
			this.shapeY = y;
		}

		/**
		 * Set width of render shape.
		 * 
		 * @param width
		 *            width of render shape in pixel.
		 */
		public void setShapeWidth(int width) {
			if (this.shapeWidth != 0) {
				this.dX *= (float) width / (float) this.shapeWidth;
			}

			this.shapeWidth = width;
		}

		/**
		 * Set height of render shape.
		 * 
		 * @param height
		 *            height of render shape in pixel.
		 */
		public void setShapeHeight(int height) {
			if (this.shapeHeight != 0) {
				this.dY *= (float) height / (float) this.shapeHeight;
			}

			this.shapeHeight = height;
		}

		/**
		 * Checks if there is an active box zoom selection.
		 * 
		 * @return {@code true} if box is active, {@code false} otherwise.
		 */
		public boolean isBoxActive() {
			return this.boxActive;
		}

		/**
		 * Get x coordinates of box begin in pixel.
		 * 
		 * @return x coordinates of box begin.
		 */
		public float getBoxStartX() {
			return this.boxStartX;
		}

		/**
		 * Get y coordinates of box begin in pixel.
		 * 
		 * @return Y coordinates of box begin.
		 */
		public float getBoxStartY() {
			return this.boxStartY;
		}

		/**
		 * Get x coordinates of box end in pixel.
		 * 
		 * @return x coordinates of box end.
		 */
		public float getBoxEndX() {
			return this.boxEndX;
		}

		/**
		 * Get y coordinates of box end in pixel.
		 * 
		 * @return y coordinates of box end.
		 */
		public float getBoxEndY() {
			return this.boxEndY;
		}

		/**
		 * Checks if there is an active lasso selection.
		 * 
		 * @return {@code true} if there is an active lasso, {@code false} otherwise.
		 */
		public boolean isLassoActive() {
			return this.lassoActive;
		}

		/**
		 * Get x coordinates of all lasso points.
		 * 
		 * @see #isLassoActive()
		 * @return x coordinates of all lasso points, {@code null} if there is no active lasso.
		 */
		public int[] getLassoPointsX() {
			int[] result = null;

			if (this.lassoX != null) {
				Integer[] tmp = new Integer[this.lassoX.size()];
				result = new int[this.lassoX.size()];

				this.lassoX.toArray(tmp);

				for (int i = 0; i < tmp.length; i++) {
					result[i] = tmp[i];
				}
			}

			return result;
		}

		/**
		 * Get y coordinates of all lasso points.
		 * 
		 * @see #isLassoActive()
		 * @return y coordinates of all lasso points, {@code null} if there is no active lasso.
		 */
		public int[] getLassoPointsY() {
			int[] result = null;

			if (this.lassoY != null) {
				Integer[] tmp = new Integer[this.lassoY.size()];
				result = new int[this.lassoY.size()];

				this.lassoY.toArray(tmp);

				for (int i = 0; i < tmp.length; i++) {
					result[i] = tmp[i];
				}
			}

			return result;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!this.pause) {
				float distanceX = e.getX() - this.lastX;
				float distanceY = this.shapeY + this.shapeHeight - e.getY() - this.lastY;
				float distance2 = distanceX * distanceX + distanceY * distanceY;

				if (distance2 > MOUSE_MOVE_THRESHOLD * MOUSE_MOVE_THRESHOLD) {
					if (this.imode == MODES.SCROLL_ZOOM) {
						this.dX += e.getX() - this.lastX;
						this.dY += this.shapeY + this.shapeHeight - e.getY() - this.lastY;

						this.lastX = e.getX();
						this.lastY = this.shapeY + this.shapeHeight - e.getY();

						this.checkConstraints();

						rerender();
					} else if (this.imode == MODES.BOXZOOM) {
						this.boxEndX = e.getX();
						this.boxEndY = this.shapeY + this.shapeHeight - e.getY();

						rerender();
					} else if (this.imode == MODES.LASSO) {
						this.lassoX.set(this.lassoX.size() - 1, e.getX());
						this.lassoY.set(this.lassoY.size() - 1, this.shapeY + this.shapeHeight - e.getY());

						if (distance2 > LASSO_DISTANCE_THRESHOLD * LASSO_DISTANCE_THRESHOLD) {
							this.lassoX.add(e.getX());
							this.lassoY.add(this.shapeY + this.shapeHeight - e.getY());

							this.lastX = e.getX();
							this.lastY = this.shapeY + this.shapeHeight - e.getY();
						}

						rerender();
					}
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			this.lastX = e.getX();
			this.lastY = this.shapeY + this.shapeHeight - e.getY();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!this.pause) {
				if (this.imode == MODES.SCROLL_ZOOM) {
					this.lastX = e.getX();
					this.lastY = this.shapeY + this.shapeHeight - e.getY();
				} else if (this.imode == MODES.BOXZOOM) {
					this.boxStartX = e.getX();
					this.boxEndX = e.getX();
					this.boxStartY = this.shapeY + this.shapeHeight - e.getY();
					this.boxEndY = this.shapeY + this.shapeHeight - e.getY();

					this.boxActive = true;
				} else if (this.imode == MODES.LASSO) {
					this.lastX = e.getX();
					this.lastY = this.shapeY + this.shapeHeight - e.getY();

					this.lassoX = new ArrayList<Integer>();
					this.lassoY = new ArrayList<Integer>();

					this.lassoX.add(e.getX());
					this.lassoX.add(e.getX());
					this.lassoY.add(this.shapeY + this.shapeHeight - e.getY());
					this.lassoY.add(this.shapeY + this.shapeHeight - e.getY());

					this.lassoActive = true;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!this.pause) {
				if (this.imode == MODES.BOXZOOM) {
					float x1 = Math.min(this.boxStartX, this.boxEndX);
					float x2 = Math.max(this.boxStartX, this.boxEndX);
					float y1 = Math.min(this.boxStartY, this.boxEndY);
					float y2 = Math.max(this.boxStartY, this.boxEndY);

					if ((x2 - x1 > BOX_THRESHOLD) && (y2 - y1 > BOX_THRESHOLD)) {
						float oldScaleX = this.scaleX;
						float oldScaleY = this.scaleY;

						this.scaleX *= this.shapeWidth / (x2 - x1);
						this.scaleY *= this.shapeHeight / (y2 - y1);

						this.scrollLevelX = (float) (Math.log(this.scaleX) / Math.log(ZOOM_STEP));
						this.scrollLevelY = (float) (Math.log(this.scaleY) / Math.log(ZOOM_STEP));

						this.checkConstraints();

						this.dX = (this.dX + this.shapeX - x1) * this.scaleX / oldScaleX;
						this.dY = (this.dY - y1) * this.scaleY / oldScaleY;

						this.checkConstraints();
					}

					this.boxActive = false;

					rerender();
					resetMode();
				} else if (this.imode == MODES.LASSO) {
					Polygon shape = new Polygon(this.getLassoPointsX(), this.getLassoPointsY(), this.lassoX.size());
					int[] selection = new int[n];
					int spos = 0;

					for (int i = 0; i < n; i++) {
						float x = vbuffer.get(i * 2);
						float y = vbuffer.get(i * 2 + 1);
						if (shape.contains(x * size * this.scaleX + this.dX + AXIS_PART_SIZE, y * size * this.scaleY
								+ this.dY)) {
							selection[spos++] = ibuffer.get(i);
						}
					}

					this.lassoActive = false;
					resetMode();

					selectionController.select(selection);
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!this.pause) {
				int clicks = e.getWheelRotation();

				// x scrolling
				this.scrollLevelX -= clicks;

				// y scrolling
				this.scrollLevelY -= clicks;

				float oldScaleX = this.scaleX;
				float oldScaleY = this.scaleY;
				float mX = e.getX() - this.shapeX;
				float mY = this.shapeY + this.shapeHeight - e.getY();

				// calc scale
				this.scaleX = (float) Math.pow(ZOOM_STEP, this.scrollLevelX);
				this.scaleY = (float) Math.pow(ZOOM_STEP, this.scrollLevelY);

				this.checkConstraints();

				// calc delta
				this.dX = mX - (this.scaleX / oldScaleX) * (mX - this.dX);
				this.dY = mY - (this.scaleY / oldScaleY) * (mY - this.dY);

				this.checkConstraints();

				rerender();
			}
		}

		@Override
		public void pause() {
			this.pause = true;
		}

		@Override
		public void resume() {
			this.pause = false;
		}

		/**
		 * Check view constraints.
		 */
		private void checkConstraints() {
			if (this.scaleX > SCALE_LIMIT_MAX) {
				this.scaleX = SCALE_LIMIT_MAX;
				this.scrollLevelX = (float) (Math.log(this.scaleX) / Math.log(ZOOM_STEP));
			}

			if (this.scaleY > SCALE_LIMIT_MAX) {
				this.scaleY = SCALE_LIMIT_MAX;
				this.scrollLevelY = (float) (Math.log(this.scaleY) / Math.log(ZOOM_STEP));
			}

			if (this.scaleX < SCALE_LIMIT_MIN) {
				this.scaleX = SCALE_LIMIT_MIN;
				this.scrollLevelX = (float) (Math.log(this.scaleX) / Math.log(ZOOM_STEP));
			}

			if (this.scaleY < SCALE_LIMIT_MIN) {
				this.scaleY = SCALE_LIMIT_MIN;
				this.scrollLevelY = (float) (Math.log(this.scaleY) / Math.log(ZOOM_STEP));
			}
		}
	}

	/**
	 * Set mode to scroll and zoom.
	 */
	private class SetModeScrollzoomAction extends AbstractAction {
		private static final long serialVersionUID = 3157128508764105558L;

		@Override
		public void actionPerformed(ActionEvent e) {
			scrollZoomButton.setSelected(true);
		}
	}

	/**
	 * Set mode to boxzoom.
	 */
	private class SetModeBoxzoomAction extends AbstractAction {
		private static final long serialVersionUID = -5293254066088051287L;

		@Override
		public void actionPerformed(ActionEvent e) {
			boxzoomButton.setSelected(true);
		}
	}

	/**
	 * Set mode to lasso.
	 */
	private class SetModeLassoAction extends AbstractAction {
		private static final long serialVersionUID = 4100672565393904157L;

		@Override
		public void actionPerformed(ActionEvent e) {
			lassoButton.setSelected(true);
		}
	}

	/**
	 * Reset view port.
	 */
	private class ResetViewportAction extends AbstractAction {
		private static final long serialVersionUID = -2304929064208302345L;

		@Override
		public void actionPerformed(ActionEvent e) {
			icontroller.reset(true);
		}
	}

	/**
	 * Clear selection.
	 */
	private class ClearSelectionAction extends AbstractAction {
		private static final long serialVersionUID = 5094275272711011972L;

		@Override
		public void actionPerformed(ActionEvent e) {
			selectionController.reset();
		}
	}
}
