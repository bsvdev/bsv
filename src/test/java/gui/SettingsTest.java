package gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gui.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

public class SettingsTest {
	@Test
	public void testInstance() {
		Settings instance1 = Settings.getInstance();
		Settings instance2 = Settings.getInstance();

		assertTrue(null != instance1);
		assertEquals(instance1, instance2);
	}

	@Test
	public void testLanuage() {
		String key = "histplot";

		// test supported Langues
		assertTrue(Settings.getInstance().getSupportedLanguages() != null);
		assertTrue(Settings.getInstance().getSupportedLanguages().length > 0);

		// test standard language (English)
		assertEquals(Locale.ENGLISH, Settings.getInstance().getLanguage());
		assertEquals("Histogram", Settings.getInstance().getResourceBundle().getString(key));

		// test other language (German)
		Settings.getInstance().setLanguage(Locale.GERMAN);
		assertEquals(Locale.GERMAN, Settings.getInstance().getLanguage());
		assertEquals("Histogramm", Settings.getInstance().getResourceBundle().getString(key));

        // test wrong language
        try {
            Settings.getInstance().setLanguage(Locale.TRADITIONAL_CHINESE);
            fail("Set unsupported language.");
        } catch (IllegalArgumentException e) {
            // ok
        }
	}

    @Test
    public void testSaveResetLoad() {
        Settings.getInstance().reset();
        Locale[] langs = Settings.getInstance().getSupportedLanguages();
        String testDir = System.getProperty("java.io.tmpdir") + "/bsv_test";
        String settingsFile = testDir + "/settings.ini";
        (new File(testDir)).mkdirs();

        // set new language
        Locale current = Settings.getInstance().getLanguage();
        Locale next = langs[0];
        if (next == current) {
            next = langs[1];
        }
        Settings.getInstance().setLanguage(next);

		// store changes
        try {
            Settings.getInstance().store(settingsFile);
        }
        catch (IOException ex) {
            fail("Got IO exception: " + ex.getMessage());
        }

        // reset
        Settings.getInstance().reset();
        assertEquals("Reset does not affect language.", current, Settings.getInstance().getLanguage());

        // load
        try {
            Settings.getInstance().load(settingsFile);
        }
        catch (IOException ex) {
            fail("Got IO exception: " + ex.getMessage());
        }
        assertEquals("Setting does not work for language.", next, Settings.getInstance().getLanguage());
    }

	@Test
	public void testDefaultSettingsFile() {
		//store old settings
		File defFile = new File(Settings.getDefaultFile());
		File renFile = new File(defFile.getAbsolutePath() + "_old");
		if (defFile.exists()) {
			defFile.renameTo(renFile);
		}
		
		//test
		String defaultSettingsPath = Settings.getDefaultFile();

		Settings.getInstance().reset();
        Locale[] langs = Settings.getInstance().getSupportedLanguages();

        // set new language
        Locale current = Settings.getInstance().getLanguage();
        Locale next = langs[0];
        if (next == current) {
            next = langs[1];
        }
        Settings.getInstance().setLanguage(next);
		try {
			Settings.getInstance().store(null);
		} catch (IOException ex) {
            fail("Got IO exception: " + ex.getMessage());
		}
		assertTrue((new File(defaultSettingsPath)).exists());

		try {
			Settings.getInstance().load(defaultSettingsPath);
		} catch (IOException ex) {
            fail("Got IO exception: " + ex.getMessage());
		}
		assertEquals("Setting does not work for language.", next, Settings.getInstance().getLanguage());
		
		//restore old settings
		if (defFile.exists()) {
			defFile.delete();
		}
		renFile.renameTo(defFile);
	}
}
