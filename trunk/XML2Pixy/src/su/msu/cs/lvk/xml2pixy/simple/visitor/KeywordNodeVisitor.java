package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.KeywordNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;

/**
 * @author gklimov
 * @created 15.03.2009 18:33:14
 */
public class KeywordNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        KeywordNode kw = (KeywordNode) node;

        kw.getParent().replace(kw, kw.getExpr());
    }
}
