package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.python.ModuleNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.StmtNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class ModuleNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        ModuleNode moduleNode = (ModuleNode) node;
        StmtNode stmt = moduleNode.getStmt();
        ParseNode childParseNode = stmt.getPhpNode();
        if (stmt.getPhpNode() != null) {
            ParseNode module = helper.create(PhpSymbols.S);
            ParseNode top_statement_list = helper.create(PhpSymbols.top_statement_list);
            module.addChild(top_statement_list);

            ParseNode top_statement = helper.createChain(new int[]{
                    PhpSymbols.top_statement,
                    PhpSymbols.statement,
                    PhpSymbols.unticked_statement,
            },
                    helper.create(PhpSymbols.T_SEMICOLON, "?>", Utils.getLinenoRight(childParseNode, 0) + 1));

            top_statement_list.addChild(childParseNode);
            top_statement_list.addChild(top_statement);

            node.setPhpNode(module);
        }

    }
}
