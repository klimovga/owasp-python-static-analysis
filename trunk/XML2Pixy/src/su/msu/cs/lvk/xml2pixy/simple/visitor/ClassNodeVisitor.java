package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.ClassNode;
import su.msu.cs.lvk.xml2pixy.ast.python.FunctionNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;

import java.util.List;

/**
 * User: klimov
 * Date: 13.01.2009
 */
public class ClassNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {

        ClassNode clazz = (ClassNode) node;
        StmtNode parentStmt = (StmtNode) clazz.getParent();
        List<PythonNode> stmts = clazz.getStmt().getNodes();
        for (PythonNode statement : stmts) {
            if (!(statement instanceof FunctionNode || statement instanceof ClassNode)) {
                parentStmt.addNode(parentStmt.getNodes().indexOf(clazz), statement);
            }
        }
        parentStmt.getNodes().remove(clazz);

    }
}                           
