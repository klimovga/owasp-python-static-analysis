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
 * Time: 23:51:55
 */
public class GetattrNode extends PythonNode {

    protected PythonNode expr;
    protected String attrName;

    public GetattrNode(PythonNode expr, String attrName) {
        this.expr = expr;
        this.attrName = attrName;
        setAsParent(this.expr);
    }

    public GetattrNode(Element jdom) {
        super(jdom);

        expr = makeNode((Element) jdom.getChild("expr").getChildren().get(0));
        expr.setParent(this);

        attrName = jdom.getAttributeValue("attrname");
    }

    public GetattrNode(Node node) {
        super(node);

        expr = (PythonNode) node.getChildren("expr").get(0).getChildren().get(0).getAstNode();
        expr.setParent(this);

        attrName = node.getJdomElement().getAttributeValue("attrname");
    }

    public void print(PrintStream out) {
        if (expr != null) expr.print(out);
        out.append('.').append(attrName);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr).toList();
    }

    public PythonNode getExpr() {
        return expr;
    }

    public void setExpr(PythonNode expr) {
        this.expr = expr;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (expr == what) {
            expr = with;
            setAsParent(with);
            return true;
        }
        return false;
    }
}
