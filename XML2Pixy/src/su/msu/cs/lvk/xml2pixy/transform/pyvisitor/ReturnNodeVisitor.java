package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.ReturnNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author gklimov
 * @created 13.03.2009 17:08:34
 */
public class ReturnNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        ReturnNode ret = (ReturnNode) node;
        PythonNode retVal = ret.getValue();

        if (retVal == null || retVal.getPhpNode() != null) {

            ParseNode retPhp;
            if (retVal == null) {
                retPhp = helper.create(PhpSymbols.statement,
                        helper.create(PhpSymbols.unticked_statement,
                                helper.create(PhpSymbols.T_RETURN, "return", node.getLineno()),
                                helper.create(PhpSymbols.T_SEMICOLON, ";", node.getLineno())));
            } else {
                ParseNode retValPhp = retVal.getPhpNode();
                if (retValPhp.getSymbol() == PhpSymbols.expr) {
                    retValPhp = retValPhp.getChild(0);
                    if (retValPhp.getSymbol() == PhpSymbols.r_cvar) {
                        retValPhp = retValPhp.getChild(0);
                    }
                }

                retPhp = helper.create(PhpSymbols.statement,
                        helper.create(PhpSymbols.unticked_statement,
                                helper.create(PhpSymbols.T_RETURN, "return", node.getLineno()),
                                makeCvar(retValPhp),
                                helper.create(PhpSymbols.T_SEMICOLON, ";", node.getLineno())));
            }

            ret.setPhpNode(retPhp);
        }
    }
}
