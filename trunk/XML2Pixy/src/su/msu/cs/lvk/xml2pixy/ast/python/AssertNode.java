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
 * Time: 22:30:05
 */
public class AssertNode extends PythonNode {

    protected PythonNode test;
    protected PythonNode fail;

    public AssertNode(Element jdom) {
        super(jdom);

        test = makeNode((Element) jdom.getChild("test").getChildren().get(0));
        test.setParent(this);
        Element fail = jdom.getChild("fail");
        if (!fail.getChildren().isEmpty()) {
            this.fail = makeNode((Element) fail.getChildren().get(0));
            this.fail.setParent(this);
        }
    }

    public AssertNode(Node node) {
        super(node);

        test = (PythonNode) node.getChildren("test").get(0).getChildren().get(0).getAstNode();
        test.setParent(this);
        List<Node> fail = node.getChildren("fail").get(0).getChildren();
        if (!fail.isEmpty()) {
            this.fail = (PythonNode) fail.get(0).getAstNode();
            this.fail.setParent(this);
        }
    }

    public void print(PrintStream out) {
        out.print("assert ");
        if (test != null) test.print(out);
        if (fail != null) {
            out.print(", ");
            fail.print(out);
        }
    }

    public PythonNode getTest() {
        return test;
    }

    public PythonNode getFail() {
        return fail;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(test).add(fail).toList();
    }
}
