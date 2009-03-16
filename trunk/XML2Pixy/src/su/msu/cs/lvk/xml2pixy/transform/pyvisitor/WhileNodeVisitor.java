package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.WhileNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author gklimov
 * @created 15.03.2009 16:27:38
 */
public class WhileNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        WhileNode whileNode = (WhileNode) node;

        if (whileNode.getTest().getPhpNode() != null &&
                whileNode.getBody().getPhpNode() != null &&
                (whileNode.getElse() == null || whileNode.getElse().getPhpNode() != null)) {
            ParseNode stmt = helper.create(PhpSymbols.statement,
                    helper.create(PhpSymbols.unticked_statement,
                            helper.create(PhpSymbols.T_WHILE, "while", whileNode.getLineno()),
                            helper.create(PhpSymbols.T_OPEN_BRACES, "(", whileNode.getLineno()),
                            makeExpr(whileNode.getTest().getPhpNode()),
                            helper.create(PhpSymbols.T_CLOSE_BRACES, ")", whileNode.getLineno()),
                            helper.create(PhpSymbols.while_statement,
                                    makeInnerStatementListInBraces(top2innerStatement(whileNode.getBody().getPhpNode()), node.getLineno())
                            )
                    )
            );

            whileNode.setPhpNode(stmt);
        }
    }
}
