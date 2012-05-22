package importexport.exporting;

import importexport.util.FileInfo;
import importexport.util.InvalidFileException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.SelectionController;
import controller.Subspace;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * Exporter provided an interface for exporting data into an arbitrary file format.
 */
public abstract class Exporter {
	/**
	 * Says that file-write-stream gets flushed every 20th element
	 */
	protected static int FLASH_AT = 20;

	/**
	 * {@link DataHub} accessing needed {@link ElementData}
	 */
	private final DataHub datahub;

	/**
	 * {@link SelecitonController} which contains current selection.
	 */
	private final SelectionController selectionController;

	/**
	 * {@link SubspaceController} for getting subspaces corresponded data.
	 */
	private final SubspaceController subspaceController;

	/**
	 * {@link FileInfoInjector} which adds {@link FileInfo}
	 */
	private final FileInfoInjector injector;

	/**
	 * Creates an concrete instance of an Exporter.
	 * 
	 * @param dataHub
	 *            the preinitialized DataHub
	 * @param selectionController
	 *            the preinitialized SelectionController
	 * @param subspaceController
	 *            the preinitialized SubspaceController
	 * @param concreteInjector
	 *            injector which injects {@link FileInfo} in newly created File.
	 */
	public Exporter(final DataHub dataHub, final SelectionController selectionController,
			final SubspaceController subspaceController, final FileInfoInjector concreteInjector) {

		if (dataHub == null || selectionController == null || subspaceController == null || concreteInjector == null) {
			throw new IllegalArgumentException();
		}

		this.datahub = dataHub;
		this.selectionController = selectionController;
		this.subspaceController = subspaceController;
		this.injector = concreteInjector;
	}

	/**
	 * Exports data of the current selection into an arbitrary supported file format.
	 * 
	 * @param f
	 *            File which will containing the exported data.
	 * @param woOutlierness
	 * 
	 * @throws IOException
	 *             thrown if IO-Operation failed.
	 * @throws InvalidFileException
	 *             threw if output file is in any case invalid.
	 * @throws DatabaseAccessException
	 *             threw if access to database failed.
	 */
	public abstract void exportFile(final File f, final boolean woOutlierness) throws IOException,
			InvalidFileException, DatabaseAccessException;

	/**
	 * Creates an adapted SSD-File by demand.
	 * 
	 * @param f
	 *            new SSD-File reference.
	 * @throws IOException
	 *             thrown if IO-Operation failed.
	 * @throws DatabaseAccessException
	 *             threw if access to database failed.
	 */
	protected final void exportSSD(final File f) throws IOException, DatabaseAccessException {
		if (f == null) {
			throw new IllegalArgumentException();
		}
		if (!f.exists()) {
			f.createNewFile();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		Subspace[] subspaces = this.subspaceController.getSubspaces();
		Feature[] curFeatures = subspaces[0].getFeatures();
		int[] curSelection = this.selectionController.getSelection();
		ElementData[] data = this.datahub.getData();
		Integer[] featIDs = null;

		int beginOutliernesses = 0;
		for (Feature feat : curFeatures) {
			if (!feat.isOutlier()) {
				beginOutliernesses++;
			}
		}

		int loopTally = 0;
		for (int i = 1; i < subspaces.length; ++i) {
			Feature tmp = null;
			Integer[] featsOfSS = null;
			int noVirtFeats = 0;

			curFeatures = subspaces[i].getFeatures();
			featIDs = new Integer[curFeatures.length];

			for (int j = 0; j < featIDs.length; ++j) {
				tmp = curFeatures[j];

				if (tmp != null && !tmp.isOutlier() && !tmp.isVirtual()) {
					featIDs[j] = tmp.getId();
				} else {
					noVirtFeats++;
				}
			}

			//festsOfSS => current set of features in corresponding subspace.
			featsOfSS = new Integer[featIDs.length - noVirtFeats];
			int x = 0;

			for (int j = 0; j < featIDs.length && x < featsOfSS.length; ++j, ++x) {
				if (featIDs[j] == null) {
					--x;
					continue;
				}

				featsOfSS[x] = featIDs[j] - 1;
			}

			bw.append("subspace " + (subspaces[i].getId() - 1) + " dimension = " + featsOfSS.length + " "
					+ Arrays.deepToString(featsOfSS));
			bw.newLine();

			//you need to flush the buffer for successful export.
			if (++loopTally == FLASH_AT) {
				bw.flush();
				loopTally = 0;
			}
		}

		bw.append("@data");
		bw.newLine();
		bw.flush();

		curFeatures = subspaces[0].getFeatures();

		for (int k = 0; k < subspaces.length - 1; ++k) {
			loopTally = 0;
			int newId = 0;

			if (selectionController.isSomethingSelected()) {
				for (int i : curSelection) {
					bw.append(newId++ + ";" + k + ";" + data[i - 1].getValue(curFeatures[beginOutliernesses + k]));
					bw.newLine();

					if (++loopTally == 20) {
						loopTally = 0;
						bw.flush();
					}
				}
			} else {
				for (ElementData d : data) {
					bw.append(newId++ + ";" + k + ";" + d.getValue(curFeatures[beginOutliernesses + k]));
					bw.newLine();

					if (++loopTally == FLASH_AT) {
						loopTally = 0;
						bw.flush();
					}
				}
			}

			bw.flush();
		}

		bw.close();
	}

	/**
	 * Returns the used {@link Datahub}.
	 * 
	 * @return currently used DataHub.
	 */
	protected final DataHub getDatahub() {
		return datahub;
	}

	/**
	 * Returns the used {@link FileInfoInjector}.
	 * 
	 * @return currently used FileInfoInjector.
	 */
	protected final FileInfoInjector getInjector() {
		return injector;
	}

	/**
	 * Returns the used {@link SelectionController}.
	 * 
	 * @return currently used SelectionController.
	 */
	protected final SelectionController getSelectionController() {
		return selectionController;
	}

	/**
	 * Returns the used {@link SubspaceController}.
	 * 
	 * @return currently used SubspaceController.
	 */
	protected final SubspaceController getSubspaceController() {
		return subspaceController;
	}
}
