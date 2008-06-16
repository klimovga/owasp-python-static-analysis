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
 * Time: 15:43:54
 */
public class AssListVisitor extends ASTVisitor {

    public AssListVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public AssListVisitor() {
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
            ParseNode top_expr = new ParseNode(PhpSymbols.expr, "expr", currentFile);
            ParseNode top_expr_without_variable = new ParseNode(
                    PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
            top_expr.addChild(top_expr_without_variable);

            ParseNode assignment_list = null;

            if (nodes.getChildren().isEmpty()) {
                assignment_list = new ParseNode(
                        PhpSymbols.assignment_list, "assignment_list", currentFile);
                ParseNode assignment_list_element = new ParseNode(
                        PhpSymbols.assignment_list_element, "assignment_list_element", currentFile);
                assignment_list_element.addChild(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
                assignment_list.addChild(assignment_list_element);
            } else {
                for (Node child : nodes.getChildren()) {
                    ParseNode assignment_list_element = new ParseNode(
                            PhpSymbols.assignment_list_element, "assignment_list_element", currentFile);
                    assignment_list_element.addChild(child.getParseNode());
                    if (assignment_list == null) {
                        assignment_list = new ParseNode(
                                PhpSymbols.assignment_list, "assignment_list", currentFile);
                    } else {
                        ParseNode tmp = new ParseNode(
                                PhpSymbols.assignment_list, "assignment_list", currentFile);
                        tmp.addChild(assignment_list);
                        tmp.addChild(new ParseNode(
                                PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        assignment_list = tmp;
                    }
                    assignment_list.addChild(assignment_list_element);
                }
            }

            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_LIST, "T_LIST", currentFile, "list", lineno));
            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            top_expr_without_variable.addChild(assignment_list);
            top_expr_without_variable.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile,
                    ")", Utils.getLinenoRight(assignment_list, lineno)));

            node.setParseNode(top_expr);
        }
    }

}
