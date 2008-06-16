package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:54:06
 */
public class CompareVisitor extends ASTVisitor {

    public CompareVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public CompareVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node expr = getFirstChild(node, "expr");
        Node ops = getFirstChild(node, "ops");
        Node rightNode = getFirstChild(ops, null);
        expr = getFirstChild(expr, null);

        if (expr.getParseNode() != null && rightNode.getParseNode() != null) {
            ParseNode left = makeExprInBraces(expr.getParseNode());
            ParseNode right = makeExprInBraces(rightNode.getParseNode());

            ParseNode expression = new ParseNode(PhpSymbols.expr, "expr", currentFile);
            ParseNode expr_without_variable = new ParseNode(PhpSymbols.expr_without_variable,
                    "expr_without_variable", currentFile);
            expression.addChild(expr_without_variable);

            ParseNode pnOp = makeCompare(node, lineno);
            if (pnOp != null) {
                expr_without_variable.addChild(left);
                expr_without_variable.addChild(pnOp);
                expr_without_variable.addChild(right);
            } else if (ops.getJdomElement().getTextTrim().equals("in")) { // "in" operator
                expression = makeFunctionCall("in_array", new ParseNode[]{ left, right } , null, lineno);
            } else if (ops.getJdomElement().getTextTrim().equals("not in")) { // "not in" operator
                expr_without_variable.addChild(new ParseNode(PhpSymbols.T_NOT, "T_NOT", currentFile, "!", lineno));
                expr_without_variable.addChild(makeFunctionCall("in_array", new ParseNode[]{ left, right } , null, lineno));
            } else {
                expression = null;
            }

            node.setParseNode(expression);
        }

    }
}
