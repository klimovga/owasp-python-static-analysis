package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.ConstNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class ConstNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {

        ConstNode constNode = (ConstNode) node;


        if (constNode.getValue() != null) {
            ParseNode phpConst = null;
            if (constNode.getValue() instanceof Integer) {
                phpConst = helper.create(PhpSymbols.T_LNUMBER, constNode.getValue().toString(), constNode.getLineno());
            } else if (constNode.getValue() instanceof Double) {
                phpConst = helper.create(PhpSymbols.T_DNUMBER, constNode.getValue().toString(), constNode.getLineno());
            } else {
                phpConst = helper.create(PhpSymbols.T_CONSTANT_ENCAPSED_STRING,
                        constNode.getValue().toString(), constNode.getLineno());
            }

            constNode.setPhpNode(
                    helper.createChain(new int[]{
                            PhpSymbols.expr,
                            PhpSymbols.expr_without_variable,
                            PhpSymbols.scalar,
                            PhpSymbols.common_scalar
                    }, phpConst)
            );
        }

    }
}
