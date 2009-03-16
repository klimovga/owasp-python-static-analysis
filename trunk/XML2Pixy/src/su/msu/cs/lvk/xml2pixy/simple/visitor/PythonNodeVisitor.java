package su.msu.cs.lvk.xml2pixy.simple.visitor;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.python.*;

import java.util.Arrays;

/**
 * User: KlimovGA
 * Date: 16.10.2008
 * Time: 9:39:48
 */
public class PythonNodeVisitor {

    private static final Logger logger = Logger.getLogger(PythonNodeVisitor.class);

    public void visit(PythonNode node) {
//        logger.trace(node.getNodeName());
    }

    protected StmtNode getNearestStmt(PythonNode node) {
        while (node != null && node.getClass() != StmtNode.class) {
            node = node.getParent();
        }

        return (StmtNode) node;
    }

    protected void addToNearestStmt(PythonNode node, PythonNode what) {
        if (what != null) {
            PythonNode child = null;
            while (node != null && node.getClass() != StmtNode.class) {
                child = node;
                node = node.getParent();
            }

            if (node != null) {
                StmtNode stmt = (StmtNode) node;
                if (child != null) {
                    stmt.addNode(stmt.getNodes().indexOf(child), what);
                } else {
                    stmt.addNode(what);
                }
            } else {
                throw new RuntimeException("Couldn't find ancestor StmtNode");
            }
        }
    }

    protected PythonNode callRandom(PythonNode location) {
        return new CallFuncNode(new NameNode("random__randint").copyLocation(location),
                                Arrays.asList(new ConstNode(0).copyLocation(location), new ConstNode(1).copyLocation(location)),
                                null, null).copyLocation(location);
    }

}
