package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;

/**
 * @author ikonv
 */
public class TransformerHelper {
    public static void insertExprAsStatementBefore(ParseNode statementNode, ParseNode expr) {
        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(statementNode.getFileName());
        ParseNode listNode = statementNode.getParent().getParent();

        ParseNode next = listNode.getChild(0);
        ParseNode newStatementList = helper.create(listNode.getSymbol(),
                next,
                helper.create(statementNode.getParent().getSymbol(),
                        helper.create(PhpSymbols.statement,
                                helper.create(PhpSymbols.unticked_statement,
                                        expr, helper.create(PhpSymbols.T_SEMICOLON, ";", -1)))));

        listNode.getChildren().set(0, newStatementList);
        newStatementList.setParent(listNode);
    }

    public static void insertStatementBefore(ParseNode statementNode, ParseNode statement) {
        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(statementNode.getFileName());
        ParseNode listNode = statementNode.getParent().getParent();

        ParseNode next = listNode.getChild(0);
        ParseNode newStatementList = helper.create(listNode.getSymbol(),
                next, helper.create(statementNode.getParent().getSymbol(), statement));

        listNode.getChildren().set(0, newStatementList);
        newStatementList.setParent(listNode);
    }

    public static ParseNode findParentStatement(ParseNode node) {
        ParseNode statement = node;
        while (statement != null && statement.getSymbol() != PhpSymbols.statement) {
            statement = statement.getParent();
        }

        return statement;
    }
}
