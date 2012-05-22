package gui.views.plots;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * This interfaces describes classes that can handle user interaction with a {@link GLPlot}.
 */
public interface InteractionHandler extends MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * Pause user interaction e.g. when a plot is processing data.
	 */
	void pause();

	/**
	 * Resume user interaction.
	 */
	void resume();
}
