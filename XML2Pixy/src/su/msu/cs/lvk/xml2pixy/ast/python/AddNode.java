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
 * Date: 22.09.2008
 * Time: 22:24:18
 */

/**
 * Also used as basic binary operation
 */
public class AddNode extends PythonNode {

    protected PythonNode left;
    protected PythonNode right;

    public AddNode() {
        super();
    }

    public AddNode(Element jdom) {
        super(jdom);

        setLeft(makeNode((Element)jdom.getChild("left").getChildren().get(0)));
        setRight(makeNode((Element)jdom.getChild("right").getChildren().get(0)));
    }

    public AddNode(Node node) {
        super(node);

        setLeft((PythonNode)node.getChildren("left").get(0).getChildren().get(0).getAstNode());
        setRight((PythonNode)node.getChildren("right").get(0).getChildren().get(0).getAstNode());
    }

    public void print(PrintStream out) {
        out.append('(');
        if (left != null) left.print(out);
        out.append(" + ");
        if (right != null) right.print(out);
        out.append(')');
    }

    public PythonNode getLeft() {
        return left;
    }

    public PythonNode getRight() {
        return right;
    }

    public void setLeft(PythonNode left) {
        this.left = left;
        setAsParent(left);
    }

    public void setRight(PythonNode right) {
        this.right = right;
        setAsParent(right);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(left).add(right).toList();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (what == left) setLeft(with);
        else if (what == right) setRight(with);

        return with.getParent() == this;
    }
}
