package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:12:43
 */
public class ModuleVisitor extends ASTVisitor {

    public ModuleVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ModuleVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String currentModule) throws VisitorException  {
        Node child = getFirstChild(node, "node");
        child = getFirstChild(child, null);
        ParseNode childParseNode = child == null ? null : child.getParseNode();
        if (childParseNode != null) {
            ParseNode module = new ParseNode(PhpSymbols.S, "S", currentFile);
            ParseNode top_statement_list = new ParseNode(
                    PhpSymbols.top_statement_list, "top_statement_list", currentFile);
            module.addChild(top_statement_list);
            ParseNode top_statement = new ParseNode(
                    PhpSymbols.top_statement, "top_statement", currentFile);
            top_statement_list.addChild(childParseNode);
            top_statement_list.addChild(top_statement);
            ParseNode statement = new ParseNode(
                    PhpSymbols.statement, "statement", currentFile);
            top_statement.addChild(statement);
            ParseNode unticked_statement = new ParseNode(
                    PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            statement.addChild(unticked_statement);
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile,
                    "?>", Utils.getLinenoRight(childParseNode, 0) + 1));
            node.setParseNode(module);
        }
    }

}
