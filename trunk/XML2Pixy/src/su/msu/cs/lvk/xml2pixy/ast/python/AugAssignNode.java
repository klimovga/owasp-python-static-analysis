package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:43:22
 */
public class AugAssignNode extends PythonNode {

    protected PythonNode node;
    protected PythonNode expr;
    protected String op;

    public AugAssignNode(Element jdom) {
        super(jdom);

        this.node = makeNode((Element) jdom.getChild("node").getChildren().get(0));
        this.expr = makeNode((Element) jdom.getChild("expr").getChildren().get(0));
        this.op = jdom.getAttributeValue("op");
        this.node.setParent(this);
        this.expr.setParent(this);
    }

    public AugAssignNode(Node node) {
        super(node);

        this.node = (PythonNode) node.getChildren("node").get(0).getChildren().get(0).getAstNode();
        this.expr = (PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode();
        this.op = node.getJdomElement().getAttributeValue("op");
        this.node.setParent(this);
        this.expr.setParent(this);
    }

    public void print(PrintStream out) {
        if (node != null) node.print(out);
        out.append(' ').append(op).append(' ');
        if (expr != null) expr.print(out);
    }

    public PythonNode getNode() {
        return node;
    }

    public PythonNode getExpr() {
        return expr;
    }

    public String getOp() {
        return op;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(node).add(expr).toList();
    }
}
