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
 * Date: 22.09.2008
 * Time: 22:51:43
 */
public class AssignNode extends PythonNode {

    protected List<PythonNode> nodes = new ArrayList<PythonNode>();
    protected PythonNode expr;

    public AssignNode(PythonNode node, PythonNode expr) {
        if (node != null) {
            nodes.add(node);
            node.setParent(this);
        }

        setExpr(expr);
    }

    public AssignNode(List<PythonNode> nodes, PythonNode expr) {
        if (nodes != null) {
            this.nodes.addAll(nodes);
            for (PythonNode node : nodes) node.setParent(this);
        }
        setExpr(expr);
    }

    public AssignNode(Element jdom) {
        super(jdom);

        Element nodesElem = (Element) jdom.getChildren("nodes").get(0);
        for (Object child : nodesElem.getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            nodes.add(newNode);
        }
        Element exprNode = (Element) jdom.getChildren("expr").get(0);
        setExpr(makeNode((Element) exprNode.getChildren().get(0)));

    }

    public AssignNode(Node node) {
        super(node);

        for (Node child : node.getChildren("nodes").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            nodes.add(newNode);
        }

        setExpr((PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode());
    }

    public void print(PrintStream out) {
        for (PythonNode child : nodes) {
            child.print(out);
            out.print(" = ");
        }
        if (expr != null) expr.print(out);
    }

    public List<PythonNode> getNodes() {
        return nodes;
    }

    public PythonNode getExpr() {
        return expr;
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
        if (this.expr != null) this.expr.setParent(this);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).add(nodes).toList();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (with != null) {
            if (expr == what) {
                expr = with;
                with.setParent(this);
                return true;
            } else {
                int index = nodes.indexOf(what);
                if (index >= 0) {
                    nodes.set(index, with);
                    with.setParent(this);
                }
                return true;
            }

        }
        return false;
    }
}
