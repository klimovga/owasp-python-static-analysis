package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.AddNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 19.01.2009
 */
public class AddNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        AddNode add = (AddNode) node;
        if (add.getLeft().getPhpNode() != null && add.getRight().getPhpNode() != null) {
            add.setPhpNode(helper.create(PhpSymbols.expr,
                    helper.create(PhpSymbols.expr_without_variable,
                            makeExprInBraces(add.getLeft().getPhpNode()),
                            getBinaryOperation(add.getNodeName(), add.getLineno()),
                            makeExprInBraces(add.getRight().getPhpNode())
                    )));
        }
    }

}
