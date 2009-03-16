package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.AugAssignNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;

/**
 * @author gklimov
 * @created 14.03.2009 16:55:37
 */
public class AugAssignNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        AugAssignNode aug = (AugAssignNode) node;

        if (aug.getNode().getPhpNode() != null && aug.getExpr() != null) {
            ParseNode result = helper.create(PhpSymbols.expr,
                    helper.create(PhpSymbols.expr_without_variable,
                            makeCvar(aug.getNode().getPhpNode()),
                            makeAugAssign(aug.getOp(), node.getLineno()),
                            makeExpr(aug.getExpr().getPhpNode())
                    )
            );

            aug.setPhpNode(result);
        }



    }


}
