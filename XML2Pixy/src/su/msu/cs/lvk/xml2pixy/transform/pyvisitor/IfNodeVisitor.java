package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.IfNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.List;

/**
 * @author gklimov
 * @created 15.03.2009 14:41:24
 */
public class IfNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        IfNode ifNode = (IfNode) node;

        List<PythonNode> tests = ifNode.getTests();
        List<StmtNode> stmts = ifNode.getStmts();
        StmtNode elze = ifNode.getElse();

        boolean first = true;
        ParseNode elseIfList = helper.create(PhpSymbols.elseif_list, makeEpsilon());
        ParseNode elseSingle = helper.create(PhpSymbols.else_single);

        ParseNode mainTest = null;
        ParseNode statement = null;

        for (int i = 0; i < tests.size(); i++) {
            PythonNode test = tests.get(i);
            StmtNode stmt = i < stmts.size() ? stmts.get(i) : null;
            ParseNode phpTest = test.getPhpNode();
            ParseNode phpStmt = stmt == null ? null : stmt.getPhpNode();

            if (phpTest != null && phpStmt != null) {
                // first test goes to IF
                if (first) {
                    first = false;

                    mainTest = makeExpr(phpTest);

                    statement = makeInnerStatementListInBraces(top2innerStatement(phpStmt), test.getLineno());

                } else { // others go to ELSEIF
                    elseIfList = helper.create(PhpSymbols.elseif_list,
                            elseIfList,
                            helper.create(PhpSymbols.T_ELSEIF, "elseif", test.getLineno()),
                            helper.create(PhpSymbols.T_OPEN_BRACES, "(", test.getLineno()),
                            makeExpr(phpTest),
                            helper.create(PhpSymbols.T_CLOSE_BRACES, ")", test.getLineno()),
                            makeInnerStatementListInBraces(top2innerStatement(phpStmt), test.getLineno())
                    );
                }
            }

        }

        if (elze != null && elze.getPhpNode() != null) {
            elseSingle.addChild(helper.create(PhpSymbols.T_ELSE, "else", node.getLineno()));
            elseSingle.addChild(makeInnerStatementListInBraces(top2innerStatement(elze.getPhpNode()), node.getLineno()));
        } else {
            elseSingle.addChild(makeEpsilon());
        }

        if (mainTest != null && statement != null) {

            ParseNode untickedStatement = helper.create(PhpSymbols.unticked_statement,
                    helper.create(PhpSymbols.T_IF, "if", node.getLineno()),
                    helper.create(PhpSymbols.T_OPEN_BRACES, "(", node.getLineno()),
                    mainTest,
                    helper.create(PhpSymbols.T_CLOSE_BRACES, ")", node.getLineno()),
                    statement,
                    elseIfList,
                    elseSingle
            );

            ifNode.setPhpNode(helper.create(PhpSymbols.statement, untickedStatement));
        }


    }
}
