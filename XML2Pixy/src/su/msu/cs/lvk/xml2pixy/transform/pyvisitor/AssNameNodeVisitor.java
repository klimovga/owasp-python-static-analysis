package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.AssNameNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class AssNameNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {

        AssNameNode assName = (AssNameNode) node;

        assName.setPhpNode(helper.createChain(
                new int[]{
                        PhpSymbols.cvar,
                        PhpSymbols.cvar_without_objects
                },
                makeReferenceVariableByName(assName.getName(), assName.getLineno())));
    }
}
