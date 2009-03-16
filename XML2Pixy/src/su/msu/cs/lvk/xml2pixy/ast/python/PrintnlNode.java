package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 23:38:38
 */
public class PrintnlNode extends PythonNode {

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();

    public PrintnlNode(Element jdom) {
        super(jdom);

        Element nodesElem = (Element) jdom.getChildren("nodes").get(0);
        for (Object child : nodesElem.getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public PrintnlNode(Node node) {
        super(node);
        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public void print(PrintStream out) {
        boolean first = true;
        out.print("print ");
        for (PythonNode child : nodes) {
            if (!first) out.print(", ");
            first = false;
            child.print(out);
        }
    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public List<ASTNode> getChildren() {
        return new ArrayList<ASTNode>(nodes);
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int i = nodes.indexOf(what);
        if (i >= 0 && with != null) {
            with.setParent(this);
            nodes.set(i, with);
            return true;
        }
        return false;
    }


}
