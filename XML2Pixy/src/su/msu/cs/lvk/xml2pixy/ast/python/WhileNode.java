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
 * Time: 20:15:36
 */
public class WhileNode extends PythonNode {

    protected PythonNode test;
    protected StmtNode body;
    protected StmtNode elze;

    public WhileNode(Element jdom) {
        super(jdom);

        elze = (StmtNode) makeNode(getFirst(jdom, "else_"));
        if (elze != null) elze.setParent(this);

        body = (StmtNode) makeNode(getFirst(jdom, "body"));
        if (body != null) body.setParent(this);

        test =  makeNode(getFirst(jdom, "test"));
        if (test != null) test.setParent(this);
    }

    public WhileNode(Node node) {
        super(node);

        elze = (StmtNode) makeNode(getFirst(node, "else_"));
        if (elze != null) elze.setParent(this);

        body = (StmtNode) makeNode(getFirst(node, "body"));
        if (body != null) body.setParent(this);

        test = makeNode(getFirst(node, "test"));
        if (test != null) test.setParent(this);
    }

    public void print(PrintStream out) {
        out.append("while ");
        test.print(out);
        out.println(":");
        printIndented(body, out);
        if (elze != null) {
            out.println();
            out.println("else:");
            printIndented(elze, out);
        }
    }

    public PythonNode getTest() {
        return test;
    }

    public StmtNode getBody() {
        return body;
    }

    public StmtNode getElse() {
        return elze;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(test).add(body).add(elze).toList();
    }

}
