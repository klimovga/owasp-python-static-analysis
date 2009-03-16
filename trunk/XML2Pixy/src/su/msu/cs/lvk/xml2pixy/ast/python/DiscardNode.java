package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:09:19
 */
public class DiscardNode extends PythonNode {

    protected PythonNode expr;

    public DiscardNode(Element jdom) {
        super(jdom);

        expr = makeNode((Element) jdom.getChild("expr").getChildren().get(0));
        expr.setParent(this);
    }

    public DiscardNode(Node node) {
        super(node);

        expr = (PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode();
        expr.setParent(this);
    }

    public void print(PrintStream out) {
        if (expr != null) expr.print(out);
    }

    public PythonNode getExpr() {
        return expr;
    }

    public List<ASTNode> getChildren() {
        return Arrays.asList((ASTNode) expr);
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (expr == what) {
            expr = with;
            setAsParent(expr);
            return true;
        }
        return false; 
    }
}
