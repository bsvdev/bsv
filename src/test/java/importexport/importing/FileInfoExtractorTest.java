package importexport.importing;

import importexport.util.CSVFileInfo;
import importexport.util.FileInfo;
import importexport.util.InvalidFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the funcationality of the HeaderFactories.
 */
public class FileInfoExtractorTest {

    private static final String RESPATH = "src/test/resources/";

    @Test
    public void arffTest1() {
        String expectedRel = "HeaderOnlyArffTestFile1";
        String[] expectedAttr = {"var_0", "var_1", "var_2", "var_3",
            "var_4", "class"};
        int expectedFLODS = 11;

        File f = new File(
                RESPATH + "/importFiles/HeaderTestsOnly/Test1.arff");
        FileInfoExtractor hFac = new ArffFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(f);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }

        boolean cond = h != null
                && (((CSVFileInfo) h).getFirstLineOfDataSegment() > 0)
                && expectedRel.equals(h.getName())
                && expectedFLODS == ((CSVFileInfo) h).getFirstLineOfDataSegment()
                && Arrays.deepEquals(expectedAttr, ((CSVFileInfo) h).getFeatures());
        Assert.assertTrue(cond);
    }

    @Test
    public void nullArffFileTest() {
        File f = null;
        FileInfoExtractor hFac = new ArffFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(f);
        } catch (FileNotFoundException e) {
            Assert.assertTrue(f == null);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException!");
        } catch (InvalidFileException e) {
            Assert.fail("Unexpected IOException!");
        }
    }

    @Test
    public void emptyArffTest() {
        File f = new File(RESPATH + "importFiles/emptyArff.arff");
        FileInfoExtractor hFac = new ArffFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(f);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }

        Assert.assertTrue(((CSVFileInfo) h).getName() == null && ((CSVFileInfo) h).getFirstLineOfDataSegment() < 0);
    }

    @Test
    public void csvTest1() {
        String expectedRel = "Test1.csv";
        String[] expectedAttr = {"var_0", "var_1", "var_2", "var_3",
            "var_4", "class"};
        int expectedFLODS = 2;

        File f = new File(
                RESPATH + "/importFiles/HeaderTestsOnly/Test1.csv");
        FileInfoExtractor hFac = new CSVFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(f);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }

        boolean cond = h != null && expectedRel.equals(h.getName())
                && expectedFLODS == ((CSVFileInfo) h).getFirstLineOfDataSegment()
                && Arrays.deepEquals(expectedAttr, ((CSVFileInfo) h).getFeatures());
        Assert.assertTrue(cond);
    }

    @Test
    public void nullCSVFileTest() {
        File f = null;
        FileInfoExtractor hFac = new CSVFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(f);
        } catch (FileNotFoundException e) {
            Assert.assertTrue(f == null);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException!");
        } catch (InvalidFileException e) {
            Assert.fail("Unexpected IOException!");
        }
    }

    @Test
    public void notExistingCSVFileTest() {
        File f = null;
        FileInfoExtractor hFac = new CSVFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(new File("/no.csv"));
        } catch (FileNotFoundException e) {
			Assert.assertTrue(true);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException!");
        } catch (InvalidFileException e) {
            Assert.fail("Unexpected IOException!");
        }
    }

    @Test
    public void notExistingArffFileTest() {
        File f = null;
        FileInfoExtractor hFac = new ArffFileInfoExtractor();
        FileInfo h = null;
        try {
            h = hFac.extractFileInfo(new File("/no.arff"));
        } catch (FileNotFoundException e) {
			Assert.assertTrue(true);
        }  catch (IOException e) {
            Assert.fail("Unexpected IOException!");
        } catch (InvalidFileException e) {
            Assert.fail("Unexpected IOException!");
        }
    }

    @Test
    public void emptyCSVTest() {
        File f = new File(RESPATH + "importFiles/emptyCSV.csv");
        FileInfoExtractor hFac = new CSVFileInfoExtractor();
        try {
            hFac.extractFileInfo(f);
        } catch (InvalidFileException e) {
        	Assert.assertTrue(true);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
    }

    @Test
    public void getDelimiterFromNullFile() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        try {
            csv.getDelimiter(null);
        } catch (FileNotFoundException e) {
            Assert.assertTrue(true);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
    }

    @Test
    public void getDelimiterCSVTest1() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/comma_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ',');
    }

    @Test
    public void getDelimiterCSVTest2() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/semicolon_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ';');
    }

    @Test
    public void getDelimiterCSVTest3() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/colon_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ':');
    }

    @Test
    public void getDelimiterCSVTest4() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/space_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ' ');
    }

    @Test
    public void getDelimiterCSVTest5() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/tab_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == '\t');
    }

    @Test
    public void getDelimiterCSVTest6() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/non_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == 0);
    }

    @Test
    public void getDelimiterCSVTest7() {
        CSVFileInfoExtractor csv = new CSVFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(null);
        } catch (FileNotFoundException e) {
            Assert.assertTrue(true);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == 0);
    }

    @Test
    public void getDelimiterARFFTest1() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/comma_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ',');
    }

    @Test
    public void getDelimiterARFFTest2() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/semicolon_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ';');
    }

    @Test
    public void getDelimiterARFFTest3() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/colon_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ':');
    }

    @Test
    public void getDelimiterARFFTest4() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/space_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == ' ');
    }

    @Test
    public void getDelimiterARFFTest5() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/tab_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == '\t');
    }

    @Test
    public void getDelimiterARFFTest6() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(new File(RESPATH + "importFiles/delimiterTestsOnly/non_seperated.csv"));
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == 0);
    }

    @Test
    public void getDelimiterARFFTest7() {
        ArffFileInfoExtractor csv = new ArffFileInfoExtractor();
        char del = 0;
        try {
            del = csv.getDelimiter(null);
        } catch (FileNotFoundException e) {
            Assert.assertTrue(true);
        } catch (Throwable e) {
            Assert.fail("Unexpected Throwable");
        }
        Assert.assertTrue(del == 0);
    }
}
