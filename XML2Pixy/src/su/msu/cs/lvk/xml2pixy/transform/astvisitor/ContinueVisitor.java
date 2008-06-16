package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:56:00
 */
public class ContinueVisitor extends ASTVisitor {

    public ContinueVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ContinueVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        ParseNode statement = new ParseNode(
                PhpSymbols.statement, "statement", currentFile);
        ParseNode unticked_statement = new ParseNode(
                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
        statement.addChild(unticked_statement);

        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_CONTINUE, "T_CONTINUE", currentFile, "continue", lineno));
        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));
        node.setParseNode(statement);
    }

}
