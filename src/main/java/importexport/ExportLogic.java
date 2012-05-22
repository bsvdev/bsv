package importexport;

import importexport.exporting.ArffFileInfoInjector;
import importexport.exporting.CSVBasedExporter;
import importexport.exporting.CSVFileInfoInjector;
import importexport.exporting.Exporter;
import importexport.util.InvalidFileException;
import importexport.util.UnsupportedFileExtensionException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import controller.DataHub;
import controller.ElementData;
import controller.SelectionController;
import controller.SubspaceController;
import db.DatabaseAccessException;

/**
 * ExportLogic administrates exporters and exporting files.
 */
public final class ExportLogic {

	/**
	 * Concrete instance for the ExportLogic.
	 */
	private static ExportLogic instance = new ExportLogic();

	/**
	 * The {@link DataHub} for accessing the needed {@link ElementData}.
	 */
	private DataHub datahub;

	/**
	 * The {@link SelectionController} which handles current selections.
	 */
	private SelectionController selController;

	/**
	 * The {@link SubspaceController} for accessing subspace related data.
	 */
	private SubspaceController subController;

	/**
	 * Maps file extensions to their respective exporter.
	 */
	private final HashMap<String, Exporter> exporter;

	/**
	 * Creates the Instance for the export logic.
	 */
	private ExportLogic() {
		exporter = new HashMap<String, Exporter>();
		datahub = null;
		selController = null;
		subController = null;
	}

	/**
	 * Adds an concrete exporter for datasheets to this ExportLogic.
	 * 
	 * @param fileExtension
	 *            Ending of files which should be exported with the transmitted importer.
	 * @param concreteExporter
	 *            The concrete exporter which should export files with the given ending.
	 */
	private void add(final String fileExtension, final Exporter concreteExporter) {
		this.exporter.put(fileExtension, concreteExporter);
	}

	/**
	 * Initialize or rather reinitialize the static ExportLogic instance.
	 * 
	 * @param datahub
	 *            the preinitialized DataHub
	 * @param selectionController
	 *            the preinitialized SelectionController
	 * @param subspaceController
	 *            the preinitialized SubspaceController
	 */
	public static void init(final DataHub datahub, final SelectionController selectionController,
			final SubspaceController subspaceController) {
		if (datahub == null || selectionController == null || subspaceController == null) {
			throw new IllegalArgumentException("Non-null controllers are needed for Export.");
		}

		instance.datahub = datahub;
		instance.selController = selectionController;
		instance.subController = subspaceController;

		instance.add("arff", new CSVBasedExporter(datahub, selectionController, subspaceController,
				new ArffFileInfoInjector()));
		instance.add("csv", new CSVBasedExporter(datahub, selectionController, subspaceController,
				new CSVFileInfoInjector()));
	}

	/**
	 * Returns the initialized instance of the ExpoortLogic.
	 * 
	 * For getting an instance of ExportLogic you have to initialize it at first with a DataHub, a SelectionController
	 * and a SubspaceController.
	 * 
	 * @return the static instance of class ImportLogic or null if instance isn't initialized.
	 */
	public static ExportLogic getInstance() {
		if (instance.datahub == null || instance.selController == null || instance.subController == null) {
			return null;
		}

		return instance;
	}

	/**
	 * Returns a String[] with all available format endings.
	 * 
	 * @return all available format endings as String[].
	 */
	public String[] getExportFormats() {
		return (exporter.keySet().toArray(new String[exporter.keySet().size()]));
	}

	/**
	 * Exports an selected data to transmitted file.
	 * 
	 * @param output
	 *            File which will containing the exported data.
	 * @param woOutlierness
	 *            should adapted ssd be produced.
	 * @throws IOException
	 *             thrown if IO-Operation failed.
	 * @throws InvalidFileException
	 *             threw if output file is in any case invalid.
	 * @throws DatabaseAccessException
	 *             threw if access to database failed.
	 */
	public void exportFile(final File output, final boolean woOutlierness) throws IOException, InvalidFileException,
			DatabaseAccessException {
		export(output, woOutlierness, initExport(output));
	}

	/**
	 * Extract file format ending of output and creates output file.
	 * 
	 * @param output
	 *            File which will containing the exported data.
	 * @return the file ending of the output file.
	 * @throws IOException
	 *             thrown if IO-Operation failed.
	 */
	private String initExport(final File output) throws IOException {
		int lastDot = output.getName().lastIndexOf('.');
		String extension = "";

		if (lastDot > -1) {
			extension = output.getName().substring(lastDot + 1, output.getName().length());
		}

		output.createNewFile();

		return extension;
	}

	/**
	 * Actual export action caller.
	 * 
	 * @param output
	 *            File which will containing the exported data.
	 * @param woOutlierness
	 *            should adapted ssd produced.
	 * @param format
	 *            Format of the new created file.
	 * @throws IOException
	 *             thrown if IO-Operation failed.
	 * @throws InvalidFileException
	 *             threw if output file is in any case invalid.
	 * @throws DatabaseAccessException
	 *             threw if access to database failed.
	 */
	private void export(final File output, final boolean woOutlierness, final String format) throws IOException,
			InvalidFileException, DatabaseAccessException {

		Exporter actExporter = exporter.get(format);

		if (actExporter != null) {
			actExporter.exportFile(output, woOutlierness);
		} else {
			throw new UnsupportedFileExtensionException();
		}
	}
}
