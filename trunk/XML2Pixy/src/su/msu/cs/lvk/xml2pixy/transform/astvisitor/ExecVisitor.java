package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 30.04.2008
 * Time: 10:07:34
 */
public class ExecVisitor extends ASTVisitor {

    public ExecVisitor() {
        super();
    }

    public ExecVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public void visit(Node node, String currentFile1, int lineno, String module) throws VisitorException {

        Node expr = getFirstChild(getFirstChild(node, "expr"), null);

        if (expr.getParseNode() == null) {
            return;
        }

        node.setParseNode(makeFunctionCall("__builtin____exec", new ParseNode[]{expr.getParseNode()},
                null, getLineno(node.getJdomElement())));

    }
}
