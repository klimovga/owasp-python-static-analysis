package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 *
 */
public class WhileVisitor extends ASTVisitor {

    protected static Logger log = Logger.getLogger(SliceVisitor.class.getName());

    public WhileVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public WhileVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        Node test = getFirstChild(getFirstChild(node, "test"), null);
        Node body = getFirstChild(getFirstChild(node, "body"), null);

        if (test.getParseNode() != null && body.getParseNode() != null) {
            ParseNode statement = new ParseNode(
                    PhpSymbols.statement, "statement", currentFile);
            ParseNode unticked_statement = new ParseNode(
                    PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            statement.addChild(unticked_statement);

            ParseNode while_statement = new ParseNode(
                    PhpSymbols.while_statement, "while_statement", currentFile);
            while_statement.addChild(makeInnerStatementListInBraces(
                    top2innerStatement(body.getParseNode()), lineno));

            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_WHILE, "T_WHILE", currentFile, "while", lineno));
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            unticked_statement.addChild(makeConditionExpression(test.getParseNode()));
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile,
                    ")", Utils.getLinenoRight(test.getParseNode(), lineno)));
            unticked_statement.addChild(while_statement);

            node.setParseNode(statement);
        }
    }

    private ParseNode makeConditionExpression(ParseNode test) {
        if (test.getSymbol() == PhpSymbols.expr) {
            return test;
        } else if (test.getSymbol() == PhpSymbols.reference_variable) {
            // special case like: while ($foo) { ... }
            ParseNodeHelper helper = new ParseNodeHelper();
            return helper.createChain(new int[] {
                    PhpSymbols.expr, PhpSymbols.r_cvar, PhpSymbols.cvar, PhpSymbols.cvar_without_objects
                }, test);
        } else {
            log.error("Wrong condition node in while: line " + test.getLineno());
            return test;
        }
    }

}
