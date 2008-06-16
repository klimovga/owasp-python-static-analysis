package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:10:42
 */
public class NotVisitor extends ASTVisitor {

    public NotVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public NotVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        ParseNode unaryOpNode = makeUnary(node, lineno);
        if (unaryOpNode != null) {
            node.setParseNode(unaryOpNode);
        }
    }

}
