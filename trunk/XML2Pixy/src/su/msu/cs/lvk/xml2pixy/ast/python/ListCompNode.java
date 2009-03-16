package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 01.10.2008
 * Time: 22:14:06
 */
public class ListCompNode extends PythonNode {

    protected PythonNode expr;
    protected List<ListCompForNode> quals = new ArrayList<ListCompForNode>();

    protected ListCompNode() {
        super();
    }

    public ListCompNode(Element jdom) {
        super(jdom);

        expr = makeNode(getFirst(jdom, "expr"));
        expr.setParent(this);

        for (Object child : jdom.getChild("quals").getChildren()) {
            ListCompForNode newNode = (ListCompForNode) makeNode((Element) child);
            newNode.setParent(this);
            quals.add(newNode);
        }

    }

    public ListCompNode(Node node) {
        super(node);

        expr = makeNode(getFirst(node, "expr"));
        expr.setParent(this);

        for (Node child : node.getChildren("quals").get(0).getChildren()) {
            ListCompForNode newNode = (ListCompForNode) makeNode(child);
            newNode.setParent(this);
            quals.add(newNode);
        }
    }

    public void print(PrintStream out) {
        out.append('[');
        expr.print(out);
        for (ListCompForNode listCompFor : quals) {
            out.append(' ');
            listCompFor.print(out);
        }
        out.append(']');
    }

    public PythonNode getExpr() {
        return expr;
    }

    public List<ListCompForNode> getQuals() {
        return quals;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).add(quals).toList();
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
    }

    public void setQuals(List<ListCompForNode> quals) {
        this.quals = quals;
    }
}
