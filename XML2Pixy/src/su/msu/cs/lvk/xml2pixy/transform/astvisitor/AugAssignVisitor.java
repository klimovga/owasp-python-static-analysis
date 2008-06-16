package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:29:11
 */
public class AugAssignVisitor extends ASTVisitor {

    public AugAssignVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public AugAssignVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node rarg = getFirstChild(getFirstChild(node, "expr"), null);
        Node larg = getFirstChild(getFirstChild(node, "node"), null);
        if (rarg != null && rarg.getParseNode() != null &&
                larg != null && larg.getParseNode() != null) {
            ParseNode expr = new ParseNode(PhpSymbols.expr, "expr", currentFile);
            ParseNode expr_without_variable = new ParseNode(PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
            expr.addChild(expr_without_variable);

            expr_without_variable.addChild(makeCvar(larg.getParseNode()));
            expr_without_variable.addChild(makeAugAssign(
                    node.getJdomElement().getAttributeValue("op"), lineno));
            expr_without_variable.addChild(makeExpr(rarg.getParseNode()));

            node.setParseNode(expr);
        }
    }

}
