package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author gklimov
 * @created 15.03.2009 12:30:17
 */
public class BreakNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        node.setPhpNode(
                helper.create(PhpSymbols.statement,
                        helper.create(PhpSymbols.unticked_statement,
                                helper.create(PhpSymbols.T_BREAK, "break", node.getLineno()),
                                helper.create(PhpSymbols.T_SEMICOLON, ";", node.getLineno())
                        )
                )
        );
    }

}
