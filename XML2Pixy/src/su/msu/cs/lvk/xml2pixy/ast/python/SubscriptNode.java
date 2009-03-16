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
 * Time: 22:42:09
 */
public class SubscriptNode extends PythonNode {

    protected PythonNode expr;
    protected List<PythonNode> subs = new ArrayList<PythonNode>();

    protected SubscriptNode() {
        super();
    }

    public SubscriptNode(PythonNode expr, PythonNode ... subs) {
        this.expr = expr;
        if (expr != null) expr.setParent(this);

        for (PythonNode sub : subs) {
            if (sub != null) sub.setParent(this);
            this.subs.add(sub);
        }
    }

    public SubscriptNode(Element jdom) {
        super(jdom);

        expr = makeNode(getFirst(jdom, "expr"));
        expr.setParent(this);

        for (Object child : jdom.getChild("subs").getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            subs.add(newNode);
        }
    }

    public SubscriptNode(Node node) {
        super(node);

        expr = makeNode(getFirst(node, "expr"));
        expr.setParent(this);

        for (Node child : node.getChildren("subs").get(0).getChildren()) {
            PythonNode newNode = makeNode(child);
            newNode.setParent(this);
            subs.add(newNode);
        }
    }

    public void print(PrintStream out) {
        expr.print(out);
        out.append('[');
        boolean first = true;
        for (PythonNode sub : subs) {
            if (!first) out.append(", ");
            first = false;
            sub.print(out);
        }
        out.append(']');
    }

    public PythonNode getExpr() {
        return expr;
    }

    public List<PythonNode> getSubs() {
        return subs;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).add(subs).toList();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int index;
        if (what == expr) {
            expr = with;
            setAsParent(expr);
        } else if ((index = subs.indexOf(what)) >= 0) {
            subs.set(index, with);
            setAsParent(with);
        } else {
            return false;
        }

        return true;
    }
}
