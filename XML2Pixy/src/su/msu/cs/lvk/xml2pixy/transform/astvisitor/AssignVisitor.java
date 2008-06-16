package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:26:53
 */
public class AssignVisitor extends ASTVisitor {

    public AssignVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public AssignVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node expr = getFirstChild(node, "expr");
        expr = getFirstChild(expr, null);
        Node larg = getFirstChild(node, "nodes");

        boolean ready = expr.getParseNode() != null;
        if (ready) {
            for (Node child : larg.getChildren()) {
                if (child.getParseNode() == null) {
                    ready = false;
                    break;
                }
            }
        }

        if (ready) {
            ParseNode curr_expr = null;

            List<Node> largs = new ArrayList<Node>(larg.getChildren());
            Collections.reverse(largs);
            for (Node left : largs) {
                ParseNode assExpr = new ParseNode(
                        PhpSymbols.expr, "expr", currentFile);
                ParseNode expr_without_variable = new ParseNode(
                        PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
                assExpr.addChild(expr_without_variable);

                if (left.getJdomElement().getName().equals("AssTuple") ||
                        left.getJdomElement().getName().equals("AssList")) {
                    for (Object obj : left.getParseNode().getChild(0).getChildren()) {
                        ParseNode child = (ParseNode) obj;
                        expr_without_variable.addChild(child);
                    }
                } else {
                    expr_without_variable.addChild(makeCvar(left.getParseNode()));
                }
                expr_without_variable.addChild(new ParseNode(
                        PhpSymbols.T_ASSIGN, "T_ASSIGN", currentFile, "=", lineno));
                expr_without_variable.addChild(curr_expr == null
                        ? makeExpr(expr.getParseNode())
                        : curr_expr);

                curr_expr = assExpr;
            }

            node.setParseNode(curr_expr);
        }
    }

}
