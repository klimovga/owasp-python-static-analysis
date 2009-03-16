package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;
import su.msu.cs.lvk.xml2pixy.ast.python.TryFinallyNode;

/**
 * @author gklimov
 * @created 15.03.2009 19:03:29
 */
public class TryFinallyNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) {
        TryFinallyNode tryFinally = (TryFinallyNode) node;

        for (PythonNode stmt : tryFinally.getBody().getNodes()) {
            addToNearestStmt(node, stmt);
        }

        for (PythonNode stmt : tryFinally.getFinal().getNodes()) {
            addToNearestStmt(node, stmt);
        }

        StmtNode parent = (StmtNode) node.getParent();
        parent.getNodes().remove(node);
    }
}
