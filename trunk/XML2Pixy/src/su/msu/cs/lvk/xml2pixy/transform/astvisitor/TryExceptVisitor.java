package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;

/**
 * @author ikonv
 */
public class TryExceptVisitor extends ASTVisitor {

    public TryExceptVisitor() {
    }

    public TryExceptVisitor(SymbolTable table) {
        super(table);
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        // ignore handlers and insert try body
        ParseNode bodyNode = getFirstChild(node, "body").getChildren().get(0).getParseNode();
        node.setParseNode(bodyNode);
    }
}
