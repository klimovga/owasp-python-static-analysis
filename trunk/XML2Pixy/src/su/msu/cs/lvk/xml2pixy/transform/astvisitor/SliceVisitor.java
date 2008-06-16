package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Visit slice construct like x[4:6]
 *
 * @author ikonv
 */
public class SliceVisitor extends ASTVisitor {

    protected static Logger log = Logger.getLogger(SliceVisitor.class.getName());

    public SliceVisitor() {
        super();
    }

    public SliceVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        Attribute flags = node.getJdomElement().getAttribute("flags");
        if (flags != null && "OP_APPLY".equals(flags.getValue())) {
            ParseNode targetNode = getChild(getFirstChild(node, "expr"), "expr");
            targetNode = makeCvar(targetNode);

            ParseNode lowerNode = createLowerNode(node, currentFile, lineno);
            ParseNode upperNode = createUpperNode(node, targetNode, currentFile, lineno);

            if (lowerNode != null && upperNode != null && targetNode != null) {
                ParseNode funcNode = makeFunctionCall("analysis_slice_list",
                        new ParseNode[] {lowerNode, upperNode}, targetNode, lineno);
                node.setParseNode(funcNode);
            } else {
                // will be set later?
                node.setParseNode(null);
            }
        } else {
            log.warn("WARNING: Unsupported kind of Slice found (line: "
                    + currentFile + ':' + lineno + ")");
        }
    }

    private ParseNode createLowerNode(Node node, String currentFile, int lineno) {
        ParseNode lowerNode;
        Node lower = getFirstChild(node, "lower");
        if (!lower.getChildren().isEmpty()) {
            lowerNode = lower.getChildren().get(0).getParseNode();
        } else {
            // lower bound is not set, set it to 0
            lowerNode = makeNumberExpr(0, lineno);
        }
        return lowerNode;
    }

    private ParseNode createUpperNode(Node node, ParseNode targetNode, String currentFile, int lineno) {
        ParseNode upperNode;
        Node upper = getFirstChild(node, "upper");
        if (!upper.getChildren().isEmpty()) {
            upperNode = upper.getChildren().get(0).getParseNode();
        } else {
            // upper bound is not set, set it to count($arr)
            upperNode = makeFunctionCall("count", new ParseNode[0], targetNode, lineno);
        }
        return upperNode;
    }

    private ParseNode getChild(Node node, String name) {
        if (node.getChildren().isEmpty()) {
            log.warn("WARNING: " + name + " body of Slice is empty!");
            return null;
        } else {
            return node.getChildren().get(0).getParseNode();
        }
    }
}
