package su.msu.cs.lvk.xml2pixy.transform;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 30.09.2007
 * Time: 0:27:40
 */

/**
 * Wrapper for xml tags. Maps source python ast nodes to target php parse subtrees.
 */
public class Node {

    private Element jdomElement;
    private ParseNode parseNode;
    private List<Node> children;
    private ASTNode astNode;

    public Node() {
        children = new ArrayList<Node>();
    }

    public Node(Element jdomElement) {
        this();
        this.jdomElement = jdomElement;
    }

    public Element getJdomElement() {
        return jdomElement;
    }

    public void setJdomElement(Element jdomElement) {
        this.jdomElement = jdomElement;
    }

    public ParseNode getParseNode() {
        return parseNode;
    }

    public void setParseNode(ParseNode parseNode) {
        this.parseNode = parseNode;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public void addChild(int index, Node node) {
        if (children == null) children = new ArrayList<Node>();
        children.add(node);
    }

    public ASTNode getAstNode() {
        return astNode;
    }

    public void setAstNode(ASTNode astNode) {
        this.astNode = astNode;
    }

    /**
     * Builds a list of children nodes with the given name.
     * @param name name of target xml tag children
     * @return a list of children with the given name or all children if <code>name</code> is blank
     */
    public List<Node> getChildren(String name) {
        if (Utils.isBlank(name)) return this.children;
        List<Node> nodes = new ArrayList<Node>();
        for (Node child : this.children) {
            if (child.getJdomElement().getName().equals(name)) {
                nodes.add(child);
            }
        }
        return nodes;
    }
}
