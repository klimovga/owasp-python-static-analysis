package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 23:09:57
 */
public class ClassNode extends ScopeNode {

    protected List<PythonNode> bases = new ArrayList<PythonNode>();
    protected StmtNode stmt;

    public ClassNode(Element jdom) {
        super(jdom);

        name = jdom.getAttributeValue("name");
        stmt = (StmtNode) makeNode((Element) jdom.getChild("code").getChildren().get(0));
        stmt.setParent(this);
        for (Object child : jdom.getChild("bases").getChildren()) {
            PythonNode base = makeNode((Element) child);
            base.setParent(this);
            bases.add(base);
        }
    }

    public ClassNode(Node node) {
        super(node);

        name = node.getJdomElement().getAttributeValue("name");
        stmt = (StmtNode) node.getChildren("code").get(0).getChildren().get(0).getAstNode();
        stmt.setParent(this);
        for (Node base : node.getChildren("bases").get(0).getChildren()) {
            PythonNode baseNode = (PythonNode) base.getAstNode();
            baseNode.setParent(this);
            bases.add(baseNode);
        }
    }

    public void print(PrintStream out) {
        out.append("class ").append(name);
        if (!bases.isEmpty()) {
            boolean first = true;
            out.append('(');
            for (PythonNode base : bases) {
                if (!first) out.append(", ");
                first = false;
                base.print(out);
            }
            out.append(')');
        }
        out.append(':');
        out.println();
        indent += indentStep;
        if (stmt != null) stmt.print(out);
        indent -= indentStep;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(stmt).add(bases).toList();
    }

    public List<PythonNode> getBases() {
        return bases;
    }

    public StmtNode getStmt() {
        return stmt;
    }

    
}
