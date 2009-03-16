package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;

/**
 * @author gklimov
 * @created 15.03.2009 19:29:18
 */
public class RaiseNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        RaiseNode raise = (RaiseNode) node;

        addToNearestStmt(node, new AssignNode(new AssNameNode(RaiseNode.EXCEPTION_INFO_VAR + "1"), raise.getExpr1()));
        if (raise.getExpr2() != null) {
            addToNearestStmt(node, new AssignNode(new AssNameNode(RaiseNode.EXCEPTION_INFO_VAR + "2"), raise.getExpr2()));
        }
        if (raise.getExpr3() != null) {
            addToNearestStmt(node, new AssignNode(new AssNameNode(RaiseNode.EXCEPTION_INFO_VAR + "3"), raise.getExpr3()));
        }

        ((StmtNode) raise.getParent()).getNodes().remove(node);
    }

}
