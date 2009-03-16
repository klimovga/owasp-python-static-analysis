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
 * Date: 24.09.2008
 * Time: 23:32:11
 */
public class ReturnNode extends PythonNode {

    protected PythonNode value;

    protected ReturnNode() {
        super();
    }

    public ReturnNode(PythonNode value) {
        this.value = value;
        setAsParent(this.value);
        copyLocation(value);
    }

    public ReturnNode(Element jdom) {
        super(jdom);

        value = makeNode((Element) jdom.getChild("value").getChildren().get(0));
        if (value.getClass().equals(ConstNode.class)) {
            if (((ConstNode) value).getValue() == null) {
                value = null;
            }
        }
        if (value != null) value.setParent(this);
    }

    public ReturnNode(Node node) {
        super(node);

        value = (PythonNode) node.getChildren("value").get(0).getChildren().get(0).getAstNode();
        if (value.getClass().equals(ConstNode.class)) {
            if (((ConstNode) value).getValue() == null) {
                value = null;
            }
        }
        if (value != null) value.setParent(this);
    }

    public void print(PrintStream out) {
        out.append("return ");
        if (value != null) value.print(out);
    }

    public PythonNode getValue() {
        return value;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(value).toList();
    }

    public void setValue(PythonNode value) {
        this.value = value;
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (value == what) {
            if (with != null) {
                with.setParent(this);
            }
            value = with;
            return true;
        }

        return false;
    }
}
