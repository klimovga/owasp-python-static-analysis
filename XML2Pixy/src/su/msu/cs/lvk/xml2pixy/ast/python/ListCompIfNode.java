package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 01.10.2008
 * Time: 21:59:20
 */
public class ListCompIfNode extends PythonNode {

    protected PythonNode test;

    protected ListCompIfNode() {
        super();
    }

    public ListCompIfNode(Element jdom) {
        super(jdom);

        test = makeNode(getFirst(jdom, "test"));
        test.setParent(this);
    }

    public ListCompIfNode(Node node) {
        super(node);

        test = makeNode(getFirst(node, "test"));
        test.setParent(this);
    }

    public void print(PrintStream out) {
        out.append("if ");
        test.print(out);
    }

    public PythonNode getTest() {
        return test;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(test).toList();
    }

}
