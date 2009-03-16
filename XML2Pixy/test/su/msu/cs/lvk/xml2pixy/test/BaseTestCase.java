package su.msu.cs.lvk.xml2pixy.test;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpLexer;
import at.ac.tuwien.infosys.www.phpparser.PhpParser;
import junit.framework.TestCase;
import su.msu.cs.lvk.xml2pixy.Converter;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.io.FileReader;

/**
 * Created at: [23.10.2007] 16:09:05
 *
 * @author gklimov
 */
public abstract class BaseTestCase extends TestCase {

    protected String path;
    protected String xmlFileName;
    protected String phpFileName;
    protected ParseNode found;
    protected ParseNode expected;

    protected BaseTestCase(String fileName) {
        this.xmlFileName = fileName;
        this.phpFileName = fileName;
    }

    protected void setUp() {
        this.path = "tests/func/trivial/";
    }

    protected void runTest() throws Throwable {
//        String xmlFile = "xml/" + xmlFileName + ".xml";
        String pyFile = xmlFileName;
        String phpFile = "php/" + phpFileName + ".php";
        Converter.mainFile = xmlFileName;
        try {
            found = Utils.buildParseTree(this.path + pyFile, false);

            PhpLexer phpLexer = new PhpLexer(new FileReader(this.path + phpFile));
            phpLexer.setFileName(phpFile);
            PhpParser phpParser = new PhpParser(phpLexer);
            expected = (ParseNode) phpParser.parse().value;

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(this.xmlFileName + " failed: " + e.toString(), false);
        }

        String result = ProcessingUtils.compare(this.expected, this.found);
        if (result != null) {
            Utils.printTree(this.expected);
            Utils.printTree(this.found);
        }
        assertNull(result, this.xmlFileName + ": " + result);
    }

}
