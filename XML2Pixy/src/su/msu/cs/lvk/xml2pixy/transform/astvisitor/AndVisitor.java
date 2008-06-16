package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:21:30
 */
public class AndVisitor extends ASTVisitor {

    public AndVisitor() {
        super();
    }

    public AndVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        ParseNode naryOpNode = makeNary(node, lineno);
        if (naryOpNode != null) {
            node.setParseNode(naryOpNode);
        }
    }

}
