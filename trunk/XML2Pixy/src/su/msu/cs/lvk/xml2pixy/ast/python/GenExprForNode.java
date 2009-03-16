package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GenExprForNode extends PythonNode {

    protected PythonNode assign;
    protected List<GenExprIfNode> ifs = new ArrayList<GenExprIfNode>();
    protected PythonNode iter;

    protected GenExprForNode() {
        super();
    }

    public GenExprForNode(Element jdom) {
        super(jdom);

        for (Object child : jdom.getChild("ifs").getChildren()) {
            GenExprIfNode newNode = (GenExprIfNode)makeNode((Element)child);
            setAsParent(newNode);
            this.ifs.add(newNode);
        }
        assign = makeNode(getFirst(jdom, "assign"));
        iter = makeNode(getFirst(jdom, "iter"));

        setAsParent(assign, iter);
    }

    public GenExprForNode(Node node) {
        super(node);

        for (Node child : node.getChildren("ifs").get(0).getChildren()) {
            GenExprIfNode newNode = (GenExprIfNode) makeNode(child);
            setAsParent(newNode);
            this.ifs.add(newNode);
        }

        assign = makeNode(getFirst(node, "assign"));
        iter = makeNode(getFirst(node, "iter"));

        setAsParent(assign, iter);
    }

    public void print(PrintStream out) {
        out.append("for ");
        assign.print(out);
        out.append(" in ");
        iter.print(out);
        for (GenExprIfNode ifNode : ifs) {
            out.append(" ");
            ifNode.print(out);
        }
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int index;
        if (assign == what) {
            assign = with;
            setAsParent(assign);
        } else if (iter == what) {
            iter = with;
            setAsParent(with);
        } else if ((index = ifs.indexOf(what)) >= 0) {
            if (with != null) {
                ifs.set(index, (GenExprIfNode)with);
                setAsParent(with);
            } else {
                ifs.remove(index);
            }
        } else {
            return false;
        }
        return true;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(assign).add(ifs).add(iter).toList();
    }

    public PythonNode getAssign() {
        return assign;
    }

    public void setAssign(PythonNode assign) {
        this.assign = assign;
    }

    public List<GenExprIfNode> getIfs() {
        return ifs;
    }

    public void setIfs(List<GenExprIfNode> ifs) {
        this.ifs = ifs;
    }

    public PythonNode getIter() {
        return iter;
    }

    public void setIter(PythonNode iter) {
        this.iter = iter;
    }
}
