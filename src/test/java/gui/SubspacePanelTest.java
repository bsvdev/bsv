package gui;

import static org.junit.Assert.assertEquals;
import gui.subspacePanel.FeatureSubspaceDialog;
import gui.subspacePanel.SubspaceChooseDialog;
import gui.subspacePanel.SubspacePanel;

import java.io.File;

import javax.swing.JDialog;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import controller.SubspaceController;
import db.Database;
import db.DatabaseAccessException;
import db.IncompatibleVersionException;
import db.InvalidDriverException;

/**
 * Test the classes {@link SubspacePanel}, {@link SubspaceChooseDialog} and {@link FeatureSubspaceDialog}
 */
public class SubspacePanelTest {

	/**
	 * Test the constructor for illegal argument exceptions
	 */
	@Test
	public void subspacePanelArgumentTest() {
		try {
			new SubspacePanel(null);
			Assert.fail("SubspaceController was null");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test the constructor for illegal argument exceptions
	 */
	@Ignore
	@Test
	public void subspaceChooseDialogArgumentTest() {
		String path = System.getProperty("java.io.tmpdir") + "/bsv_controller_tests";
		String dbFile = path + "/database-junit-group.bsv";
		// create working directory
		(new File(path)).mkdirs();

		// make sure the old file is deleted
		(new File(dbFile)).delete();

		// create a new Database
		Database database = null;

		try {
			database = new Database(dbFile);
		} catch (InvalidDriverException e) {
			Assert.fail(e.getMessage());
		} catch (IncompatibleVersionException e) {
			Assert.fail(e.getMessage());
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// test the constructor
		try {
			new SubspaceChooseDialog(null, new SubspaceController(database));
			Assert.fail("SubspaceController was null");
		} catch (IllegalArgumentException e) {
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}
		try {
			new SubspaceChooseDialog(new JDialog(), null);
			Assert.fail("SubspaceController was null");
		} catch (IllegalArgumentException e) {
		}

		// create valid instance
		SubspaceChooseDialog chooser = null;
		try {
			chooser = new SubspaceChooseDialog(new JDialog(), new SubspaceController(database));
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// test the method
		try {
			chooser.updateView(null);
			Assert.fail("Subspaces was null");
		} catch (IllegalArgumentException e) {
		}

		// clean up all files after testing
		// shutdown
		try {
			database.shutdown();
		} catch (DatabaseAccessException e) {
			Assert.fail(e.getMessage());
		}

		// clean up database
		if (database != null) {
			assertEquals(true, (new File(dbFile)).delete());
		}
	}

	/**
	 * Test the constructor for illegal argument exceptions
	 */
	@Ignore
	@Test
	public void featureSubspaceDialogArgumentTest() {
		try {
			new FeatureSubspaceDialog(null);
			Assert.fail("All subspaces was null");
		} catch (IllegalArgumentException e) {
		}
	}
}
