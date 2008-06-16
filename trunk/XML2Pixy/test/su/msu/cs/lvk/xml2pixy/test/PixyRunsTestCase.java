package su.msu.cs.lvk.xml2pixy.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created at: [23.10.2007] 15:26:09
 *
 * @author gklimov
 */
public class PixyRunsTestCase extends BaseTestCase {

    private static final String[] TEST_FILES = {
            "getstarted.py"
    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        for (String file : TEST_FILES) {
            suite.addTest(new PixyRunsTestCase(file));
        }
        return suite;
    }

    protected PixyRunsTestCase(String fileName) {
        super(fileName);
    }

    protected void setUp() {
        this.path = "tests/func/pixyruns/";
    }

}
