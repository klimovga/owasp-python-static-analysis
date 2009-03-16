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
 * Date: 23.09.2008
 * Time: 23:27:48
 */
public class CompareNode extends PythonNode {

    protected PythonNode expr;
    protected List<String> ops = new ArrayList<String>();
    protected List<PythonNode> exprs = new ArrayList<PythonNode>();

    public CompareNode(PythonNode o1, String op, PythonNode o2) {
        setAsParent(o1, o2);
        this.expr = o1;
        ops.clear();
        exprs.clear();
        if (o2 != null) {
            ops.add(op);
            exprs.add(o2);
        }
    }

    public CompareNode(Element jdom) {
        super(jdom);

        expr = makeNode((Element) jdom.getChild("expr").getChildren().get(0));
        expr.setParent(this);

        Element ops = jdom.getChild("ops");
        for (int i = 0; i < ops.getChildren().size(); i++) {
            this.ops.add(ops.getContent(i * 2).getValue().trim());
            PythonNode newExpr = makeNode((Element) ops.getChildren().get(i));
            newExpr.setParent(this);
            this.exprs.add(newExpr);
        }
    }

    public CompareNode(Node node) {
        super(node);

        expr = (PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode();
        Node ops = node.getChildren("ops").get(0);
        List<Node> children = ops.getChildren();
        for (int i = 0; i < children.size(); i++) {
            this.ops.add(ops.getJdomElement().getContent(i * 2).getValue().trim());
            PythonNode newExpr = (PythonNode) children.get(i).getAstNode();
            newExpr.setParent(this);
            exprs.add(newExpr);

        }
        expr.setParent(this);
    }

    public void print(PrintStream out) {
        if (expr != null) expr.print(out);
        for (int i = 0; i < ops.size() && i < exprs.size(); i++) {
            out.append(' ').append(ops.get(i)).append(' ');
            exprs.get(i).print(out);
        }
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).add(exprs).toList();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (expr == what) {
            expr = with;
            setAsParent(with);
            return true;
        } else if (exprs.contains(what)) {
            exprs.set(exprs.indexOf(what), with);
            setAsParent(with);
            return true;
        }
        return false;
    }

    public PythonNode getExpr() {
        return expr;
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
    }

    public List<String> getOps() {
        return ops;
    }

    public void setOps(List<String> ops) {
        this.ops = ops;
    }

    public List<PythonNode> getExprs() {
        return exprs;
    }

    public void setExprs(List<PythonNode> exprs) {
        this.exprs = exprs;
    }
}
