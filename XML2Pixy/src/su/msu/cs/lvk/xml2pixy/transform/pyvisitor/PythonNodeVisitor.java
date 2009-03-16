package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class PythonNodeVisitor extends ASTVisitor {

    private static final Logger logger = Logger.getLogger(PythonNodeVisitor.class);


    public void visit(PythonNode node) throws VisitorException {
        logger.trace(node.getNodeName());
    }

    public ParseNodeHelper getHelper() {
        return helper;
    }

    public void setHelper(ParseNodeHelper helper) {
        this.helper = helper;
    }

}
