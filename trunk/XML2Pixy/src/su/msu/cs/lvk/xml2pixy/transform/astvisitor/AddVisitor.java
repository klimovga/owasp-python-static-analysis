package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:17:52
 */
public class AddVisitor extends ASTVisitor {

    public AddVisitor() {
        super();
    }

    public AddVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        ParseNode binaryOpNode = makeBinary(node, lineno);
        if (binaryOpNode != null) {
            node.setParseNode(binaryOpNode);
        }
    }

}
