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
 * Time: 23:06:14
 */
public class RaiseNode extends PythonNode {

    public static final String EXCEPTION_INFO_VAR = "__exception_info__";

    protected PythonNode expr1;
    protected PythonNode expr2;
    protected PythonNode expr3;

    protected RaiseNode() {
        super();
    }

    public RaiseNode(Element jdom) {
        super(jdom);

        expr1 = makeNode(getFirst(jdom, "expr1"));
        if (expr1 != null) expr1.setParent(this);

        expr2 = makeNode(getFirst(jdom, "expr2"));
        if (expr2 != null) expr2.setParent(this);

        expr3 = makeNode(getFirst(jdom, "expr3"));
        if (expr3 != null) expr3.setParent(this);
    }

    public RaiseNode(Node node) {
        super(node);

        expr1 = makeNode(getFirst(node, "expr1"));
        if (expr1 != null) expr1.setParent(this);

        expr2 = makeNode(getFirst(node, "expr2"));
        if (expr2 != null) expr2.setParent(this);

        expr3 = makeNode(getFirst(node, "expr3"));
        if (expr3 != null) expr3.setParent(this);
    }

    public void print(PrintStream out) {
        out.append("raise");
        if (expr1 != null) {
            out.append(" ");
            expr1.print(out);
        }
        if (expr2 != null) {
            out.append(", ");
            expr2.print(out);
        }
        if (expr3 != null) {
            out.append(", ");
            expr3.print(out);
        }
    }

    public PythonNode getExpr1() {
        return expr1;
    }

    public PythonNode getExpr2() {
        return expr2;
    }

    public PythonNode getExpr3() {
        return expr3;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(expr1).add(expr2).add(expr3).toList();
    }

}
