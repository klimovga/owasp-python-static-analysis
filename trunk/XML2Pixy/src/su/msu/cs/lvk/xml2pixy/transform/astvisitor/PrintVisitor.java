package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:15:20
 */
public class PrintVisitor extends ASTVisitor {

    public PrintVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public PrintVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        boolean ready = true;
        for (Node child : getFirstChild(node, "nodes").getChildren()) {
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

            List<Node> printNodes = getFirstChild(node, "nodes").getChildren();
            if (printNodes.isEmpty()) {
                node.setParseNode(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            } else {
                ParseNode current = null;
                for (Node child : printNodes) {
                    if (current == null) {
                        current = new ParseNode(
                                PhpSymbols.echo_expr_list, "echo_expr_list", currentFile);
                    } else {
                        ParseNode echo_expr_list = new ParseNode(
                                PhpSymbols.echo_expr_list, "echo_expr_list", currentFile);
                        echo_expr_list.addChild(current);
                        echo_expr_list.addChild(new ParseNode(
                                PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        current = echo_expr_list;
                    }
                    current.addChild(makeExpr(child.getParseNode()));
                }


                unticked_statement.addChild(new ParseNode(
                        PhpSymbols.T_ECHO, "T_ECHO", currentFile, "echo", lineno));
                unticked_statement.addChild(current);
                unticked_statement.addChild(new ParseNode(
                        PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));

                node.setParseNode(statement);
            }
        }
    }
}
