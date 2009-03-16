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
 * Time: 22:23:19
 */
public class TryFinallyNode extends PythonNode {

    protected StmtNode body;
    protected StmtNode finall;

    protected TryFinallyNode() {
        super();
    }

    public TryFinallyNode(Element jdom) {
        super(jdom);

        body = (StmtNode) makeNode(getFirst(jdom, "body"));
        body.setParent(this);

        finall = (StmtNode) makeNode(getFirst(jdom, "final"));
        finall.setParent(this);
    }

    public TryFinallyNode(Node node) {
        super(node);

        body = (StmtNode) makeNode(getFirst(node, "body"));
        body.setParent(this);

        finall = (StmtNode) makeNode(getFirst(node, "final"));
        finall.setParent(this);
    }

    public void print(PrintStream out) {
        out.println("try:");
        printIndented(body, out);
        out.println();
        out.println("finally:");
        printIndented(finall, out);
    }

    public StmtNode getBody() {
        return body;
    }

    public StmtNode getFinal() {
        return finall;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(body).add(finall).toList();
    }

}
