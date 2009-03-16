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
 * Time: 22:56:47
 */
public class CallFuncNode extends PythonNode {

    protected PythonNode node;
    protected List<PythonNode> args = new ArrayList<PythonNode>();
    protected PythonNode starArgs;
    protected PythonNode dstarArgs;

    public CallFuncNode(PythonNode node, List<PythonNode> args, PythonNode starArgs, PythonNode dstarArgs) {
        this.node = node;
        this.args = new ArrayList<PythonNode>(args);
        this.starArgs = starArgs;
        this.dstarArgs = dstarArgs;

        setAsParent(node, starArgs, dstarArgs);
        setAsParent(args);
    }

    public CallFuncNode(Element jdom) {
        super(jdom);

        this.node = makeNode((Element) jdom.getChild("node").getChildren().get(0));
        this.node.setParent(this);

        for (Object child : jdom.getChild("args").getChildren()) {
            PythonNode newNode = makeNode((Element) child);
            newNode.setParent(this);
            this.args.add(newNode);
        }
        List starArgs = jdom.getChild("star_args").getChildren();
        if (!starArgs.isEmpty()) {
            this.starArgs = makeNode((Element) starArgs.get(0));
            this.starArgs.setParent(this);
        }
        List dstarArgs = jdom.getChild("dstar_args").getChildren();
        if (!dstarArgs.isEmpty()) {
            this.dstarArgs = makeNode((Element) dstarArgs.get(0));
            this.dstarArgs.setParent(this);
        }
    }

    public CallFuncNode(Node node) {
        super(node);

        this.node = (PythonNode) node.getChildren("node").get(0).getChildren().get(0).getAstNode();
        this.node.setParent(this);

        for (Node child : node.getChildren("args").get(0).getChildren()) {
            PythonNode newNode = (PythonNode) child.getAstNode();
            newNode.setParent(this);
            this.args.add(newNode);
        }
        List<Node> starArgs = node.getChildren("star_args").get(0).getChildren();
        if (!starArgs.isEmpty()) {
            this.starArgs = (PythonNode) starArgs.get(0).getAstNode();
            this.starArgs.setParent(this);
        }
        List<Node> dstarArgs = node.getChildren("dstar_args").get(0).getChildren();
        if (!dstarArgs.isEmpty()) {
            this.dstarArgs = (PythonNode) dstarArgs.get(0).getAstNode();
            this.dstarArgs.setParent(this);
        }
    }

    public void print(PrintStream out) {
        if (node != null) node.print(out);
        out.append('(');
        boolean first = true;
        for (PythonNode child : args) {
            if (!first) out.append(", ");
            first = false;
            child.print(out);
        }
        if (starArgs != null) {
            if (!first) out.append(", ");
            first = false;
            out.append('*');
            starArgs.print(out);
        }
        if (dstarArgs != null) {
            if (!first) out.append(", ");
            out.append("**");
            dstarArgs.print(out);
        }
        out.append(')');
    }

    public PythonNode getNode() {
        return node;
    }

    public List<PythonNode> getArgs() {
        return args;
    }

    public PythonNode getStarArgs() {
        return starArgs;
    }

    public PythonNode getDstarArgs() {
        return dstarArgs;
    }

    public void setNode(PythonNode node) {
        this.node = node;
        if (this.node != null) this.node.setParent(this);
    }

    public void setArgs(List<PythonNode> args) {
        this.args = args;
    }

    public void setStarArgs(PythonNode starArgs) {
        this.starArgs = starArgs;
        if (this.starArgs != null) this.starArgs.setParent(this);
    }

    public void setDstarArgs(PythonNode dstarArgs) {
        this.dstarArgs = dstarArgs;
        if (this.dstarArgs != null) this.dstarArgs.setParent(this);
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(node).add(args).add(starArgs).add(dstarArgs).toList();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        int index;
        if (node == what) setNode(with);
        else if (starArgs == what) setStarArgs(with);
        else if (dstarArgs == what) setDstarArgs(with);
        else if ((index = args.indexOf(what)) >= 0) {
            if (with != null) {
                args.set(index, with);
                with.setParent(this);
            } else {
                args.remove(index);
            }
        } else {
            return false;
        }

        return true;
    }


}
