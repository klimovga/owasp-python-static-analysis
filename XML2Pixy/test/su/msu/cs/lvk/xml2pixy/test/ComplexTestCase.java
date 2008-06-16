package su.msu.cs.lvk.xml2pixy.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 16:55:58
 */
public class ComplexTestCase extends BaseTestCase {

    private static final String[] TEST_FILES = new String[]{
            "class1",
            "class2",
            "from1",
            "from2",
            "import1",
            "import2",
            "import3",
            "import4",
            "import5",
            "import6",
            "import7",
            "import8",
            "import9",
            "sys1",
            "sys2"
//            "types" too complex to fix
            
    };

    protected ComplexTestCase(String fileName) {
        super(fileName);
        this.xmlFileName = fileName + "/main.py";
        this.phpFileName = fileName + ".py";
    }

    protected void setUp() {
        this.path = "tests/func/complex/";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        for (String file : TEST_FILES) {
            suite.addTest(new ComplexTestCase(file));
        }
        return suite;
    }

}
