package importexport.exporting;

import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;

import java.io.BufferedWriter;
import java.io.IOException;

import controller.Feature;
import db.DatabaseAccessException;

/**
 * Injects the header of a .arff-file which should be build.
 */
public class ArffFileInfoInjector extends CSVFileInfoInjector {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void injectFileInfo(final BufferedWriter bw, final FileInfo fInfo, final Feature[] features)
			throws IOException, DatabaseAccessException {
		if (fInfo instanceof CSVFileInfo) {
			CSVFileInfo info = (CSVFileInfo) fInfo;

			bw.write(("@relation " + info.getName()));
			bw.newLine();
			bw.newLine();

			int normalFeatNr = 0;
			for (Feature f : features) {
				if (!f.isOutlier()) {
					++normalFeatNr;
				}
			}

			/* Starts with i = 1, because feeture[0] is
			 * effective outlierness of current object.
			 */
			for (int i = 1; i < normalFeatNr; ++i) {
				bw.append(("@attribute " + features[i]));
				bw.newLine();
			}

			bw.append("@attribute class {0, 1}");
			bw.newLine();
			bw.newLine();

			bw.append("@data");
			bw.newLine();

			bw.flush();
		}
	}
}
