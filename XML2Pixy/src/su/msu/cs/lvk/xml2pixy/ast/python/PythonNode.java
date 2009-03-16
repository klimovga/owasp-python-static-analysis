package su.msu.cs.lvk.xml2pixy.ast.python;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 22:19:32
 */
public class PythonNode extends ASTNode {

    /*
    TODO:
Decorators
Ellipsis
Expression
TryExcept
     */


    private static Logger log = Logger.getLogger(PythonNode.class);

    protected static int indent = 0;
    protected static int indentStep = 4;

    protected PythonNode parent;

    protected PythonNode() {
        this.nodeName = StringUtils.substringAfterLast(this.getClass().getName(), ".");
        this.nodeName = StringUtils.substringBefore(this.nodeName, "Node");
    }

    public PythonNode(Element jdom) {
        if (jdom == null) {
            throw new IllegalArgumentException();
        }
        this.nodeName = jdom.getName();
        this.lineno = parseLineno(jdom);
        this.fileName = jdom.getAttributeValue("filename");
    }

    public PythonNode(Node node) {
        this(node.getJdomElement());
        node.setAstNode(this);
    }

    protected int parseLineno(Element jdom) {
        try {
            return jdom.getAttribute("lineno").getIntValue();
        } catch (Exception e) {
            return -1;
        }
    }

    public static PythonNode makeNode(Element jdom) {
        if (jdom == null) return null;
        String nodeName = jdom.getName().trim();
        try {
            return (PythonNode) Class.forName("su.msu.cs.lvk.xml2pixy.ast.python." + nodeName + "Node")
                    .getConstructor(Element.class).newInstance(jdom);
        } catch (Exception e) {
            PythonNode newNode = new PythonNode(jdom);
            if (Character.isUpperCase(nodeName.charAt(0))) {
                log.debug("unknown node " + nodeName, e);
                log.warn("WARNING: Node " + nodeName + " is currently unsupported (line: "
                        + newNode.getFileName() + ':' + newNode.getLineno() + ")");
            }
            return newNode;
        }
    }

    public static PythonNode makeNode(Node node) {
        if (node == null) return null;
        String nodeName = node.getJdomElement().getName().trim();
        if (node.getAstNode() != null) {
            return (PythonNode) node.getAstNode();
        }
        try {
            return (PythonNode) Class.forName("su.msu.cs.lvk.xml2pixy.ast.python."
                    + nodeName + "Node")
                    .getConstructor(Node.class).newInstance(node);
        } catch (Throwable e) {
            PythonNode newNode = new PythonNode(node);
            if (Character.isUpperCase(nodeName.charAt(0))) {
                log.debug("unknown node " + nodeName, e);
                log.warn("WARNING: Node " + nodeName + " is currently unsupported (line: "
                        + newNode.getFileName() + ':' + newNode.getLineno() + ")");
            }
            return newNode;
        }
    }

    protected Integer getInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double getDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public PythonNode getParent() {
        return parent;
    }

    public void setParent(PythonNode parent) {
        this.parent = parent;
    }

    protected Element getFirst(Element element, String name) {
        if (element != null) {
            Element child = element.getChild(name);
            if (child != null) {
                List children = child.getChildren();
                if (children != null && !children.isEmpty()) {
                    return (Element) children.get(0);
                }
            }
        }

        return null;
    }

    protected Node getFirst(Node node, String name) {
        if (node != null) {
            List<Node> children = node.getChildren(name);
            if (children != null && !children.isEmpty()) {
                children = children.get(0).getChildren();
                if (children != null && !children.isEmpty()) {
                    return children.get(0);
                }
            }
        }

        return null;
    }

    protected void printIndented(PythonNode node, PrintStream out) {
        indent += indentStep;
        node.print(out);
        indent -= indentStep;
    }

    public List<ASTNode> getChildren() {
        return new ArrayList<ASTNode>();
    }

    public boolean replace(PythonNode what, PythonNode with) {
        throw new UnsupportedOperationException(getNodeName() + "Node.replace()");
    }

    public ScopeNode getScope() {
        PythonNode current = this;
        while (current != null) {
            if (current instanceof ScopeNode) {
                return (ScopeNode) current;
            }
            current = current.getParent();
        }

        return null;
    }

    public PythonNode setLocation(String file, int lineno) {
        this.fileName = file;
        this.lineno = lineno;
        return this;
    }

    public PythonNode copyLocation(PythonNode node) {
        this.fileName = node.getFileName();
        this.lineno = node.findLineno();
        return this;
    }

    public int findLineno() {
        if (this.getLineno() < 0) {
            List<ASTNode> children = getChildren();
            for (ASTNode child : children) {
                int lineno = ((PythonNode) child).findLineno();
                if (lineno >= 0) {
                    return lineno;
                }
            }
        } else {
            return this.getLineno();
        }
        return -1;
    }

    public void setAsParent(List<PythonNode> nodes) {
        for (PythonNode node : nodes) {
            if (node != null) node.setParent(this);
        }
    }

    public void setAsParent(PythonNode ... nodes) {
        setAsParent(Arrays.asList(nodes));
    }

}
