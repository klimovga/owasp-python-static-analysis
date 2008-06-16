package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:16:54
 */
public class ReturnVisitor extends ASTVisitor {

    public ReturnVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ReturnVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException   {
        Node value = getFirstChild(getFirstChild(node, "value"), null);
        if (value == null || value.getParseNode() != null) {

            ParseNode statement = new ParseNode(
                    PhpSymbols.statement, "statement", currentFile);
            ParseNode unticked_statement = new ParseNode(
                    PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            statement.addChild(unticked_statement);
            ParseNode retVal = value == null ? null : value.getParseNode();
            if (retVal != null && retVal.getSymbol() == PhpSymbols.expr) {
                retVal = retVal.getChild(0);
                if (retVal.getSymbol() == PhpSymbols.r_cvar) {
                    retVal = retVal.getChild(0);
                }
            }

            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_RETURN, "T_RETURN", currentFile, "return", lineno));
            if (retVal != null) unticked_statement.addChild(makeCvar(retVal));
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));

            node.setParseNode(statement);
        }
    }

}
