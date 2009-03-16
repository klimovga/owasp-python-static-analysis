package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.ForNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author Panther
 * @created 12.03.2009 1:20:58
 */
public class ForNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        ForNode forNode = (ForNode) node;

        ParseNode list = forNode.getList().getPhpNode();

        if (list != null && forNode.getAssign().getPhpNode() != null && forNode.getBody() != null) {
            ParseNode stmt = helper.create(PhpSymbols.unticked_statement,
                    helper.create(PhpSymbols.T_FOREACH, "foreach", node.getLineno()),
                    helper.create(PhpSymbols.T_OPEN_BRACES, "(", node.getLineno()),
                    list.getSymbol() == PhpSymbols.expr
                            ? list.getChild(0)
                            : helper.create(PhpSymbols.w_cvar, makeCvar(list)),
                    helper.create(PhpSymbols.T_AS, "as", node.getLineno()),
                    helper.create(PhpSymbols.w_cvar, makeCvar(forNode.getAssign().getPhpNode())),
                    helper.create(PhpSymbols.foreach_optional_arg, makeEpsilon()),
                    helper.create(PhpSymbols.T_CLOSE_BRACES, ")", node.getLineno()),
                    helper.create(PhpSymbols.foreach_statement,
                            helper.create(PhpSymbols.statement, helper.create(PhpSymbols.unticked_statement,
                                    helper.create(PhpSymbols.T_OPEN_CURLY_BRACES, "{", node.getLineno()),
                                    top2innerStatement(forNode.getBody().getPhpNode()),
                                    helper.create(PhpSymbols.T_CLOSE_CURLY_BRACES, "}", node.getLineno())
                            ))
                    )
            );

            forNode.setPhpNode(helper.create(PhpSymbols.statement, stmt));
        }
    }

}