package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:01:19
 */
public class ListNode extends PythonNode {

    public static final String APPEND_METHOD = "append";

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();

    public ListNode(PythonNode ... nodes) {
        super();

        this.nodes.addAll(Arrays.asList(nodes));
        setAsParent(this.nodes);
    }

    public ListNode(Element jdom) {
        super(jdom);

        for (Object child : jdom.getChild("nodes").getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public ListNode(Node node) {
        super(node);

        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            nodes.add(newNode);
        }

    }

    public void print(PrintStream out) {
        boolean first = true;
        out.append('[');
        for (PythonNode child : nodes) {
            if (!first) out.append(", ");
            first = false;
            child.print(out);
        }
        out.append(']');
    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public List<ASTNode> getChildren() {
        return new ArrayList<ASTNode>(nodes);
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int i = nodes.indexOf(what);
        if (i >= 0) {
            nodes.set(i, with);
        } else {
            return false;
        }
        setAsParent(with);
        return true;
    }
}
