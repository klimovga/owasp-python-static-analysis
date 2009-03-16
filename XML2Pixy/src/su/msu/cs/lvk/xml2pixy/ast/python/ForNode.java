package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 25.09.2008
 * Time: 23:00:39
 */
public class ForNode extends PythonNode {

    protected PythonNode assign;
    protected StmtNode body;
    protected StmtNode elze;
    protected PythonNode list;

    public ForNode() {
        super();
    }

    public ForNode(Element jdom) {
        super(jdom);

        assign = makeNode(getFirst(jdom, "assign"));
        body = (StmtNode) makeNode(getFirst(jdom, "body"));
        elze = (StmtNode) makeNode(getFirst(jdom, "else_"));
        list = makeNode(getFirst(jdom, "list"));

        setAsParent(assign, body, elze, list);
    }

    public ForNode(Node node) {
        super(node);

        assign = makeNode(getFirst(node, "assign"));
        body = (StmtNode) makeNode(getFirst(node, "body"));
        elze = (StmtNode) makeNode(getFirst(node, "else_"));
        list = makeNode(getFirst(node, "list"));

        setAsParent(assign, body, elze, list);
    }

    public void print(PrintStream out) {
        out.append("for ");
        if (assign != null) assign.print(out);
        out.append(" in ");
        if (list != null) list.print(out);
        out.println(":");
        indent += indentStep;
        if (body != null) body.print(out);
        indent -= indentStep;
        if (elze != null) {
            out.println("else:");
            elze.print(out);
        }
    }

    public PythonNode getAssign() {
        return assign;
    }

    public StmtNode getBody() {
        return body;
    }

    public StmtNode getElse() {
        return elze;
    }

    public PythonNode getList() {
        return list;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(assign).add(body).add(elze).add(list).toList();
    }

    public void setAssign(PythonNode assign) {
        this.assign = assign;
        setAsParent(this.assign);
    }

    public void setBody(StmtNode body) {
        this.body = body;
        setAsParent(this.body);
    }

    public void setElze(StmtNode elze) {
        this.elze = elze;
        setAsParent(this.elze);
    }

    public void setList(PythonNode list) {
        this.list = list;
        setAsParent(this.list);
    }
}
