package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:14:51
 */
public class PassVisitor extends ASTVisitor {

    public PassVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public PassVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        node.setParseNode(new ParseNode(
                PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
    }
}
