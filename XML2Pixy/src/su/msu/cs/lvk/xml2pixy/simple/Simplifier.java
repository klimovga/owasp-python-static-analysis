package su.msu.cs.lvk.xml2pixy.simple;

import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.simple.visitor.PythonNodeVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: KlimovGA
 * Date: 16.10.2008
 * Time: 9:34:24
 */
public class Simplifier extends AbstractTransformVisitor {

    public static final String PACKAGE = "su.msu.cs.lvk.xml2pixy.simple.visitor";

    public Simplifier() {
        super();
        visitors.put("AddNode", new PythonNodeVisitor());
        visitors.put("AndNode", new PythonNodeVisitor());
        visitors.put("IfNode", new PythonNodeVisitor());
    }

    public boolean visit(ASTNode node) throws VisitorException {
        PythonNodeVisitor visitor = getVisitor(node.getClass());
        if (visitor != null) visitor.visit((PythonNode) node);
        return true;
    }

    protected PythonNodeVisitor getVisitor(Class nodeClass) {
        String nodeName = Utils.afterLast(nodeClass.getName(), '.');
        if (visitors.containsKey(nodeName)) {
            return visitors.get(nodeName);
        } else {
            // try to find existing superclass visitor
            PythonNodeVisitor superVisitor = super.getVisitor(nodeClass);
            if (superVisitor != null) {
                return superVisitor;
            }

            PythonNodeVisitor visitor = null;
            Class parent = nodeClass;

            // Go through class hierarchy to find suitable visitor
            while (visitor == null) {
                try {
                    String className = Utils.afterLast(parent.getName(), ".");
                    visitor = (PythonNodeVisitor) Class.forName(PACKAGE + "." +
                            className + "Visitor").getConstructor().newInstance();
                } catch (Throwable e) {
                    parent = parent.getSuperclass();
                }
            }

            visitors.put(nodeName, visitor);
            return visitor;
        }
    }


}
