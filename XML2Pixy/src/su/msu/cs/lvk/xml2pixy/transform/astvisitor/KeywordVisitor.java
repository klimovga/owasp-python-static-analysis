package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 01.02.2008
 * Time: 1:41:34
 */
public class KeywordVisitor extends ASTVisitor {

    public KeywordVisitor() {
        super();
    }

    public KeywordVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    /**
     * Actually do nothing. This is used after, in CallFuncVisitor.
     * Salls super version, because is not used properly. 
     * @param node node object
     * @param currentFile current file
     * @param lineno line number
     * @param module module name
     * @throws VisitorException
     */
    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
//        super.visit(node, currentFile, lineno, module);

        Node expr = getFirstChild(getFirstChild(node, "expr"), null);

        node.setParseNode(expr.getParseNode());

    }
}
