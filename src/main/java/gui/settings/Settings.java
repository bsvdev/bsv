package gui.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The class {@code Settings} is globally reachable in the UI and provides the settings, done by the user.
 */
public final class Settings {
	/**
	 * List of supported and translated languages.
	 */
	private static final Locale[] SUPPORTED_LANGUAGES = { Locale.GERMAN, Locale.ENGLISH };

	/**
	 * Contains all Strings for the program depending on the chosen language.
	 */
	private ResourceBundle rb;

	/**
	 * Stores the active language.
	 */
	private Locale language;

	/**
	 * Stores the only instance of the Settings class.
	 */
	private static Settings st;

	/**
	 * Private Constructor, called only from getInstance().
	 */
	private Settings() {
		this.reset();
	}

	/**
	 * Returns the instance of this class.
	 *
	 * @return returns the instance stored in st.
	 */
	public static Settings getInstance() {
		if (st == null) {
			st = new Settings();
		}

		return st;
	}

	/**
	 * Load settings from file.
	 *
	 * @param filename
	 *            filename (and path) to settings file. If {@code null}, default file will be used.
	 * @throws IOException
	 *             Thrown if file can not be written. See exception details for more informations.
	 */
	public void load(String filename) throws IOException {
		String path = filename;

		if (path == null) {
			path = getDefaultFile();
		}

		Properties defaults = new Properties();
		defaults.setProperty("lang", "en");

		Properties props = new Properties(defaults);
		FileInputStream fi = new FileInputStream(path);
		props.load(fi);
		fi.close();

		this.setLanguage(new Locale(props.getProperty("lang")));
	}

	/**
	 * Stores settings to file.
	 *
	 * @param filename
	 *            filename (and path) to settings file. If {@code null}, default file will be used.
	 * @throws IOException
	 *             Thrown if file can not be written. See exception details for more informations.
	 */
	public void store(String filename) throws IOException {
		String path = filename;
		if (path == null) {
			path = getDefaultFile();
		}

		if ((new File(path)).exists()) {
			(new File(path)).delete();
		}

		Properties props = new Properties();
		props.setProperty("lang", this.language.getLanguage());
		FileOutputStream fo = new FileOutputStream(path);
		props.store(fo, "");
		fo.close();
	}

	/**
	 * Resets the properties to a default state.
	 */
	public void reset() {
		this.setLanguage(Locale.ENGLISH);
	}

	/**
	 * Access to the ResourceBundle of the Settings class.
	 *
	 * @return the ResourceBundle.
	 */
	public ResourceBundle getResourceBundle() {
		return this.rb;
	}

	/**
	 * Provides a list of the supported languages.
	 *
	 * @return An array containing these languages.
	 */
	public Locale[] getSupportedLanguages() {
		return SUPPORTED_LANGUAGES;
	}

	/**
	 * Returns the current language.
	 *
	 * @return A locale corresponding to that language.
	 */
	public Locale getLanguage() {
		return this.language;
	}

	/**
	 * Sets the current language according to the parameter.
	 *
	 * @param language
	 *            Specifies the new current language.
	 */
	public void setLanguage(Locale language) {
		if (!Arrays.asList(SUPPORTED_LANGUAGES).contains(language)) {
			throw new IllegalArgumentException("Language is not supported!");
		}

		this.language = language;
		this.rb = ResourceBundle.getBundle("Properties", this.language);
	}

	/**
	 * Get path to default settings file depending on system and user.
	 *
	 * @return default settings file
	 */
	public static String getDefaultFile() {
		// get folder depending on OS
		String os = System.getProperty("os.name").toUpperCase();
		String folder = System.getProperty("user.dir");
		if (os.contains("WIN")) {
			folder = System.getenv("APPDATA") + "/BSV";
		} else if (os.contains("MAC")) {
			folder = System.getProperty("user.home") + "/Library/Application Support/BSV";
		} else if (os.contains("LINUX") || os.contains("NIX")) {
			folder = System.getProperty("user.home") + "/.config/bsv";
		}

		// create folder
		(new File(folder)).mkdirs();

		return folder + "/.bsv_settings";
	}
}
