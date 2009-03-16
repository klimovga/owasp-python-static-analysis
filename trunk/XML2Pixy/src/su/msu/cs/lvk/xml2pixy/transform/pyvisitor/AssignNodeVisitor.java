package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.AssListNode;
import su.msu.cs.lvk.xml2pixy.ast.python.AssNameNode;
import su.msu.cs.lvk.xml2pixy.ast.python.AssignNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class AssignNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {

        AssignNode assign = (AssignNode) node;
        PythonNode child = assign.getNodes().get(0);

        ParseNode phpAssign = null;
        if (assign.getExpr().getPhpNode() != null && child != null) {
            if (child instanceof AssListNode) {
                phpAssign = helper.create(PhpSymbols.expr_without_variable,
                        helper.create(PhpSymbols.T_LIST, "list", child.getLineno()),
                        helper.create(PhpSymbols.T_OPEN_BRACES, "(", child.getLineno()),
                        child.getPhpNode(),
                        helper.create(PhpSymbols.T_CLOSE_BRACES, ")", child.getLineno())
                );
            } else if (child instanceof AssNameNode) {
                AssNameNode assName = (AssNameNode) child;
                phpAssign = helper.create(PhpSymbols.expr_without_variable,
                        makeCvar(makeReferenceVariableByName(assName.getName(), assName.getLineno()))
                );
            }
        }

        if (phpAssign != null) {
            helper.addChild(phpAssign, helper.create(PhpSymbols.T_ASSIGN, "=", assign.getLineno()))
                    .addChild(makeExpr(assign.getExpr().getPhpNode()));
            assign.setPhpNode(helper.create(PhpSymbols.expr, phpAssign));
        }

    }
}
