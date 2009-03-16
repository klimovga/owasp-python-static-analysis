package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

/**
 * User: KlimovGA
 * Date: 16.10.2008
 * Time: 10:13:25
 */
public class AddNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        AddNode add = (AddNode) node;
        PythonNode parent = node.getParent();
        if (!(parent instanceof AssignNode || parent instanceof StmtNode || parent instanceof AugAssignNode)) {
            String tmpVar = ProcessingUtils.getNextTempVar();
            // replace expression with temporary variable
            parent.replace(add, new NameNode(tmpVar).copyLocation(add));

            addToNearestStmt(parent, new AssignNode(new AssNameNode(tmpVar).copyLocation(add), add).copyLocation(add));
        }
    }
}
