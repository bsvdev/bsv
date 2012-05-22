package gui.main;

import gui.settings.Settings;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Info dialog that shows some informations about the program
 */
public class InfoDialog extends JDialog {

	private static final long serialVersionUID = -3322816913402856913L;

	/**
	 * Creates a new info dialog
	 *
	 * @param owner
	 *            the owner frame of the dialog (can be {@code null})
	 */
	public InfoDialog(Frame owner) {
		super(owner, true);

		// set some configs
		this.setTitle(Settings.getInstance().getResourceBundle().getString("infoDialogTitle"));
		this.setResizable(false);

		// build html like content
		StringBuilder content = new StringBuilder();
		content.append("<html>");
		content.append("<div style=\"margin:50px;\">");

		try {
			// get manifest
			File jFile = new File(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			JarFile jarFile = new JarFile(jFile);
			Manifest manifest = jarFile.getManifest();
			Attributes attributes = manifest.getMainAttributes();

			// title
			content.append("<h1>");
			content.append(attributes.getValue("Implementation-Title"));
			content.append("</h1>");

			content.append("<p>");

			// version
			content.append("Version: ");
			content.append(attributes.getValue("Implementation-Version"));
			content.append("<br>");

			// developer
			content.append("Developer: ");
			content.append(attributes.getValue("Implementation-Vendor"));
			content.append("<br>");

			// license
			content.append("License: ");
			content.append("<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache 2.0 license</a>");
			content.append("<br>");

			// website
			content.append("Website: ");
			content.append("<a href=\"https://github.com/bsvdev/bsv\">https://github.com/bsvdev/bsv</a>");

			content.append("</p>");

			// libs
			content.append("<h2>");
			content.append("Libraries");
			content.append("</h2>");

			// JUnit
			content.append(buildLibString("JUnit", "http://www.junit.org/", "Common Public License - v 1.0"));

			// GlueGen
			content.append(buildLibString("GlueGen", "http://jogamp.org/gluegen/www", "New BSD 2-clause license"));

			// JOGL
			content.append(buildLibString("JOGL", "http://jogamp.org/jogl/www", "New BSD 2-clause license"));

			// opencsv
			content.append(buildLibString("opencsv", "http://opencsv.sourceforge.net/", "Apache 2.0 license"));

			// SQLite
			content.append(buildLibString("Xerial SQLite JDBC Driver",
					"http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC", "Apache 2.0 license"));

			// Apache Commons
			content.append(buildLibString("Apache Commons", "http://commons.apache.org", "Apache 2.0 license"));

			// Apache Batik
			content.append(buildLibString("Apache Batik", "http://xmlgraphics.apache.org/batik/", "Apache 2.0 license"));

			// Iconic
			content.append(buildLibString("Iconic", "http://somerandomdude.com/work/iconic/", "CC BY-SA 3.0"));
		} catch (IOException ex) {
			content.append(":(");
		} catch (URISyntaxException ex) {
			content.append(":(");
		}

		// add content
		content.append("</div>");
		content.append("</html>");
		JEditorPane pane = new JEditorPane("text/html", content.toString());
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					URL url = hle.getURL();

					if (Desktop.isDesktopSupported()) {
						try {
							URI uri = new URI(url.toString());
							Desktop.getDesktop().browse(uri);
						} catch (URISyntaxException ex) {
							// ignore
						} catch (IOException ex) {
							// ignore
						}
					}
				}
			}
		});
		this.add(pane);
		this.pack();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			this.setLocationRelativeTo(null);
		}
		super.setVisible(visible);
	}

	/**
	 * Builds a html string for the specified library
	 *
	 * @param name
	 *            name of the library
	 * @param url
	 *            url of the library website
	 * @param license
	 *            license of the library
	 * @return the html string
	 */
	private static String buildLibString(String name, String url, String license) {
		StringBuilder result = new StringBuilder();

		result.append("<a href=\"");
		result.append(url);
		result.append("\">");
		result.append(name);
		result.append(" (");
		result.append(license);
		result.append(")</a><br>");

		return result.toString();
	}
}
