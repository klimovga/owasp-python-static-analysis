package su.msu.cs.lvk.xml2pixy.ast;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.ArrayList;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 15.10.2008
 * Time: 22:43:48
 */
public class Walker {
    protected static Logger log = Logger.getLogger(Walker.class);

    private Visitor visitor;

    public Walker(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Walk JDOM tree with provided walker using breadth-first algorithm.
     *
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public ASTNode walkWide(ASTNode root) {
        List<ASTNode> nodes = buildList(root);

        for (ASTNode node : nodes) {
            try {
                if (!visitor.visit(node)) break;
            } catch (VisitorException e) {
                log.error("Error visiting node " + node, e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Walk JDOM tree with provided walker using reversed breadth-first algorithm.
     *
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public ASTNode walkWideReverse(ASTNode root) {
        List<ASTNode> nodes = buildList(root);

        for (int i = nodes.size() - 1; i >= 0; i--) {
            try {
                if (!visitor.visit(nodes.get(i))) break;
            } catch (VisitorException e) {
                log.error("Error visiting node " + nodes.get(i), e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Walk JDOM tree with provided walker using deep-first algorithm.
     *
     * @param root tree root element
     * @return Node element wrapping provided root
     */
    public ASTNode walkDeep(ASTNode root) {
        ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
        nodes.add(root);

        for (int i = 0; i < nodes.size(); i++) {
            ASTNode node = nodes.get(i);
            List<ASTNode> children = node.getChildren();
            if (i == nodes.size() - 1) {
                nodes.addAll(children);
            } else {
                nodes.addAll(i + 1, children);
            }
        }

        for (ASTNode node : nodes) {
            try {
                if (!visitor.visit(node)) break;
            } catch (VisitorException e) {
                log.error("Error visiting node " + node, e);
            }
        }

        if (!nodes.isEmpty()) return nodes.get(0);
        else return null;
    }

    /**
     * Builds children nodes list for provided JDOM element for using by breadth-first walker.
     *
     * @param root jdom element
     * @return built list of children Nodes
     */
    public List<ASTNode> buildList(ASTNode root) {
        ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
        nodes.add(root);

        for (int i = 0; i < nodes.size(); i++) {
            ASTNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
        }

        return nodes;
    }
}
