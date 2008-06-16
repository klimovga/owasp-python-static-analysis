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
 * Time: 15:19:32
 */
public class ListVisitor extends ASTVisitor {
    public ListVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ListVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node nodes = getFirstChild(node, "nodes");
        boolean ready = true;
        for (Node child : nodes.getChildren()) {
            if (child.getParseNode() == null) {
                ready = false;
                break;
            }
        }
        if (ready) {
            ParseNode top_expr = new ParseNode(
                    PhpSymbols.expr, "expr", currentFile);
            ParseNode top_expr_without_variable = new ParseNode(
                    PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
            top_expr.addChild(top_expr_without_variable);
            ParseNode array_pair_list = new ParseNode(
                    PhpSymbols.array_pair_list, "array_pair_list", currentFile);
            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_ARRAY, "T_ARRAY", currentFile, "array", lineno));
            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            top_expr_without_variable.addChild(array_pair_list);
            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", Utils.getLinenoRight(array_pair_list, lineno)));

            if (nodes.getChildren().isEmpty()) {
                array_pair_list.addChild(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            } else {
                ParseNode non_empty_array_pair_list = null;
                ParseNode possible_comma = new ParseNode(
                        PhpSymbols.possible_comma, "possible_comma", currentFile);
                possible_comma.addChild(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
                for (Node child : nodes.getChildren()) {
                    if (non_empty_array_pair_list == null) {
                        non_empty_array_pair_list = new ParseNode(
                                PhpSymbols.non_empty_array_pair_list, "non_empty_array_pair_list", currentFile);
                    } else {
                        ParseNode tmp = new ParseNode(
                                PhpSymbols.non_empty_array_pair_list, "non_empty_array_pair_list", currentFile);
                        tmp.addChild(non_empty_array_pair_list);
                        tmp.addChild(new ParseNode(
                                PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        non_empty_array_pair_list = tmp;
                    }
                    non_empty_array_pair_list.addChild(makeExpr(child.getParseNode()));
                }
                array_pair_list.addChild(non_empty_array_pair_list);
                array_pair_list.addChild(possible_comma);
            }

            node.setParseNode(top_expr);
        }
    }

}
