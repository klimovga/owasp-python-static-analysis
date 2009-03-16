package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 01.10.2008
 * Time: 23:11:20
 */
public class TryExceptNode extends PythonNode {

    protected StmtNode body;
    protected List<StmtNode> handlers = new ArrayList<StmtNode>();
    protected StmtNode elze;

    protected TryExceptNode() {
        super();
    }

    public TryExceptNode(Element jdom) {
        super(jdom);

        body = (StmtNode) makeNode(getFirst(jdom, "body"));

        elze = (StmtNode) makeNode(getFirst(jdom, "else_"));

        for (Object obj : jdom.getChild("handlers").getChildren()) {
            Element elem = (Element) obj;
            if (elem.getName().equals("Stmt")) {
                StmtNode stmt = (StmtNode) makeNode(elem);
                setAsParent(stmt);
                handlers.add(stmt);
            }
        }
        setAsParent(body, elze);
    }

    public TryExceptNode(Node node) {
        super(node);

        body = (StmtNode) makeNode(getFirst(node, "body"));
        setParent(this);

        elze = (StmtNode) makeNode(getFirst(node, "else_"));

        for (Node child : getFirst(node, "handlers").getChildren()) {
            if (child.getJdomElement().getName().equals("Stmt")) {
                StmtNode stmt = (StmtNode) makeNode(child);
                setAsParent(stmt);
                handlers.add(stmt);
            }
        }
        setAsParent(body, elze);
    }

    public void print(PrintStream out) {
        out.println("try:");
        printIndented(body, out);
        for (StmtNode stmt : handlers) {
            out.println();
            for (int i = 0; i < indent; i++) {
                out.print(' ');
            }
            out.append("except ").append(String.valueOf(handlers.indexOf(stmt)));
            out.println(":");
            printIndented(stmt, out);
        }

        if (elze != null) {
            out.println();
            for (int i = 0; i < indent; i++) {
                out.print(' ');
            }
            out.println("else:");
            printIndented(elze, out);
        }
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(body).add(elze).toList();
    }

    public StmtNode getBody() {
        return body;
    }

    public void setBody(StmtNode body) {
        this.body = body;
        setAsParent(body);
    }

    public List<StmtNode> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<StmtNode> handlers) {
        this.handlers = handlers;
    }

    public StmtNode getElze() {
        return elze;
    }

    public void setElze(StmtNode elze) {
        this.elze = elze;
        setAsParent(elze);
    }
}
