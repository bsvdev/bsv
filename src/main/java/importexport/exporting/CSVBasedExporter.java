package importexport.exporting;

import importexport.util.CSVFileInfo;
import importexport.util.InvalidFileException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.BufferOverflowException;

import controller.DataHub;
import controller.ElementData;
import controller.Feature;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * {@inheritDoc}
 */
public class CSVBasedExporter extends Exporter {
	/**
	 * Creates a new Exporter for any csv-based formatted file
	 * (e.g. ".csv" or ".arff").
	 * 
	 * @param datahub
	 *            used to get the data which should be exported.
	 * @param selectionController
	 *            used to extract the current selection.
	 * @param subspaceController
	 *            used to get all necessary information about the subspaces.
	 * @param concreteInjector
	 *            injector which is used to create the file headers.
	 */
	public CSVBasedExporter(final DataHub datahub, final SelectionController selectionController,
			final SubspaceController subspaceController, final FileInfoInjector concreteInjector) {
		super(datahub, selectionController, subspaceController, concreteInjector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportFile(final File output, final boolean wOutlierness) throws InvalidFileException, IOException,
			DatabaseAccessException {

		if (output == null) {
			throw new InvalidFileException();
		}

		SelectionController selCon = this.getSelectionController();
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		CSVFileInfo fInfo = createFileInfo(output);
		Feature[] feats = this.getSubspaceController().getSubspaces()[0].getFeatures();

		this.getInjector().injectFileInfo(bw, fInfo, feats);

		ElementData[] data = this.getDatahub().getData();
		int bufferSize = 0;
		boolean flushStream = false;
		if (selCon.isSomethingSelected()) {
			int[] selection = selCon.getSelection();
			for (int selectedItem : selection) {
				if (++bufferSize == FLASH_AT) {
					bufferSize = 0;
					flushStream = true;
				}
				writeLine(buildLine(data[selectedItem - 1]), bw, flushStream);
			}
		} else {
			for (ElementData d : data) {
				if (++bufferSize == FLASH_AT) {
					bufferSize = 0;
					flushStream = true;
				}
				writeLine(buildLine(d), bw, flushStream);
			}
		}

		bw.close();

		if (wOutlierness) {
			/* get from UI always a file with a valid file extendsion like ".arff",
			 * so e.g. ".arff" will be replaced by ".ssd".
			 */
			int lastDotOfOutput = output.getAbsolutePath().lastIndexOf('.');
			StringBuilder ssdFileName = new StringBuilder(output.getAbsolutePath());
			ssdFileName.delete(lastDotOfOutput, ssdFileName.length());
			ssdFileName.append(".ssd");
			this.exportSSD(new File(ssdFileName.toString()));
		}
	}

	
	/**
	 * Builds a line which should be written into the csv-based file,
	 * based on the informations of an object of class {@link ElementData}.
	 * @param d
	 * 			ElementData object which should be exported
	 * @return 
	 * 			The built line.
	 * @throws DatabaseAccessException
	 * 			Threw if access to current database is failed.
	 */
	private StringBuilder buildLine(final ElementData d) throws DatabaseAccessException {
		StringBuilder sb = new StringBuilder();
		Feature[] feats = this.getSubspaceController().getActiveSubspace().getFeatures();
		Float tmp;

		for (Feature f : feats) {
			if (!f.isOutlier() && !f.isVirtual()) {
				tmp = d.getValue(f);

				if (tmp.isNaN()) {
					sb.append("?,");
				} else {
					sb.append((d.getValue(f) + ","));
				}
			}
		}

		if (getInjector() instanceof ArffFileInfoInjector) {
			sb.append("0");
		} else {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb;
	}

	/**
	 * Writes previous built {@link CharSequence} into a file.
	 * 
	 * @param cs
	 * 			{@link CharSequence} which should be written to file.
	 * @param bw
	 * 			{@link BufferOverflowException} which is responsible for
	 * 			write action.
	 * @param flush
	 * 			true if writer stream should be flushed after write action.
	 * @throws IOException
	 * 			threw if an arbitrary io-operation failed.
	 */
	private void writeLine(final CharSequence cs, final BufferedWriter bw,
			boolean flush) throws IOException {
		bw.write(cs.toString());
		bw.newLine();
		if (flush) {
			bw.flush();
		}
	}

	/**
	 * Creates {@link CSVFileInfo} which for in future exported file.
	 * @param f
	 * 			File for what the {@link CSVFileInfo} is built for.
	 * @return 
	 * 			The constructed {@link CSVFileInfo} object.
	 * @throws DatabaseAccessException
	 * 			Threw if access to current database is failed.
	 */
	private CSVFileInfo createFileInfo(final File f) throws DatabaseAccessException {
		String relation = f.getName();
		Feature[] features = this.getSubspaceController().getSubspaces()[0].getFeatures();
		String[] featureNames = new String[features.length];

		for (int i = 1; i < features.length; ++i) {
			featureNames[i] = features[i].getName();
		}
		//fLODS = first line of data segment.
		int fLODS = -1;

		FileInfoInjector injector = this.getInjector();
		
		if (injector instanceof ArffFileInfoInjector) {
			fLODS = 4 + features.length;
		} else if (injector instanceof CSVFileInfoInjector) {
			fLODS = 2;
		}

		return new CSVFileInfo(relation, featureNames, fLODS, ',');
	}
}
