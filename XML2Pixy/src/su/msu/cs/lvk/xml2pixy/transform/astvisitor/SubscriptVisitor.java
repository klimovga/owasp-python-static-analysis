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
 * Time: 15:18:36
 */
public class SubscriptVisitor extends ASTVisitor {

    public SubscriptVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public SubscriptVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        Node expr = getFirstChild(getFirstChild(node, "expr"), null);
        Node sub = getFirstChild(getFirstChild(node, "subs"), null);

        if (expr.getParseNode() != null && sub.getParseNode() != null) {
            ParseNode reference_variable = new ParseNode(PhpSymbols.reference_variable, "reference_variable", currentFile);

            ParseNode dim_offset = new ParseNode(PhpSymbols.dim_offset, "dim_offset", currentFile);
            dim_offset.addChild(makeExpr(sub.getParseNode()));

            reference_variable.addChild(makeReferenceVariable(expr.getParseNode()));
            reference_variable.addChild(new ParseNode(PhpSymbols.T_OPEN_RECT_BRACES, "T_OPEN_RECT_BRACES", currentFile, "[", lineno));
            reference_variable.addChild(dim_offset);
            reference_variable.addChild(new ParseNode(PhpSymbols.T_CLOSE_RECT_BRACES, "T_CLOSE_RECT_BRACES", currentFile, "]", Utils.getLinenoRight(dim_offset, lineno)));

            node.setParseNode(reference_variable);
        }
    }

}
