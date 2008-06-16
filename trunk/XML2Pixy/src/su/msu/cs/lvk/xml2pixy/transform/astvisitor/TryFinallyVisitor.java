package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;

/**
 * Simple visitor. Adds the body of try block and then the body of finally block
 *
 * @author ikonv
 */
public class TryFinallyVisitor extends ASTVisitor {

    public TryFinallyVisitor() {
        super();
    }

    public TryFinallyVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        ParseNode bodyNode = getFirstChild(node, "body").getChildren().get(0).getParseNode();
        ParseNode finalNode = getFirstChild(node, "final").getChildren().get(0).getParseNode();

        // add body node instead of epsilon in final node and get body + final
        ParseNode epsParent = finalNode;
        while (epsParent.getChild(0).getSymbol() != PhpSymbols.T_EPSILON) {
            if (epsParent.getSymbol() != PhpSymbols.top_statement_list
                    && epsParent.getSymbol() != PhpSymbols.inner_statement_list) {
                throw new AssertionError("Statement list expected");
            }
            
            epsParent = epsParent.getChild(0);
        }

        finalNode.getChildren().set(0, bodyNode);
        bodyNode.setParent(finalNode);

        node.setParseNode(finalNode);
    }
}
