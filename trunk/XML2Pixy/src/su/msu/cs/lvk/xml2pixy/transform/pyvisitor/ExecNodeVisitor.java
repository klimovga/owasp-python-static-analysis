package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.ast.python.ExecNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author gklimov
 * @created 15.03.2009 13:34:27
 */
public class ExecNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        ExecNode exec = (ExecNode) node;
        ParseNode phpExpr = exec.getExpr().getPhpNode();
        if (phpExpr != null) {
           exec.setPhpNode(makeFunctionCall("exec", new ParseNode[]{makeExpr(phpExpr)}, null, node.getLineno()));
        }
    }
}
