package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:08:55
 */
public class IfVisitor extends ASTVisitor {

    public IfVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public IfVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node else_ = getFirstChild(node, "else_");
        Node tests = getFirstChild(node, "tests");
        boolean ready = tests.getChildren().size() % 2 == 0;
        if (ready) for (Node child : else_.getChildren()) {
            if (child.getParseNode() == null) {
                ready = false;
                break;
            }
        }
        if (ready) for (Node child : tests.getChildren()) {
            if (child.getParseNode() == null) {
                ready = false;
                break;
            }
        }

        if (ready) {
            ParseNode statement = new ParseNode(
                    PhpSymbols.statement, "statement", currentFile);
            ParseNode unticked_statement = new ParseNode(
                    PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            statement.addChild(unticked_statement);

            ParseNode firstExpr = null;
            ParseNode firstStmt = null;
            ParseNode elseIfEpsilon = new ParseNode(
                    PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2);
            ParseNode elseIf = new ParseNode(
                    PhpSymbols.elseif_list, "elseif_list", currentFile);
            elseIf.addChild(elseIfEpsilon);
            for (int i = 0; i < tests.getChildren().size(); i += 2) {
                Node expr = tests.getChildren().get(i);
                Node stmt = tests.getChildren().get(i + 1);
                if (firstExpr == null || firstStmt == null) {
                    firstExpr = expr.getParseNode();
                    firstStmt = stmt.getParseNode();
                } else {
                    ParseNode current = new ParseNode(
                            PhpSymbols.elseif_list, "elseif_list", currentFile);
                    current.addChild(elseIf);
                    current.addChild(new ParseNode(
                            PhpSymbols.T_ELSEIF, "T_ELSEIF", currentFile,
                            "elseif", getLineno(expr.getJdomElement())));
                    current.addChild(new ParseNode(
                            PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile,
                            "(", getLineno(expr.getJdomElement())));
                    current.addChild(makeExpr(expr.getParseNode()));
                    current.addChild(new ParseNode(
                            PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile,
                            ")", Utils.getLinenoRight(expr.getParseNode(), getLineno(expr.getJdomElement()))));
                    current.addChild(makeInnerStatementListInBraces(
                            top2innerStatement(stmt.getParseNode()),
                            Utils.getLinenoLeft(stmt.getParseNode(), lineno)));

                    elseIf = current;
                }
            }

            ParseNode else_single = new ParseNode(PhpSymbols.else_single, "else_single", currentFile);
            Node elseStmt = getFirstChild(else_, null);
            if (elseStmt != null && elseStmt.getParseNode() != null) {
                else_single.addChild(new ParseNode(
                        PhpSymbols.T_ELSE, "T_ELSE", currentFile, "else",
                        Utils.getLinenoLeft(elseStmt.getParseNode(), lineno)));
                else_single.addChild(makeInnerStatementListInBraces(
                        top2innerStatement(elseStmt.getParseNode()),
                        Utils.getLinenoLeft(elseStmt.getParseNode(), lineno)));
            } else {
                else_single.addChild(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            }

            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_IF, "T_IF", currentFile, "if", lineno));
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            unticked_statement.addChild(makeExpr(firstExpr)); //expr
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")",
                    Utils.getLinenoRight(firstExpr, lineno)));
            unticked_statement.addChild(makeInnerStatementListInBraces(
                    top2innerStatement(firstStmt), lineno)); //stmt
            unticked_statement.addChild(elseIf); //elseif
            unticked_statement.addChild(else_single); //else

            node.setParseNode(statement);
        }
    }
}
