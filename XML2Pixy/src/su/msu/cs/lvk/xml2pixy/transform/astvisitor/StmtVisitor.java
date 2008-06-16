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
 * Time: 15:17:28
 */
public class StmtVisitor extends ASTVisitor {

    public StmtVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public StmtVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        List<Node> children = getFirstChild(node, "nodes").getChildren();
/*
        boolean ready = true;
        for (Node child : children) {
            if (child.getParseNode() == null) {
                ready = false;
                break;
            }
        }
        if (ready) {
*/
            ParseNode current = new ParseNode(
                    PhpSymbols.top_statement_list, "top_statement_list", currentFile);
            current.addChild(new ParseNode(
                    PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            for (Node child : children) {
                if (child.getJdomElement().getName().equals("Pass")
                        || child.getParseNode() == null // just ignore unsupported statements
                        || child.getParseNode().getSymbol() == PhpSymbols.T_EPSILON) {
                    continue;
                }

                ParseNode statement = child.getParseNode();

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


                    ParseNode top_statement_list = new ParseNode(
                            PhpSymbols.top_statement_list, "top_statement_list", currentFile);
                    ParseNode top_statement = new ParseNode(
                            PhpSymbols.top_statement, "top_statement", currentFile);
                    top_statement_list.addChild(current);
                    top_statement_list.addChild(top_statement);


                    if (statement.getSymbol() != PhpSymbols.statement &&
                            statement.getSymbol() != PhpSymbols.declaration_statement) {
                        statement = new ParseNode(
                                PhpSymbols.statement, "statement", currentFile);
                        ParseNode unticked_statement = new ParseNode(
                                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
                        statement.addChild(unticked_statement);
                        unticked_statement.addChild(makeExpr(child.getParseNode()));
                        unticked_statement.addChild(new ParseNode(
                                PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile,
                                ";", getLineno(child.getJdomElement())));

                    }

                    top_statement.addChild(statement);

                    current = top_statement_list;
                }
            }
            node.setParseNode(current);
//        }
    }

}
