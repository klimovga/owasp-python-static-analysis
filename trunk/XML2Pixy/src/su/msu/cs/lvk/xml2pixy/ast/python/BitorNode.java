package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * User: gklimov
 * Date: 23.09.2008 22:51:04
 */
public class BitorNode extends AndNode {
    public BitorNode() {
        super();
    }

    public BitorNode(List<PythonNode> nodes) {
        super(nodes);
    }

    public BitorNode(Element jdom) {
        super(jdom);
    }

    public BitorNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append('(');
        boolean first = true;
        for (PythonNode child : nodes) {
            if (!first) out.print(" | ");
            first = false;
            child.print(out);
        }
        out.append(')');
    }
}
