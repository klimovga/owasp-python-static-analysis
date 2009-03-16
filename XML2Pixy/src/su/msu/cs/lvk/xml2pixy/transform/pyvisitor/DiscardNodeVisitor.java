package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import su.msu.cs.lvk.xml2pixy.ast.python.DiscardNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 19.01.2009
 */
public class DiscardNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        DiscardNode discard = (DiscardNode) node;
        discard.setPhpNode(discard.getExpr().getPhpNode());
    }
}
