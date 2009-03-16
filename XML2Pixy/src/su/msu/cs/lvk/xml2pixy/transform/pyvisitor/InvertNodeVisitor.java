package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.InvertNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 19.01.2009
 */
public class InvertNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        InvertNode invert = (InvertNode) node;

        if (invert.getExpr().getPhpNode() != null) {
            invert.setPhpNode(helper.create(PhpSymbols.expr,
                    helper.create(PhpSymbols.expr_without_variable,
                            getUnaryOperation(invert.getNodeName(), invert.getLineno()),
                            makeExprInBraces(invert.getExpr().getPhpNode()))));
        }


    }

}
