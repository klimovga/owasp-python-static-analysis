package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class AssignNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        AssignNode assign = (AssignNode) node;

        StmtNode stmt = (StmtNode) assign.getParent();

        if (assign.getNodes().size() > 1) {
            int index = stmt.getNodes().indexOf(assign);
            String tmpVar = ProcessingUtils.getNextTempVar();
            stmt.getNodes().remove(assign);
            stmt.addNode(index, new AssignNode(new AssNameNode(tmpVar).copyLocation(assign), assign.getExpr()).copyLocation(assign));

            for (PythonNode child : assign.getNodes()) {
                stmt.addNode(++index, new AssignNode(child, new NameNode(tmpVar).copyLocation(assign)).copyLocation(assign));
            }
        }
    }
}
