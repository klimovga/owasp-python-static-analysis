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
 * Time: 23:20:09
 */

/**
 * Also used as basic unary operation
 */
public class InvertNode extends PythonNode {

    protected PythonNode expr;

    protected InvertNode() {
        super();
    }

    public InvertNode(PythonNode expr) {
        this.expr = expr;
        if (expr != null) expr.setParent(this);
    }

    public InvertNode(Element jdom) {
        super(jdom);

        expr = makeNode((Element) jdom.getChild("expr").getChildren().get(0));
        expr.setParent(this);
    }

    public InvertNode(Node node) {
        super(node);

        expr = (PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode();
        expr.setParent(this);
    }

    public void print(PrintStream out) {
        out.append('~');
        if (expr != null) expr.print(out);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).toList();
    }

    public PythonNode getExpr() {
        return expr;
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
        if (this.expr != null) this.expr.setParent(this);
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (expr == what) setExpr(with);

        return with.getParent() == this;
    }
}
