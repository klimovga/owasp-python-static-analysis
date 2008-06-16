package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 *
 */
public class ForVisitor extends ASTVisitor {

    protected static Logger log = Logger.getLogger(ForVisitor.class.getName());

    public ForVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ForVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        //TODO foreach with dictionaries
        Node body = getFirstChild(getFirstChild(node, "body"), null);
        Node assign = getFirstChild(getFirstChild(node, "assign"), null);
        Node list = getFirstChild(getFirstChild(node, "list"), null);


        if (body.getParseNode() != null &&
                assign.getParseNode() != null &&
                list.getParseNode() != null) {

            if (!assign.getJdomElement().getName().equals("AssName")) {
                log.warn("WARNING: complex FOR loops is not supported (" +
                        currentFile + ':' + lineno + ")");
            }

            ParseNode statement = new ParseNode(
                    PhpSymbols.statement, "statement", currentFile);
            ParseNode unticked_statement = new ParseNode(
                    PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            statement.addChild(unticked_statement);

            ParseNode foreach_statement = new ParseNode(
                    PhpSymbols.foreach_statement, "foreach_statement", currentFile);
            ParseNode key = new ParseNode(
                    PhpSymbols.w_cvar, "w_cvar", currentFile);
            key.addChild(assign.getParseNode());
            ParseNode array;
            if (list.getParseNode().getSymbol() == PhpSymbols.expr) {
                array = list.getParseNode().getChild(0);
            } else {
                array = new ParseNode(
                        PhpSymbols.w_cvar, "w_cvar", currentFile);
                array.addChild(makeCvar(list.getParseNode()));
            }

            foreach_statement.addChild(makeInnerStatementListInBraces(
                    top2innerStatement(body.getParseNode()), lineno));

            ParseNode foreach_optional_arg = new ParseNode(
                    PhpSymbols.foreach_optional_arg, "foreach_optional_arg", currentFile);
            foreach_optional_arg.addChild(new ParseNode(
                    PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));

            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_FOREACH, "T_FOREACH", currentFile, "foreach", lineno));
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            unticked_statement.addChild(array);
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_AS, "T_AS", currentFile, "as", lineno));
            unticked_statement.addChild(key);
            unticked_statement.addChild(foreach_optional_arg);
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile,
                    ")", Utils.getLinenoRight(key, lineno)));
            unticked_statement.addChild(foreach_statement);

            node.setParseNode(statement);
        }
    }
}
