package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 21:53:47
 */

/**
 * Also used as basic n-ary operation
 */
public class AndNode extends PythonNode {

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();

    public AndNode(List<PythonNode> nodes) {
        this.nodes = nodes;
        for (PythonNode node : nodes) {
            if (node != null) node.setParent(this);
        }
    }

    public AndNode() {
        super();
    }

    public AndNode(Element jdom) {
        super(jdom);

        for (Object child : jdom.getChild("nodes").getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public AndNode(Node node) {
        super(node);

        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public void print(PrintStream out) {
        out.append('(');
        boolean first = true;
        for (PythonNode child : nodes) {
            if (!first) out.print(" and ");
            first = false;
            child.print(out);
        }
        out.append(')');
    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<PythonNode> nodes) {
        this.nodes = nodes;

        for (PythonNode node : nodes) {
            node.setParent(this);
        }
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(nodes).toList();
    }

    public void addNode(PythonNode node) {
        if (node != null) {
            node.setParent(this);
            nodes.add(node);
        }
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (with != null) {
            int index = nodes.indexOf(what);
            if (index >= 0) {
                nodes.set(index, with);
                with.setParent(this);
                return true;
            }
        }
        return false;
    }
}
