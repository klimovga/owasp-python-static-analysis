package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class StmtNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        StmtNode stmt = (StmtNode) node;

        ParseNode current = helper.createChain(new int[]{PhpSymbols.top_statement_list}, makeEpsilon());

        for (PythonNode child : stmt.getNodes()) {
            if (child.getPhpNode() == null // just ignore unsupported statements
                    || child.getPhpNode().getSymbol() == PhpSymbols.T_EPSILON) {
                continue;
            }

            ParseNode statement = child.getPhpNode();

            if (statement.getSymbol() == PhpSymbols.top_statement_list) {
                ParseNode last = statement;
                while (last.getSymbol() == PhpSymbols.top_statement_list) {
                    last = last.getChild(0);
                }
                last = last.getParent();
                last.getChildren().clear();
                for (Object obj : current.getChildren()) {
                    last.addChild((ParseNode) obj);
                }

                current = statement;
            } else {
                if (statement.getSymbol() != PhpSymbols.statement &&
                        statement.getSymbol() != PhpSymbols.declaration_statement) {
                    statement = helper.create(PhpSymbols.statement,
                            helper.create(PhpSymbols.unticked_statement,
                                    makeExpr(child.getPhpNode()),
                                    helper.create(PhpSymbols.T_SEMICOLON, ";", child.getLineno())));
                }

                current = helper.create(PhpSymbols.top_statement_list,
                        current,
                        helper.create(PhpSymbols.top_statement, statement)
                );

            }

        }

        node.setPhpNode(current);

    }
}
