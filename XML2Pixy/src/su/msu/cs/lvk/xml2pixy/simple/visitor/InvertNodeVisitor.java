package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

/**
 * User: KlimovGA
 * Date: 16.10.2008
 * Time: 10:48:43
 */
public class InvertNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        InvertNode invert = (InvertNode) node;
        PythonNode parent = node.getParent();
        if (!(parent instanceof AssignNode || parent instanceof StmtNode)) {
            String tmpVar = ProcessingUtils.getNextTempVar();
            // replace expression with temporary variable
            parent.replace(invert, new NameNode(tmpVar).copyLocation(invert));

            addToNearestStmt(parent, new AssignNode(new AssNameNode(tmpVar).copyLocation(invert), invert).copyLocation(invert));
        }
    }

}
