package importexport;

import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;
import importexport.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;

import org.junit.Test;

public class UtilityAndOtherTest {

	@Test
	public void fileInfoW0Feats() {
		FileInfo info = new CSVFileInfo("Test", new String[0], 0, ';');
		Assert.assertTrue(info.noOfFeatures() == 0);
	}

	@Test
	public void fileInfoNulledFeaturees() {
		FileInfo info = new CSVFileInfo("Test", null, 0, ',');
		Assert.assertTrue(info.noOfFeatures() == 0);
	}

	@Test
	public void testDelimiterComma() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/comma_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == ',');

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testDelimiterColon() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/colon_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == ':');

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testDelimiterSemiColon() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/semicolon_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == ';');

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testDelimiterSpace() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/space_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == ' ');

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testDelimiterTab() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/tab_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == '\t');

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testNoDelimiter() {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					new File(
							"src/test/resources/importFiles/delimiterTestsOnly/non_seperated.csv"
							)));

			char delimiter = Utility.filterDelimiterFromString(fr.readLine());
			Assert.assertTrue(delimiter == 0);

		} catch (Throwable e) {
			Assert.fail("Unexptected Throwable " + e.getClass());
		}
	}

	@Test
	public void testIsFloat_CorrectFloat1() {
		String estimatedFloat = "4.95";
		Assert.assertTrue(Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_CorrectFloat2() {
		String estimatedFloat = ".95";
		Assert.assertTrue(Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_CorrectFloat3() {
		String estimatedFloat = "-0.03838383";
		Assert.assertTrue(Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_CorrectFloat4() {
		String estimatedFloat = "-.000000334";
		Assert.assertTrue(Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_CorrectFloat5() {
		String estimatedFloat = "42";
		Assert.assertTrue(Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_WrongFormattedFloat1() {
		String estimatedFloat = "4,95";
		Assert.assertTrue(!Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_WrongFormattedFloat2() {
		String estimatedFloat = "4S.95";
		Assert.assertTrue(!Utility.isFloat(estimatedFloat));
	}

	@Test
	public void testIsFloat_StringTransmitted() {
		String estimatedFloat = "Hallo";
		Assert.assertTrue(!Utility.isFloat(estimatedFloat));
	}

}
