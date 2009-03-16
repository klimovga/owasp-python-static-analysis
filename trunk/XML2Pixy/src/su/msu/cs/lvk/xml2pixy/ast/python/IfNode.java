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
 * Date: 29.09.2008
 * Time: 20:26:23
 */
public class IfNode extends PythonNode {

    protected StmtNode elze;
    protected List<PythonNode> tests = new ArrayList<PythonNode>();
    protected List<StmtNode> stmts = new ArrayList<StmtNode>();

    public IfNode(List<PythonNode> tests, List<StmtNode> stmts, StmtNode elze) {
        setTests(tests);
        setStmts(stmts);
        setElse(elze);
    }

    public IfNode(Element jdom) {
        super(jdom);

        elze = (StmtNode) makeNode(getFirst(jdom, "else_"));
        if (elze != null) elze.setParent(this);

        List children = jdom.getChild("tests").getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (i % 2 == 0) {
                PythonNode test = makeNode((Element) children.get(i));
                test.setParent(this);
                tests.add(test);
            } else {
                StmtNode stmt = (StmtNode) makeNode((Element) children.get(i));
                stmt.setParent(this);
                stmts.add(stmt);
            }
        }
    }

    public IfNode(Node node) {
        super(node);

        elze = (StmtNode) makeNode(getFirst(node, "else_"));
        if (elze != null) elze.setParent(this);

        List<Node> children = node.getChildren("tests").get(0).getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (i % 2 == 0) {
                PythonNode test = makeNode(children.get(i));
                test.setParent(this);
                tests.add(test);
            } else {
                StmtNode stmt = (StmtNode) makeNode(children.get(i));
                stmt.setParent(this);
                stmts.add(stmt);
            }
        }
    }

    public void print(PrintStream out) {
        printIf(0, out);
        for (int i = 1; i < tests.size(); i++) {
            out.println();
            out.append("el");
            printIf(i, out);
        }
        if (elze != null) {
            out.println();
            out.println("else:");
            printIndented(elze, out);
        }
    }

    protected void printIf(int i, PrintStream out) {
        out.append("if ");
        tests.get(i).print(out);
        out.println(":");
        printIndented(stmts.get(i), out);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(tests).add(stmts).add(elze).toList();
    }

    public StmtNode getElse() {
        return elze;
    }

    public List<PythonNode> getTests() {
        return tests;
    }

    public List<StmtNode> getStmts() {
        return stmts;
    }

    public void setTest(int index, PythonNode expr) {
        expr.setParent(this);
        tests.set(index, expr);
    }

    public void setStmt(int index, StmtNode stmt) {
        stmt.setParent(this);
        stmts.set(index, stmt);
    }

    public void setElse(StmtNode elze) {
        this.elze = elze;
        if (elze != null) elze.setParent(this);
    }

    public void setTests(List<PythonNode> tests) {
        this.tests = tests;
        for (PythonNode test : tests) {
            if (test != null) test.setParent(this);
        }
    }

    public void setStmts(List<StmtNode> stmts) {
        this.stmts = stmts;
        for (PythonNode stmt : stmts) {
            if (stmt != null) stmt.setParent(this);
        }
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int i;
        if (elze == what) {
            elze = (StmtNode) with;
        } else if ((i = tests.indexOf(what)) >= 0) {
            tests.set(i, with);
        } else if ((i = stmts.indexOf(what)) >= 0) {
            stmts.set(i, (StmtNode) with);
        } else {
            return false;
        }

        setAsParent(with);
        return true;
    }
}
