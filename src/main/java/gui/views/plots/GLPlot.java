package gui.views.plots;

import gui.bsvComponents.BSVSpinner;
import gui.main.EventController;
import gui.settings.Settings;
import gui.views.ViewPanel;
import gui.views.ViewUtils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.IntBuffer;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.jogamp.opengl.util.awt.Screenshot;
import com.jogamp.opengl.util.awt.TextRenderer;

import controller.DataHub;
import controller.Feature;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * This class implements an abstract OpenGL based view. It prepares the render context and can be used for hardware
 * accelerated views and plots. Because this class uses an OpenGL interface, some management work must be done manually
 * e.g. some memory management.
 */
public abstract class GLPlot extends ViewPanel implements GLEventListener {
	private static final long serialVersionUID = 647149035023106625L;

	/**
	 * How many milliseconds should we count frames to calculate the fps.
	 */
	private static final int FPS_PART_LENGTH = 1000;

	/**
	 * Should we display the FPS.
	 */
	private static final boolean SHOW_FPS = false;

	/**
	 * Controls number of pixels between ticks of axis.
	 */
	private static final int PIXELS_PER_TICK = 40;

	/**
	 * Controls minimum size of saved image.
	 */
	private static final int SCREENSHOT_SIZE_MIN = 100;

	/**
	 * Controls max size of saved image.
	 */
	private static final int SCREENSHOT_SIZE_MAX = 5000;

	/**
	 * Action that shows export panel.
	 */
	private final AbstractAction showExportAction;

	/**
	 * Action that exports.
	 */
	private final AbstractAction doExportAction;

	/**
	 * Action that cancels the export.
	 */
	private final AbstractAction closeExportAction;

	/**
	 * Renderer that can be used for rendering text.
	 */
	protected TextRenderer trenderer;

	/**
	 * Render target used as overlay.
	 */
	private final GLJPanel glJPanel;

	/**
	 * Sidebar for tools and settings.
	 */
	private final JPanel sidebar;

	/**
	 * Panel for export.
	 */
	private final JPanel exportPanel;

	/**
	 * Panel for export buttons.
	 */
	private final JPanel exportButtonPanel;

	/**
	 * Panel for export size.
	 */
	private final JPanel exportSizePanel;

	/**
	 * Spinner for export image width.
	 */
	private final JSpinner exportWidthSpinner;

	/**
	 * Spinner for export image height.
	 */
	private final JSpinner exportHeightSpinner;

	/**
	 * Font used for rendering text.
	 */
	private final Font font;

	/**
	 * Stores time of the beginning of the part.
	 */
	private long fpsPartBegin = 0;

	/**
	 * Counter for rendered frames in this part.
	 */
	private long fpsPartFrames = 0;

	/**
	 * Stores current fps.
	 */
	private int fps = 0;

	/**
	 * X position of render shape.
	 */
	private int shapeX;

	/**
	 * Y position of render shape.
	 */
	private int shapeY;

	/**
	 * Width of render shape.
	 */
	private int shapeWidth;

	/**
	 * Height of render shape.
	 */
	private int shapeHeight;

	/**
	 * User interaction handler.
	 */
	private InteractionHandler ihandler;

	/**
	 * Flag, indicating the need to update shown data.
	 */
	private boolean newData = true;

	/**
	 * Flag, indicating if this view is valid. Used for exception handling.
	 */
	private boolean valid = true;

	/**
	 * Processing task.
	 */
	private Future<Boolean> processingTask;

	/**
	 * Multithreaded executer for processor.
	 */
	private final ExecutorService processorExecutor;

	/**
	 * Stores process task status.
	 */
	private boolean taskReady;

	/**
	 * Multithreaded executor for renderer.
	 */
	private final ExecutorService renderExecutor = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Flag, indicating if a screenshot should be taken when display method will be called.
	 */
	private boolean takeScreenshot = false;

	/**
	 * Width of the screenshot.
	 */
	private int screenshotWidth;

	/**
	 * Height of the screenshot.
	 */
	private int screenshotHeight;

	/**
	 * File where screenshot should be stored.
	 */
	private File screenshotFile;

	/**
	 * Stores vertex buffer object handlers.
	 */
	protected int[] vbo;

	/**
	 * Create Swing UI.
	 */
	protected abstract void createUI();

	/**
	 * Register all shortcuts for this view.
	 */
	protected abstract void registerShortcuts();

	/**
	 * Unregister all shortcuts for this view.
	 */
	protected abstract void unregisterShortcuts();

	/**
	 * Sets features in active subspace.
	 * 
	 * @param features
	 *            active features.
	 */
	protected abstract void setFeatures(Feature[] features);

	/**
	 * Will be called when the rendering context is initialized.
	 * 
	 * Should load data, allocate memory and set context options.
	 * 
	 * @param gl
	 *            the rendering context.
	 */
	protected abstract void init(GL2 gl);

	/**
	 * Will be called whenever the context will be reshaped.
	 * 
	 * @param gl
	 *            the rendering context.
	 */
	protected abstract void rescale(GL2 gl);

	/**
	 * Will be called whenever a frame should be render.
	 * 
	 * Should draw all content to the frame. No initialization, cleanup, flushing or buffer swapping is necessary.
	 * 
	 * @param gl
	 *            the rendering context.
	 */
	protected abstract void draw(GL2 gl);

	/**
	 * Will be called while the render context is destroyed.
	 * 
	 * Should do cleanups e.g. freeing memory.
	 * 
	 * @param gl
	 *            the rendering context.
	 */
	protected abstract void dispose(GL2 gl);

	/**
	 * Process all data for rendering.
	 * 
	 * @throws DatabaseAccessException
	 *             if there is an error when getting data.
	 * @throws InterruptedException
	 *             if process is interrupted.
	 */
	protected abstract void processData() throws DatabaseAccessException, InterruptedException;

	/**
	 * Transfer data to graphics card memory.
	 * 
	 * @param gl
	 *            render context.
	 */
	protected abstract void uploadData(GL2 gl);

	/**
	 * Draw plot using java Graphics2D (used for export).
	 * 
	 * @param g2d
	 *            render context.
	 */
	protected abstract void draw(Graphics2D g2d);

	/**
	 * Constructs an GLPlot using a {@link DataHub} and a {@link SelectionController}.
	 * 
	 * @param dataHub
	 *            the preinitialized DataHub.
	 * @param selectionController
	 *            the preinitialized SelectionController.
	 * @param subspaceController
	 *            the preinitialized SubspaceController.
	 */
	public GLPlot(DataHub dataHub, SelectionController selectionController, SubspaceController subspaceController) {
		super(dataHub, selectionController, subspaceController);

		// init actions
		this.showExportAction = new ShowExportAction();
		this.doExportAction = new DoExportAction();
		this.closeExportAction = new CloseExportAction();

		// layout
		super.setLayout(new BorderLayout());

		// initialize multithreaded environment
		this.processorExecutor = Executors.newSingleThreadExecutor();

		// initialize GL context
		this.glJPanel = new GLJPanel(getGLCaps());
		this.glJPanel.addGLEventListener(this);

		// load some data
		this.font = new Font("Arial", Font.BOLD, 10);
		this.trenderer = new TextRenderer(this.font);

		// add canvas to self
		this.add(this.glJPanel, BorderLayout.CENTER);

		// create sidebar
		this.sidebar = new JPanel();
		this.sidebar.setBackground(Color.WHITE);
		this.sidebar.setLayout(new BoxLayout(this.sidebar, BoxLayout.Y_AXIS));
		this.add(this.sidebar, BorderLayout.LINE_END);

		// create screenshot button
		this.exportPanel = new JPanel();
		this.exportPanel.setLayout(new BoxLayout(this.exportPanel, BoxLayout.Y_AXIS));
		this.exportButtonPanel = new JPanel(new GridLayout(0, 1));
		this.exportButtonPanel.setBackground(Color.WHITE);
		this.exportPanel.setBorder(BorderFactory.createTitledBorder(Settings.getInstance().getResourceBundle()
				.getString("glPlotPanelExport")));
		JButton screenshotButton = new JButton(this.showExportAction);
		screenshotButton.setText(Settings.getInstance().getResourceBundle().getString("glPlotSave"));
		this.exportButtonPanel.add(screenshotButton);
		this.exportPanel.add(this.exportButtonPanel);

		this.exportSizePanel = new JPanel();
		this.exportSizePanel.setLayout(new BoxLayout(this.exportSizePanel, BoxLayout.Y_AXIS));
		this.exportSizePanel.setBackground(Color.WHITE);
		this.exportWidthSpinner = new BSVSpinner(new SpinnerNumberModel(SCREENSHOT_SIZE_MIN, SCREENSHOT_SIZE_MIN,
				SCREENSHOT_SIZE_MAX, 1));
		this.exportHeightSpinner = new BSVSpinner(new SpinnerNumberModel(SCREENSHOT_SIZE_MIN, SCREENSHOT_SIZE_MIN,
				SCREENSHOT_SIZE_MAX, 1));

		this.exportWidthSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner source = (JSpinner) e.getSource();
				SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
				screenshotWidth = (Integer) model.getNumber();
			}
		});

		this.exportHeightSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner source = (JSpinner) e.getSource();
				SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
				screenshotHeight = (Integer) model.getNumber();
			}
		});

		JLabel widthLabel = new JLabel(Settings.getInstance().getResourceBundle().getString("glPlotSizeDialogWidth"));
		JLabel heightLabel = new JLabel(Settings.getInstance().getResourceBundle().getString("glPlotSizeDialogHeight"));

		JPanel widthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		widthPanel.setBackground(Color.WHITE);
		widthPanel.add(widthLabel);
		widthPanel.add(this.exportWidthSpinner);

		JPanel heightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		heightPanel.setBackground(Color.WHITE);
		heightPanel.add(heightLabel);
		heightPanel.add(this.exportHeightSpinner);

		this.exportSizePanel.add(widthPanel);
		this.exportSizePanel.add(heightPanel);

		JButton okButton = new JButton(this.doExportAction);
		okButton.setText(Settings.getInstance().getResourceBundle().getString("glPlotSizeDialogOk"));

		JButton cancelButton = new JButton(this.closeExportAction);
		cancelButton.setText(Settings.getInstance().getResourceBundle().getString("glPlotSizeDialogCancel"));

		JPanel okButtonPanel = new JPanel(new GridLayout(0, 1));
		JPanel cancelButtonPanel = new JPanel(new GridLayout(0, 1));
		okButtonPanel.setBackground(Color.WHITE);
		cancelButtonPanel.setBackground(Color.WHITE);
		okButtonPanel.add(okButton);
		cancelButtonPanel.add(cancelButton);

		this.exportSizePanel.add(okButtonPanel);
		this.exportSizePanel.add(cancelButtonPanel);

		this.exportSizePanel.setVisible(false);
		this.exportPanel.add(this.exportSizePanel);

		this.addToSidebar(exportPanel);

		// initialize user interface
		this.createUI();

		// first initialization
		this.update(this.subspaceController, null);
		this.rerender();
	}

	/**
	 * Dump image using {@code screenshotFile}, {@code screenshotWidth} and {@code screenshotHeight}.
	 */
	private void dump() {
		this.takeScreenshot = true;

		GLDrawableFactory factory = GLDrawableFactory.getFactory(getGLProfile());

		GLCapabilities caps = getGLCaps();
		caps.setDoubleBuffered(false);

		GLPbuffer pBuffer = factory.createGLPbuffer(null, caps, null, screenshotWidth, screenshotHeight, null);
		pBuffer.addGLEventListener(this);
		pBuffer.display();

		takeScreenshot = false;
	}

	/**
	 * Generate svg using {@code screenshotFile}, {@code screenshotWidth} and {@code screenshotHeight}.
	 */
	private void genSVG() throws FileNotFoundException, IOException {
		// get DOM implementation
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// create svg document
		String svgNG = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNG, "svg", null);

		// create 2d graphics
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// set size
		int oldWidth = this.shapeWidth;
		int oldHeight = this.shapeHeight;
		this.shapeWidth = this.screenshotWidth;
		this.shapeHeight = this.screenshotHeight;
		svgGenerator.setSVGCanvasSize(new Dimension(this.shapeWidth, this.shapeHeight));

		// flip y axis
		svgGenerator.translate(0, this.shapeHeight);
		svgGenerator.scale(1, -1);

		// set some defaults
		svgGenerator.setFont(this.font);

		// draw
		this.draw(svgGenerator);

		// restore size
		this.shapeWidth = oldWidth;
		this.shapeHeight = oldHeight;

		// store result
		boolean useCSS = true;
		Writer out = new OutputStreamWriter(new FileOutputStream(this.screenshotFile), "UTF8");
		svgGenerator.stream(out, useCSS);
	}

	/**
	 * Rerender OpenGL surface.
	 */
	protected final synchronized void rerender() {
		this.renderExecutor.submit(new Runnable() {
			@Override
			public void run() {
				glJPanel.display();
			}
		});
	}

	/**
	 * Sets interaction controller for this plot.
	 * 
	 * @param ihandler
	 *            the new interaction controller, can be {@code null}.
	 */
	protected final void setInteractionHandler(InteractionHandler ihandler) {
		if (this.ihandler != null) {
			this.glJPanel.removeMouseListener(this.ihandler);
			this.glJPanel.removeMouseMotionListener(this.ihandler);
			this.glJPanel.removeMouseWheelListener(this.ihandler);
		}

		this.ihandler = ihandler;

		if (this.ihandler != null) {
			this.glJPanel.addMouseListener(this.ihandler);
			this.glJPanel.addMouseMotionListener(this.ihandler);
			this.glJPanel.addMouseWheelListener(this.ihandler);
		}
	}

	/**
	 * Invalidates this plot. Can be used to signal exceptions to user.
	 */
	protected final void setInvalid() {
		this.valid = false;

		if (this.ihandler != null) {
			this.ihandler.pause();
		}
	}

	/**
	 * Adds component to sidebar, used for tools and so forth.
	 * 
	 * @param component
	 *            component that should be added.
	 */
	protected final void addToSidebar(JComponent component) {
		component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(), (int) component
				.getPreferredSize().getHeight()));
		component.setAlignmentX(Component.RIGHT_ALIGNMENT);
		component.setBackground(Color.WHITE);

		this.sidebar.add(component);
	}

	/**
	 * Get x position of render shape.
	 * 
	 * @return x position of render shape.
	 */
	protected final int getShapeX() {
		return this.shapeX;
	}

	/**
	 * Get y position of render shape.
	 * 
	 * @return y position of render shape.
	 */
	protected final int getShapeY() {
		return this.shapeY;
	}

	/**
	 * Get width of render shape.
	 * 
	 * @return width of render shape.
	 */
	protected final int getShapeWidth() {
		return this.shapeWidth;
	}

	/**
	 * Get height of render shape.
	 * 
	 * @return height of render shape.
	 */
	protected final int getShapeHeight() {
		return this.shapeHeight;
	}

	/**
	 * Draws message to user on the plot.
	 * 
	 * @param gl
	 *            render context.
	 * @param msg
	 *            message text.
	 */
	protected final void drawMessage(GL2 gl, String msg) {
		this.trenderer.beginRendering(this.getShapeWidth(), this.getShapeHeight());
		this.trenderer.setColor(0.f, 0.f, 0.f, 1.f);
		Rectangle2D msgRec = this.trenderer.getBounds(msg);
		this.trenderer.draw(msg, (int) Math.round((this.getShapeWidth() - msgRec.getWidth()) / 2), (int) Math
				.round((this.getShapeHeight() - msgRec.getHeight()) / 2));
		this.trenderer.endRendering();
	}

	/**
	 * Draw an axis on the current location.
	 * 
	 * @param gl
	 *            The preinitialized and prepared rendering context. The color of the context may be changed by this
	 *            method.
	 * @param x
	 *            X position of axis root.
	 * @param y
	 *            Y position of axis root.
	 * @param length
	 *            The length of the axis in pixel.
	 * @param name
	 *            The name of the axis.
	 * @param minVar
	 *            The minimum value that is drawn on the axis.
	 * @param maxVar
	 *            The maximum value that is drawn on the axis. It must be greater than the minimum value.
	 * @param horizontal
	 *            If {@code false} the axis is vertical, otherwise, the axis is horizontal.
	 */
	protected final void drawAxis(GL2 gl, float x, float y, float length, String name, float minVar, float maxVar,
			boolean horizontal) {

		if (minVar > maxVar) {
			throw new IllegalArgumentException("maxVar must be greater than minVar!");
		}

		// calc markers/labels
		float[] markersPos = ViewUtils.calcAxisMarkers(minVar, maxVar, length, PIXELS_PER_TICK);

		gl.glPushMatrix();

		gl.glTranslatef(x, y, 0.f);

		if (horizontal) {
			gl.glRotatef(-90.f, 0.f, 0.f, 1.f);
		}

		// config
		gl.glColor3f(0.f, 0.f, 0.f);
		gl.glLineWidth(1.f);
		this.trenderer.setColor(0.f, 0.f, 0.f, 1.f);

		// draw line
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(0.f, 0.f);
		gl.glVertex2f(0.f, length + 5.f);
		gl.glEnd();

		// draw arrow
		gl.glBegin(GL2.GL_TRIANGLES);
		gl.glVertex2f(0.f, length + 15.f);
		gl.glVertex2f(-10.f, length + 5.f);
		gl.glVertex2f(10.f, length + 5.f);
		gl.glEnd();

		// draw markers
		for (int i = 1; i < markersPos.length; i++) {
			float dY = (markersPos[i] - minVar) / (maxVar - minVar) * length;
			gl.glBegin(GL2.GL_LINES);
			gl.glVertex2f(0.f, dY);

			if (horizontal) {
				gl.glVertex2f(10.f, dY);
			} else {
				gl.glVertex2f(-10.f, dY);
			}

			gl.glEnd();
		}

		// draw labels
		gl.glPushMatrix();

		for (int i = 1; i < markersPos.length; i++) {
			String label = String.format("%." + Math.round(markersPos[0]) + "f", markersPos[i]);
			float dY = (markersPos[i] - minVar) / (maxVar - minVar) * length;
			Rectangle2D labelRect = this.trenderer.getBounds(label);
			int labelLength = (int) Math.ceil(labelRect.getWidth());
			int labelHeight = (int) Math.ceil(labelRect.getHeight());
			dY -= labelHeight / 2.f;
			int dX = 0;

			if (horizontal) {
				dX = 15;
			} else {
				dX = -15 - labelLength;
			}

			gl.glPushMatrix();
			gl.glTranslatef(dX, dY, 0.f);
			gl.glBegin(GL2.GL_POLYGON);
			gl.glColor4f(1.f, 1.f, 1.f, 0.7f);
			gl.glVertex2f(-2.f, -2.f);
			gl.glVertex2f(-2.f, labelHeight + 2.f);
			gl.glVertex2f(labelLength + 2.f, labelHeight + 2.f);
			gl.glVertex2f(labelLength + 2.f, -2.f);
			gl.glEnd();
			gl.glPopMatrix();

			gl.glPushMatrix();
			this.trenderer.beginRendering(this.getShapeWidth(), this.getShapeHeight());
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glTranslatef(x, y, 0.f);

			if (horizontal) {
				gl.glRotatef(-90.f, 0.f, 0.f, 1.f);
			}

			this.trenderer.draw(label, dX, (int) dY);
			this.trenderer.endRendering();
			gl.glPopMatrix();
		}

		gl.glPopMatrix();

		// draw name background
		float nameLength = (float) this.trenderer.getBounds(name).getWidth();
		float nameHeight = (float) this.trenderer.getBounds(name).getHeight();

		gl.glPushMatrix();
		gl.glRotatef(90.f, 0.f, 0.f, 1.f);
		gl.glTranslatef((int) Math.floor(length * 0.5f - nameLength * 0.5f), 0.f, 0.f);

		if (horizontal) {
			gl.glTranslatef(0.f, -12 - 10, 0.f);
		} else {
			gl.glTranslatef(0.f, 10, 0.f);
		}

		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor4f(1.f, 1.f, 1.f, 0.7f);
		gl.glVertex2f(-2.f, -2.f);
		gl.glVertex2f(-2.f, nameHeight + 2.f);
		gl.glVertex2f(nameLength + 2.f, nameHeight + 2.f);
		gl.glVertex2f(nameLength + 2.f, -2.f);
		gl.glEnd();
		gl.glPopMatrix();

		// draw name
		gl.glPushMatrix();
		this.trenderer.beginRendering(this.getShapeWidth(), this.getShapeHeight());
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		if (horizontal) {
			gl.glTranslatef(x, y - 12 - 10, 0.f);
		} else {
			gl.glTranslatef(x - 10, y, 0.f);
			gl.glRotatef(90.f, 0.f, 0.f, 1.f);
		}

		this.trenderer.draw(name, (int) Math.floor(length * 0.5f - nameLength * 0.5f), 0);
		this.trenderer.endRendering();
		gl.glPopMatrix();

		gl.glPopMatrix();
	}

	/**
	 * Draw an axis on the current location.
	 * 
	 * @param g2d
	 *            The preinitialized and prepared rendering context. The color of the context may be changed by this
	 *            method.
	 * @param x
	 *            X postion of axis root.
	 * @param y
	 *            Y postion of axis root.
	 * @param length
	 *            The length of the axis in pixel.
	 * @param name
	 *            The name of the axis.
	 * @param minVar
	 *            The minimum value that is drawn on the axis.
	 * @param maxVar
	 *            The maximum value that is drawn on the axis. It must be greater than the minimum value.
	 * @param horizontal
	 *            If {@code false} the axis is vertical, otherwise, the axis is horizontal.
	 */
	protected final void drawAxis(Graphics2D g2d, float x, float y, float length, String name, float minVar,
			float maxVar, boolean horizontal) {

		if (minVar > maxVar) {
			throw new IllegalArgumentException("maxVar must be greater than minVar!");
		}

		// calc markers/labels
		float[] markersPos = ViewUtils.calcAxisMarkers(minVar, maxVar, length, PIXELS_PER_TICK);
		FontMetrics fm = g2d.getFontMetrics();

		// init position
		g2d.translate(x, y);
		if (horizontal) {
			g2d.rotate(-Math.PI / 2.f);
		}

		// config
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1.f));

		// draw line
		g2d.drawLine(0, 0, 0, Math.round(length + 5.f));

		// draw arrow
		g2d.fillPolygon(new int[] { 0, -10, 10 }, new int[] { Math.round(length + 15.f), Math.round(length + 5),
				Math.round(length + 5) }, 3);

		// draw markers and labels
		for (int i = 1; i < markersPos.length; i++) {
			String label = String.format("%." + Math.round(markersPos[0]) + "f", markersPos[i]);
			Rectangle2D labelRect = fm.getStringBounds(label, g2d);

			int labelWidth = (int) Math.round(labelRect.getWidth());
			int labelHeight = (int) Math.round(labelRect.getHeight());
			int dY = Math.round((markersPos[i] - minVar) / (maxVar - minVar) * length);

			if (horizontal) {
				g2d.drawLine(0, dY, 10, dY);
			} else {
				g2d.drawLine(0, dY, -10, dY);
			}

			int dX;

			if (horizontal) {
				dX = 15;
			} else {
				dX = -15 - labelWidth;
			}

			g2d.scale(1, -1);
			g2d.setColor(new Color(1.f, 1.f, 1.f, 0.7f));
			g2d.fillRect((int) Math.round(labelRect.getX() - 2) + dX, (int) Math.round(labelRect.getY() + 4) - dY,
					(int) Math.round(labelRect.getWidth() + 4), (int) Math.round(labelRect.getHeight() + 4));
			g2d.setColor(Color.BLACK);
			g2d.drawString(label, dX, -dY + Math.round(labelHeight / 2.f));
			g2d.scale(1, -1);
		}

		// draw name
		if (horizontal) {
			g2d.translate(10, 0);
		} else {
			g2d.translate(-10, 0);
		}

		g2d.rotate(Math.PI / 2.f);
		g2d.scale(1, -1);
		Rectangle2D nameRect = fm.getStringBounds(name, g2d);
		int dX = (int) Math.round(length * 0.5f - nameRect.getWidth() * 0.5f);
		int dY = 0;

		if (horizontal) {
			dY = (int) Math.round(nameRect.getHeight());
		}

		g2d.setColor(new Color(1.f, 1.f, 1.f, 0.7f));
		g2d.fillRect((int) Math.round(nameRect.getX() - 2) + dX, (int) Math.round(nameRect.getY() - 2) + dY, (int) Math
				.round(nameRect.getWidth() + 4), (int) Math.round(nameRect.getHeight() + 4));
		g2d.setColor(Color.BLACK);
		g2d.drawString(name, dX, dY);
		g2d.scale(1, -1);
		g2d.rotate(-Math.PI / 2.f);

		if (horizontal) {
			g2d.translate(-10, 0);
		} else {
			g2d.translate(10, 0);
		}

		// reset position
		g2d.translate(-x, -y);

		if (horizontal) {
			g2d.rotate(Math.PI / 2.f);
		}
	}

	/**
	 * Loads and compiles a shader.
	 * 
	 * @param gl
	 *            render context.
	 * @param type
	 *            shader type, e.g. {@code GL2.GL_FRAGMENT_SHADER}.
	 * @param resouce
	 *            resource path to shader file.
	 * @return shader id, {@code 0} on error.
	 */
	protected final int setupShader(GL2 gl, int type, String resouce) {
		int id = gl.glCreateShader(type);

		try {
			String[] source = new String[1];

			InputStream is = this.getClass().getResourceAsStream(resouce);

			// could not load the shader, abort immediately
			if (is == null) {
				return 0;
			}

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
			StringBuilder stringBuilder = new StringBuilder();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}

			bufferedReader.close();
			source[0] = stringBuilder.toString();

			gl.glShaderSource(id, 1, source, (int[]) null, 0);
			gl.glCompileShader(id);

			IntBuffer intBuffer = IntBuffer.allocate(1);
			gl.glGetShaderiv(id, GL2.GL_COMPILE_STATUS, intBuffer);

			if (intBuffer.get(0) != 1) {
				// debug code, activate it, if you wan't to test shader code
				/*
				 * gl.glGetShaderiv(id, GL2.GL_INFO_LOG_LENGTH, intBuffer); int size = intBuffer.get(0);
				 * System.err.println("Shader compile error: "); if (size > 0) { ByteBuffer byteBuffer =
				 * ByteBuffer.allocate(size); gl.glGetShaderInfoLog(id, size, intBuffer, byteBuffer); for (byte b :
				 * byteBuffer.array()) { System.err.print((char) b); } } else { System.out.println("Unknown"); }
				 */
				id = 0;
			}
		} catch (IOException e) {
			id = 0;
		}

		return id;
	}

	@Override
	public final void setVisible(boolean visible) {
		if (visible != this.isVisible()) {
			if (visible) {
				super.setVisible(visible);
				EventController.getInstance().registerKeyTarget(this);
				EventController.getInstance().setAction(this.showExportAction, "eventExportGraphics");
				this.registerShortcuts();
				this.rerender();
			} else {
				this.unregisterShortcuts();
				EventController.getInstance().removeAction("eventExportGraphics");
				super.setVisible(visible);
			}
		}
	}

	@Override
	public final void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// optimize quality
		// gl.glShadeModel(GL2.GL_SMOOTH); // more performance

		// optimize speed
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_DITHER);
		gl.setSwapInterval(1);

		// enable alpha
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		// set background color
		gl.glClearColor(1, 1, 1, 1);

		// call implementation
		this.init(gl);
	}

	@Override
	public final void dispose(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// call implementation
		this.dispose(gl);
	}

	@Override
	public final synchronized void display(GLAutoDrawable drawable) {
		// some inits
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

		// reinit text renderer because of context binding
		this.trenderer = new TextRenderer(this.font);

		// save context and data
		int oldHeight = this.shapeHeight;
		int oldWidth = this.shapeWidth;
		int[] oldVBO = this.vbo;

		if (this.takeScreenshot) {
			this.reshape(drawable, this.shapeX, this.shapeY, this.screenshotWidth, this.screenshotHeight);
		}

		// update framerate
		if (SHOW_FPS) {
			this.updateFPS();
		}

		if (this.valid && taskReady) {
			this.processingTask = null;
			this.uploadData(gl);
		}

		if (this.valid && this.newData && (this.processingTask == null)) {
			taskReady = false;
			this.processingTask = this.processorExecutor.submit(new Processor());
		}

		if (this.valid && (this.processingTask == null)) {
			// store context
			gl.glPushMatrix();

			// fix coord system
			gl.glTranslatef(0.f, this.shapeHeight, 0.f);
			gl.glScalef(1.f, -1.f, 1.f);

			// call implementation
			this.draw(gl);

			// restore context
			gl.glPopMatrix();
			gl.glColor4f(0.f, 0.f, 0.f, 0.5f);
		}

		if (this.valid && (this.processingTask != null)) {
			this.drawMessage(gl, Settings.getInstance().getResourceBundle().getString("glPlotProcessing"));
		}

		if (!this.valid) {
			this.drawMessage(gl, Settings.getInstance().getResourceBundle().getString("glPlotInvalid"));
		}

		// display framerate
		if (SHOW_FPS) {
			this.trenderer.beginRendering(this.shapeWidth, this.shapeHeight);
			this.trenderer.setColor(0.f, 0.f, 0.f, 1.f);
			this.trenderer.draw(this.fps + " FPS", 0, 0);
			this.trenderer.endRendering();
		}

		// flush
		gl.glFlush();

		// take screenshot
		if (this.takeScreenshot) {
			try {
				Screenshot.writeToFile(this.screenshotFile, this.screenshotWidth, this.screenshotHeight);
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (GLException ex) {
				ex.printStackTrace();
			}

			// restore context and data
			this.reshape(drawable, this.shapeX, this.shapeY, oldWidth, oldHeight);
			this.vbo = oldVBO;
		}
	}

	@Override
	public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.shapeX = x;
		this.shapeY = y;
		this.shapeWidth = width;
		this.shapeHeight = height;

		GL2 gl = drawable.getGL().getGL2();

		// set 2d mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, width, height, 0, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// call implementation
		this.rescale(gl);
	}

	@Override
	public final void update(Observable o, Object arg) {
		if (o == this.subspaceController) {
			try {
				this.setFeatures(this.subspaceController.getActiveSubspace().getFeatures());
			} catch (DatabaseAccessException ex) {
				this.setInvalid();
			}
		}

		this.newData = true;

		this.processingTask = null;

		if (this.isVisible()) {
			this.rerender();
		}
	}

	/**
	 * Get a prepared and selected GLProfile.
	 * 
	 * @return ideal GLProfile.
	 */
	private static GLProfile getGLProfile() {
		return GLProfile.getDefault();
	}

	/**
	 * Prepares the GLCapabilities for a optimized accelerated rendering context.
	 * 
	 * @return the detected and optimized capabilities.
	 */
	private static GLCapabilities getGLCaps() {
		GLProfile glprofile = getGLProfile();
		GLCapabilities glcaps = new GLCapabilities(glprofile);
		glcaps.setDoubleBuffered(true);
		glcaps.setHardwareAccelerated(true);

		return glcaps;
	}

	/**
	 * Updates the FPS counter.
	 * 
	 * Should be called at be beginning of the rendering process of every frame.
	 */
	private void updateFPS() {
		long now = System.currentTimeMillis();

		if (now >= this.fpsPartBegin + FPS_PART_LENGTH) {
			this.fps = (int) ((this.fpsPartFrames * 1000) / (now - this.fpsPartBegin));
			this.fpsPartFrames = 1;
			this.fpsPartBegin = now;
		} else {
			this.fpsPartFrames++;
		}
	}

	/**
	 * Show export dialog.
	 */
	private class ShowExportAction extends AbstractAction {
		private static final long serialVersionUID = -5789657423404702178L;

		@Override
		public void actionPerformed(ActionEvent e) {
			screenshotWidth = shapeWidth;
			screenshotHeight = shapeHeight;

			exportWidthSpinner.setValue(Math.max(SCREENSHOT_SIZE_MIN, Math.min(screenshotWidth, SCREENSHOT_SIZE_MAX)));
			exportHeightSpinner
					.setValue(Math.max(SCREENSHOT_SIZE_MIN, Math.min(screenshotHeight, SCREENSHOT_SIZE_MAX)));

			exportButtonPanel.setVisible(false);
			exportSizePanel.setVisible(true);

			exportPanel.setMaximumSize(new Dimension((int) exportPanel.getMaximumSize().getWidth(), (int) exportPanel
					.getPreferredSize().getHeight()));
		}

	}

	/**
	 * Do export.
	 */
	private class DoExportAction extends AbstractAction {
		private static final long serialVersionUID = -4352592799039959131L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			fc.setDialogTitle(Settings.getInstance().getResourceBundle().getString("glPlotImgDialogTitle"));
			FileFilter ffi = new ImageFileFilter();
			FileFilter ffs = new SVGFileFilter();
			fc.addChoosableFileFilter(ffi);
			fc.addChoosableFileFilter(ffs);
			int ret = fc.showSaveDialog(EventController.getInstance().getRootFrame());

			if (ret == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				boolean exportSVG = false;
				boolean exportRaster = false;

				if (ffs.accept(f)) {
					exportSVG = true;
				} else if (ffi.accept(f)) {
					exportRaster = true;
				} else {
					// fallback to default file type
					if (fc.getFileFilter().equals(ffs)) {
						f = new File(f.getPath() + ".svg");
						exportSVG = true;
					} else {
						f = new File(f.getPath() + ".png");
						exportRaster = true;
					}
				}

				screenshotFile = f;

				if (exportSVG) {
					try {
						genSVG();
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

				if (exportRaster) {
					dump();
				}
			}

			closeExportAction.actionPerformed(e);
		}
	}

	/**
	 * Cancel export.
	 */
	private class CloseExportAction extends AbstractAction {
		private static final long serialVersionUID = -1077216507135650491L;

		@Override
		public void actionPerformed(ActionEvent e) {
			exportSizePanel.setVisible(false);
			exportButtonPanel.setVisible(true);

			exportPanel.setMaximumSize(new Dimension((int) exportPanel.getMaximumSize().getWidth(), (int) exportPanel
					.getPreferredSize().getHeight()));
		}
	}

	/**
	 * Filters ImageIO files.
	 */
	private class ImageFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);

			return ImageIO.getImageWritersBySuffix(ext).hasNext();
		}

		@Override
		public String getDescription() {
			String[] tmp = ImageIO.getWriterFileSuffixes();
			StringBuilder result = new StringBuilder();
			boolean first = true;

			for (int i = 0; i < tmp.length; i++) {
				if (first) {
					first = false;
				} else {
					result.append(", ");
				}
				result.append(tmp[i]);
			}

			return result.toString();
		}
	}

	/**
	 * Filters SVG files.
	 */
	private class SVGFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".svg");
		}

		@Override
		public String getDescription() {
			return "svg";
		}

	}

	/**
	 * Processes data for rendering.
	 */
	private class Processor implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			Boolean result = Boolean.TRUE;

			try {
				if (ihandler != null) {
					ihandler.pause();
				}

				try {
					processData();
					newData = false;

					if (ihandler != null) {
						ihandler.resume();
					}
				} catch (DatabaseAccessException ex) {
					ex.printStackTrace();
					result = Boolean.FALSE;
				} catch (InterruptedException ex) {
					result = Boolean.TRUE;
				}

			} catch (Exception e) {
				e.printStackTrace();
				result = Boolean.FALSE;
			}

			valid = result;
			taskReady = true;
			rerender();

			return result;
		}
	}
}
