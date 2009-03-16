package su.msu.cs.lvk.xml2pixy.jdom;

import org.apache.log4j.Logger;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.ArrayList;
import java.util.List;

/**
 * JDOM XML tree non-recursive walker.
 */
public class JdomWalker {

    protected static Logger log = Logger.getLogger(JdomWalker.class.getName());

    private JdomVisitor visitor;

    public JdomWalker(JdomVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Walk JDOM tree with provided walker using breadth-first algorithm.
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public Node walkWide(Element root) {
        List<Node> nodes = buildList(root);

        for (Node node : nodes) {
            try {
                visitor.visit(node);
            } catch (VisitorException e) {
                log.error("Error visiting node " + node, e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Walk JDOM tree with provided walker using reversed breadth-first algorithm.
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public Node walkWideReverse(Element root) {
        List<Node> nodes = buildList(root);

        for (int i = nodes.size() - 1; i >= 0; i--) {
            try {
                visitor.visit(nodes.get(i));
            } catch (VisitorException e) {
                log.error("Error visiting node " + nodes.get(i) ,e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Walk JDOM tree with provided walker using deep-first algorithm.
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public Node walkDeep(Element root) {
        Node rootNode = new Node(root);
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(rootNode);

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            List<Element> children = (List<Element>) node.getJdomElement().getChildren();
            for (int j = 0; j < children.size(); j++) {
                Node newNode = new Node(children.get(j));
                nodes.add(i + 1 + j, newNode);
                node.addChild(-1, newNode);
            }
        }

        for (Node node1 : nodes) {
            try {
                visitor.visit(node1);
            } catch (VisitorException e) {
                log.error("Error visiting node " + node1, e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Builds children nodes list for provided JDOM element for using by breadth-first walker.
     * @param root jdom element
     * @return built list of children Nodes
     */
    public List<Node> buildList(Element root) {
        Node rootNode = new Node(root);
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(rootNode);

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            for (Element elem : (List<Element>) node.getJdomElement().getChildren()) {
                Node newNode = new Node(elem);
                node.addChild(nodes.size(), newNode);
                nodes.add(newNode);
            }
        }

        return nodes;
    }
}
