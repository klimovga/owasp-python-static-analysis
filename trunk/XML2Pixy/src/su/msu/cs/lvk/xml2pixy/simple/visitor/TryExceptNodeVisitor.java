package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.IfNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.TryExceptNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gklimov
 * @created 15.03.2009 19:17:09
 */
public class TryExceptNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) {
        TryExceptNode tryExcept = (TryExceptNode) node;

        for (PythonNode stmt : tryExcept.getBody().getNodes()) {
            addToNearestStmt(node, stmt);
        }

        List<PythonNode> randoms = new ArrayList<PythonNode>();
        for (int i = 0; i < tryExcept.getHandlers().size(); i++) {
            randoms.add(callRandom(node));
        }

        IfNode ifNode = new IfNode(
                randoms,
                tryExcept.getHandlers(),
                tryExcept.getElze()
        );

        node.getParent().replace(node, ifNode);
        
    }
}
