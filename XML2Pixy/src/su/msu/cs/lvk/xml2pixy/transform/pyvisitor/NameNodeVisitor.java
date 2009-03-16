package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.NameNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class NameNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        NameNode nameNode = (NameNode) node;
        String name = nameNode.getName();
        if (name.equals("True") || name.equals("False") || name.equals("None")) {
            String value = name.equals("True") ? "true" : name.equals("False") ? "false" : "null";
            nameNode.setPhpNode(helper.createChain(
                    new int[]{
                            PhpSymbols.expr,
                            PhpSymbols.expr_without_variable,
                            PhpSymbols.scalar
                    },
                    helper.create(PhpSymbols.T_STRING, value, node.getLineno())

            ));
        } else {
            nameNode.setPhpNode(makeReferenceVariableByName(nameNode.getName(), node.getLineno()));
        }
    }
}
