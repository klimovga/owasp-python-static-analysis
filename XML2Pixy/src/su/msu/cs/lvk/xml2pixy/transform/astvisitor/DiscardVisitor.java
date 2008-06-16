package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:58:29
 */
public class DiscardVisitor extends ASTVisitor {

    public DiscardVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public DiscardVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        // Probably: is used when a possible result is ignored
        Node expr = getFirstChild(getFirstChild(node, "expr"), null);
        if (expr != null && expr.getParseNode() != null) {
            node.setParseNode(expr.getParseNode());
        }
    }
}
