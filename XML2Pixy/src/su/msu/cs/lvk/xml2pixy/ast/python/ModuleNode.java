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
 * Date: 22.09.2008
 * Time: 22:57:32
 */
public class ModuleNode extends ScopeNode {

    protected StmtNode stmt;

    public ModuleNode() {
        super();  
    }

    public ModuleNode(boolean init) {
        super(init);
    }

    public ModuleNode(Element jdom) {
        super(jdom);
        stmt = (StmtNode) makeNode((Element) jdom.getChild("node").getChildren().get(0));
        stmt.setParent(this);

        symbolTable.init();
    }

    public ModuleNode(Node node) {
        super(node);
        stmt = (StmtNode) node.getChildren("node").get(0).getChildren().get(0).getAstNode();
        stmt.setParent(this);

        symbolTable.init();
    }

    public void print(PrintStream out) {
        if (stmt != null) stmt.print(out);
    }

    public StmtNode getStmt() {
        return stmt;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(stmt).toList();
    }

}
