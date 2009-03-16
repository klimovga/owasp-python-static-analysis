package su.msu.cs.lvk.xml2pixy.simple;

import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.Visitor;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.simple.visitor.PythonNodeVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.HashMap;
import java.util.Map;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public abstract class AbstractTransformVisitor implements Visitor {

    protected Map<String, PythonNodeVisitor> visitors;

    public AbstractTransformVisitor() {
        visitors = new HashMap<String, PythonNodeVisitor>();
    }

    public boolean visit(ASTNode node) throws VisitorException {
        PythonNodeVisitor visitor = getVisitor(node.getClass());
        if (visitor != null) visitor.visit((PythonNode) node);
        return true;
    }

    protected PythonNodeVisitor getVisitor(Class clazz) {
        return getVisitor0(clazz);
    }

    private PythonNodeVisitor getVisitor0(Class clazz) {
        String nodeName = Utils.afterLast(clazz.getName(), '.');
        if (visitors.containsKey(nodeName)) {
            return visitors.get(nodeName);
        } else {
            Class superClass = clazz.getSuperclass();
            return superClass == null ? null : getVisitor0(superClass);
        }
    }
}
