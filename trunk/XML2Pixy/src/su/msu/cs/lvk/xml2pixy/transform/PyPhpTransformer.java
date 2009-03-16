package su.msu.cs.lvk.xml2pixy.transform;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.Visitor;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;
import su.msu.cs.lvk.xml2pixy.transform.pyvisitor.PythonNodeVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class PyPhpTransformer implements Visitor {
    protected static Logger log = Logger.getLogger(PyPhpTransformer.class);

    private static final String PACKAGE = "su.msu.cs.lvk.xml2pixy.transform.pyvisitor";

    private Map<String, PythonNodeVisitor> visitors;

    public PyPhpTransformer() {
        visitors = new HashMap<String, PythonNodeVisitor>();
    }

    public boolean visit(ASTNode node) throws VisitorException {
        PythonNodeVisitor visitor = getVisitor(node.getClass());
        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(node.getFileName());
        visitor.setHelper(helper);
        visitor.visit((PythonNode) node);
        return true;
    }

    private PythonNodeVisitor getVisitor(Class nodeClass) {
        String nodeName = Utils.afterLast(nodeClass.getName(), '.');
        if (visitors.containsKey(nodeName) && visitors.get(nodeName) != null) {
            return visitors.get(nodeName);
        } else {
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
