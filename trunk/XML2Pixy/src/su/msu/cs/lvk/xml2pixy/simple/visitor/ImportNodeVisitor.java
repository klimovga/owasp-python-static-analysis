package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;

/**
 * User: klimov
 * Date: 14.01.2009
 */
public class ImportNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        StmtNode stmt = (StmtNode) node.getParent();
        stmt.getNodes().remove(node);
    }

}
