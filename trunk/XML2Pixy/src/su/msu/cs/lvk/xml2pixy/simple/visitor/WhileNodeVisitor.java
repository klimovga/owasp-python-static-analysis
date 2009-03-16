package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.WhileNode;

/**
 * User: KlimovGA
 * Date: 03.12.2008
 */
public class WhileNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
/*
        WhileNode whileNode = (WhileNode) node;
*/
        super.visit(node);
    }
}
