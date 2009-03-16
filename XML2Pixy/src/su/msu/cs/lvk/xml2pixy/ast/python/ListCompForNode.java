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
 * Time: 22:05:00
 */
public class ListCompForNode extends PythonNode {

    protected PythonNode assign;
    protected PythonNode list;
    protected List<ListCompIfNode> ifs = new ArrayList<ListCompIfNode>();

    protected ListCompForNode() {
        super();
    }

    public ListCompForNode(Element jdom) {
        super(jdom);

        assign = makeNode(getFirst(jdom, "assign"));
        assign.setParent(this);

        list = makeNode(getFirst(jdom, "list"));
        list.setParent(list);

        for (Object child : jdom.getChild("ifs").getChildren()) {
            ListCompIfNode newNode = (ListCompIfNode) makeNode((Element) child);
            newNode.setParent(this);
            ifs.add(newNode);
        }
    }

    public ListCompForNode(Node node) {
        super(node);

        assign = makeNode(getFirst(node, "assign"));
        assign.setParent(this);

        list = makeNode(getFirst(node, "list"));
        list.setParent(list);

        for (Node child : node.getChildren("ifs").get(0).getChildren()) {
            ListCompIfNode newNode = (ListCompIfNode) makeNode(child);
            newNode.setParent(this);
            ifs.add(newNode);
        }
    }

    public void print(PrintStream out) {
        out.append("for ");
        assign.print(out);
        out.append(" in ");
        list.print(out);
        for (ListCompIfNode listCompIf : ifs) {
            out.append(' ');
            listCompIf.print(out);
        }
    }

    public PythonNode getAssign() {
        return assign;
    }

    public PythonNode getList() {
        return list;
    }

    public List<ListCompIfNode> getIfs() {
        return ifs;
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(assign).add(list).add(ifs).toList();
    }

}
