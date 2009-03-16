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
 * Date: 22.09.2008
 * Time: 22:57:18
 */
public class StmtNode extends PythonNode {

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();

    public StmtNode() {
        super();
    }

    public StmtNode(PythonNode ... statements) {
        nodes = new ArrayList<PythonNode>(Arrays.asList(statements));
        setAsParent(nodes);
    }

    public StmtNode(Element jdom) {
        super(jdom);
        Element nodesElem = (Element) jdom.getChildren("nodes").get(0);
        for (Object child : nodesElem.getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public StmtNode(Node node) {
        super(node);
        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            nodes.add(newNode);
        }
    }

    public void print(PrintStream out) {
        boolean first = true;
        for (PythonNode statement : nodes) {
            // print new line and do indent
            if (!first) out.println();
            first = false;
            for (int i = 0; i < indent; i++) {
                out.print(' ');
            }
            // print statement
            statement.print(out);
        }
    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public List<ASTNode> getChildren() {
        return new ArrayList<ASTNode>(nodes);
    }

    public void setNode(int index, PythonNode node) {
        if (node != null) {
            node.setParent(this);
            nodes.set(index, node);
        }
    }

    public void addNode(int index, PythonNode node) {
        if (node != null) {
            node.setParent(this);
            if (index >= nodes.size()) {
                nodes.add(node);
            } else {
                nodes.add(index, node);
            }
        }
    }

    public void addNode(PythonNode node) {
        if (node != null) {
            node.setParent(this);
            nodes.add(node);
        }
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int index = nodes.indexOf(what);
        if (index >= 0) {
            nodes.set(index, with);
            setAsParent(with);
            return true;
        }
        return false;
    }
}
