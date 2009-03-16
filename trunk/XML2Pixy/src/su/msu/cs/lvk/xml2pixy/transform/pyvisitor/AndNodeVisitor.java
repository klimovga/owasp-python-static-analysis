package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.AndNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 19.01.2009
 */
public class AndNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        AndNode and = (AndNode) node;

        ParseNode left = null;
        ParseNode right = null;

        for (PythonNode arg : and.getNodes()) {
            if (left == null) {
                if (arg != null) {
                    left = arg.getPhpNode();
                }
            } else {
                if (arg != null) {
                    right = arg.getPhpNode();
                }

                left = makeOperation(left, right, and);
            }
        }

        if (right != null) {
            and.setPhpNode(left);
        }

    }

    private ParseNode makeOperation(ParseNode left, ParseNode right, AndNode naryNode) throws VisitorException {
        return helper.create(PhpSymbols.expr,
                    helper.create(PhpSymbols.expr_without_variable,
                            makeExprInBraces(left),
                            getNaryOperation(naryNode.getNodeName(), naryNode.getLineno()),
                            makeExprInBraces(right)));
    }
}
