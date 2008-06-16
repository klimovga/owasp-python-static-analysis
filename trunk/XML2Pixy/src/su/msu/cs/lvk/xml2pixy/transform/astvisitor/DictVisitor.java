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
 * Time: 14:57:46
 */
public class DictVisitor extends ASTVisitor {

    public DictVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public DictVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node items = getFirstChild(node, "items");
        boolean ready = items.getChildren().size() % 2 == 0;
        if (ready) for (Node child : items.getChildren()) {
            if (child.getParseNode() == null) {
                ready = false;
                break;
            }
        }

        if (ready) {
            ParseNode top_expr = new ParseNode(PhpSymbols.expr, "expr", currentFile);
            ParseNode top_expr_without_variable = new ParseNode(PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
            top_expr.addChild(top_expr_without_variable);
            ParseNode array_pair_list = new ParseNode(PhpSymbols.array_pair_list, "array_pair_list", currentFile);
            top_expr_without_variable.addChild(new ParseNode(PhpSymbols.T_ARRAY, "T_ARRAY", currentFile, "array", lineno));
            top_expr_without_variable.addChild(new ParseNode(PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            top_expr_without_variable.addChild(array_pair_list);
            top_expr_without_variable.addChild(new ParseNode(PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", Utils.getLinenoRight(array_pair_list, lineno)));

            if (items.getChildren().isEmpty()) {
                array_pair_list.addChild(new ParseNode(PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            } else {
                ParseNode non_empty_array_pair_list = null;
                ParseNode possible_comma = new ParseNode(PhpSymbols.possible_comma, "possible_comma", currentFile);
                possible_comma.addChild(new ParseNode(PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
                for (int i = 0; i < items.getChildren().size(); i += 2) {
                    Node key = items.getChildren().get(i);
                    Node value = items.getChildren().get(i + 1);
                    if (non_empty_array_pair_list == null) {
                        non_empty_array_pair_list = new ParseNode(PhpSymbols.non_empty_array_pair_list, "non_empty_array_pair_list", currentFile);
                    } else {
                        ParseNode tmp = new ParseNode(PhpSymbols.non_empty_array_pair_list, "non_empty_array_pair_list", currentFile);
                        tmp.addChild(non_empty_array_pair_list);
                        tmp.addChild(new ParseNode(PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        non_empty_array_pair_list = tmp;
                    }
                    non_empty_array_pair_list.addChild(makeExpr(key.getParseNode()));
                    non_empty_array_pair_list.addChild(new ParseNode(PhpSymbols.T_DOUBLE_ARROW, "T_DOUBLE_ARROW", currentFile, "=>", lineno));
                    non_empty_array_pair_list.addChild(makeExpr(value.getParseNode()));
                }
                array_pair_list.addChild(non_empty_array_pair_list);
                array_pair_list.addChild(possible_comma);
            }

            node.setParseNode(top_expr);
        }
    }

}
