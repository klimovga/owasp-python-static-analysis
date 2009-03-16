package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GenExprNode extends PythonNode {

    protected GenExprInnerNode code;

    protected GenExprNode() {
        super();
    }

    public GenExprNode(Element jdom) {
        super(jdom);

        code = (GenExprInnerNode) makeNode(getFirst(jdom, "code"));
        setAsParent(code);
    }

    public GenExprNode(Node node) {
        super(node);

        code = (GenExprInnerNode) makeNode(getFirst(node, "code"));
        setAsParent(code);
    }

    public void print(PrintStream out) {
        out.append("(");
        code.print(out);
        out.append(")");
    }

    public boolean replace(PythonNode what, PythonNode with) {
        if (code == what) {
            code = (GenExprInnerNode) with;
            setAsParent(code);
            return true;
        }
        return false;
    }

    public List<ASTNode> getChildren() {
        return Arrays.asList((ASTNode) code);
    }

    public GenExprInnerNode getCode() {
        return code;
    }

    public void setCode(GenExprInnerNode code) {
        this.code = code;
    }
}
