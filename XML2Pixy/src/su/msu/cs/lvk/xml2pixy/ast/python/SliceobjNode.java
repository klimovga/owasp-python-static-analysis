package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 01.10.2008
 * Time: 22:54:09
 */
public class SliceobjNode extends PythonNode {

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();

    protected SliceobjNode() {
        super();
    }

    public SliceobjNode(Element jdom) {
        super(jdom);

        for (Object child : jdom.getChild("nodes").getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public SliceobjNode(Node node) {
        super(node);

        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = makeNode(child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public void print(PrintStream out) {
        boolean first = true;
        for (PythonNode node : nodes) {
            if (!first) out.append(':');
            first = false;
            node.print(out);
        }

    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public List<ASTNode> getChildren() {
        return new ArrayList<ASTNode>(nodes);
    }

}
