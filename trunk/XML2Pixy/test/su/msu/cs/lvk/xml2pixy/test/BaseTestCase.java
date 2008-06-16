package su.msu.cs.lvk.xml2pixy.test;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpLexer;
import at.ac.tuwien.infosys.www.phpparser.PhpParser;
import junit.framework.TestCase;
import su.msu.cs.lvk.xml2pixy.Converter;
import su.msu.cs.lvk.xml2pixy.Utils;

import java.io.FileReader;
import java.util.List;

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

    protected String compare(ParseNode expected, ParseNode found) {
        if (expected.isToken() && found.isToken()) {
            if (!expected.getLexeme().equals(found.getLexeme())) {
                return error("lexeme", expected.getLexeme(), found.getLexeme(), expected.getLineno());
            }
            if (expected.getSymbol() != found.getSymbol()) {
                return error("token", expected.getName(), found.getName(), expected.getLineno());
            }
            return null;
        } else if (!expected.isToken() && !found.isToken()) {
            if (expected.getSymbol() != found.getSymbol()) {
                return error("non-terminal", expected.getName(), found.getName(), Utils.getLinenoLeft(expected));
            }
            List eChildren = expected.getChildren(), fChildren = found.getChildren();
            if (eChildren.size() != fChildren.size()) {
                return error("child number in " + expected.getName(),
                        expected.getChildren().size(),
                        found.getChildren().size(),
                        Utils.getLinenoLeft(expected));
            }
            for (int i = 0; i < eChildren.size(); i++) {
                ParseNode eChild = (ParseNode) eChildren.get(i), fChild = (ParseNode) fChildren.get(i);
                String res = compare(eChild, fChild);
                if (res != null) return res;
            }
            return null;
        } else {
            return error("", expected.isToken() ? "token" : "non-terminal",
                    found.isToken() ? "token" : "non-terminal",
                    Utils.getLinenoLeft(expected));
        }
    }

    protected String error(String diff, Object expected, Object found) {
        return this.xmlFileName + ": ERROR : Different " + diff +
                " (expected: " + expected + ", found: " + found + ")";
    }

    protected String error(String diff, Object expected, Object found, int lineno) {
        return this.xmlFileName + ": ERROR : Different " + diff +
                " (expected: " + expected + ", found: " + found + ", lineno: " + lineno + ")";
    }

    protected void runTest() throws Throwable {
        String xmlFile = "xml/" + xmlFileName + ".xml";
        String phpFile = "php/" + phpFileName + ".php";
        Converter.mainFile = xmlFileName;
        try {
            found = Utils.buildParseTree(this.path + xmlFile, false);

            PhpLexer phpLexer = new PhpLexer(new FileReader(this.path + phpFile));
            phpLexer.setFileName(phpFile);
            PhpParser phpParser = new PhpParser(phpLexer);
            expected = (ParseNode) phpParser.parse().value;

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(this.xmlFileName + " failed: " + e.toString(), false);
        }

        String result = compare(this.expected, this.found);
        if (result != null) {
            Utils.printTree(this.expected);
            Utils.printTree(this.found);
        }
        assertNull(result, result);
    }

}
