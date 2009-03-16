package su.msu.cs.lvk.xml2pixy.simple.visitor;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;

/**
 * User: klimov
 * Date: 13.01.2009
 */
public class GetattrNodeVisitor extends PythonNodeVisitor {

    private static final Logger logger = Logger.getLogger(GetattrNodeVisitor.class);

    public void visit(PythonNode node) {
        GetattrNode getattr = (GetattrNode) node;
        PythonNode expr = getattr.getExpr();
        PythonNode parent = getattr.getParent();
        boolean parentGetattr = parent instanceof GetattrNode;
        boolean parentCallFunc = parent instanceof CallFuncNode;
        if (expr instanceof NameNode) {
            NameNode nameNode = (NameNode) expr;
            String symbolName = nameNode.getName() + "." + getattr.getAttrName();
            Symbol symbol = ProcessingUtils.trySymbol(getattr.getScope(), symbolName);
            if (symbol != null) {
                parent.replace(getattr, new NameNode(parentGetattr || parentCallFunc 
                        ? symbolName
                        : symbolName).copyLocation(getattr));
            } else {
                replaceWithSubscript(getattr);
            }
        } else {
            replaceWithSubscript(getattr);
        }

    }

    protected void replaceWithSubscript(GetattrNode getattr) {
        PythonNode parent = getattr.getParent();
        PythonNode expr = getattr.getExpr();
        if (!(parent instanceof CallFuncNode)) {
            parent.replace(getattr, new SubscriptNode(expr, new ConstNode(getattr.getAttrName())));
/*
            if (expr instanceof NameNode) {
                NameNode nameNode = (NameNode) expr;
                nameNode.setName(ProcessingUtils.getFullName(nameNode));
            }
*/
        }
    }

}
