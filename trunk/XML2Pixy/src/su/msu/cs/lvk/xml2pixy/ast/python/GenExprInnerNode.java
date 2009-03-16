package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GenExprInnerNode extends PythonNode {

    protected PythonNode expr;

    protected List<GenExprForNode> quals = new ArrayList<GenExprForNode>();

    protected GenExprInnerNode() {
        super();
    }

    public GenExprInnerNode(Element jdom) {
        super(jdom);

        expr = makeNode(getFirst(jdom, "expr"));
        setAsParent(expr);

        for (Object child : jdom.getChild("quals").getChildren()) {
            GenExprForNode newNode = (GenExprForNode) makeNode((Element) child);
            setAsParent(newNode);
            this.quals.add(newNode);
        }
    }

    public GenExprInnerNode(Node node) {
        super(node);

        expr = makeNode(getFirst(node, "expr"));
        setAsParent(expr);

        for (Node child : node.getChildren("quals").get(0).getChildren()) {
            GenExprForNode newNode = (GenExprForNode) makeNode(child);
            setAsParent(newNode);
            this.quals.add(newNode);
        }
    }

    public void print(PrintStream out) {
        expr.print(out);
        for (GenExprForNode geFor : quals) {
            out.append(" ");
            geFor.print(out);
        }
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int index;
        if (expr == what) {
            expr = with;
            setAsParent(expr);
        } else if ((index = quals.indexOf(what)) >= 0) {
            if (with != null) {
                quals.set(index, (GenExprForNode) with);
                setAsParent(with);
            } else {
                quals.remove(index);
            }
        } else {
            return false;
        }
        return true;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).add(quals).toList();
    }

    public PythonNode getExpr() {
        return expr;
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
    }

    public List<GenExprForNode> getQuals() {
        return quals;
    }

    public void setQuals(List<GenExprForNode> quals) {
        this.quals = quals;
    }
}
