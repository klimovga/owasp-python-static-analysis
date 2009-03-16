package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PrintnlNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class PrintnlNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {

        PrintnlNode print = (PrintnlNode) node;

        ParseNode current = null;
        for (PythonNode child : print.getNodes()) {
            if (child.getPhpNode() != null) {
                if (current == null) {
                    current = helper.create(PhpSymbols.echo_expr_list);
                } else {
                    current = helper.create(PhpSymbols.echo_expr_list,
                            current,
                            helper.create(PhpSymbols.T_COMMA, ",", print.getLineno()));
                }
                current.addChild(makeExpr(child.getPhpNode()));
            }
        }
        if (current != null) {
            node.setPhpNode(helper.create(PhpSymbols.statement,
                    helper.create(PhpSymbols.unticked_statement,
                            helper.create(PhpSymbols.T_ECHO, "echo", node.getLineno()),
                            current,
                            helper.create(PhpSymbols.T_SEMICOLON, ";", node.getLineno()))
            ));

        }

    }
}
