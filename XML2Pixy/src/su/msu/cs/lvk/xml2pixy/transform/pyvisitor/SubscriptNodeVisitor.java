package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.SubscriptNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author gklimov
 * @created 15.03.2009 16:01:55
 */
public class SubscriptNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        SubscriptNode subscript = (SubscriptNode) node;
        PythonNode expr = subscript.getExpr();
        PythonNode sub = !subscript.getSubs().isEmpty() ? subscript.getSubs().get(0) : null;

        if (expr != null && expr.getPhpNode() != null && sub != null && sub.getPhpNode() != null) {

            ParseNode refVar = helper.create(PhpSymbols.reference_variable,
                    makeReferenceVariable(expr.getPhpNode()),
                    helper.create(PhpSymbols.T_OPEN_RECT_BRACES, "[", node.getLineno()),
                    helper.create(PhpSymbols.dim_offset, makeExpr(sub.getPhpNode())),
                    helper.create(PhpSymbols.T_CLOSE_RECT_BRACES, "]", node.getLineno())
            );

            subscript.setPhpNode(refVar);

        }

    }

}
