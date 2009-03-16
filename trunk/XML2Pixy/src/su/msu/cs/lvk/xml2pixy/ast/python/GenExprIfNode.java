package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GenExprIfNode extends PythonNode {

    protected PythonNode test;

    protected GenExprIfNode() {
        super();
    }

    public GenExprIfNode(Element jdom) {
        super(jdom);

        if (jdom != null) {
            test = makeNode(getFirst(jdom, "test"));
            setAsParent(test);
        }
    }

    public GenExprIfNode(Node node) {
        super(node);

        if (node != null) {
            test = makeNode(getFirst(node, "test"));
            setAsParent(test);
        }
    }

    public void print(PrintStream out) {
        out.append("if ");
        test.print(out);
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (test == what) {
            test = with;
            setAsParent(test);
            return true;
        }
        return false;
    }

    public List<ASTNode> getChildren() {
        return Arrays.asList((ASTNode) test);
    }

    public PythonNode getTest() {
        return test;
    }

    public void setTest(PythonNode test) {
        this.test = test;
    }
}
