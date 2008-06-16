package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;

/**
 * Point to extract expression from.
 *
 * @author ikonv
 */
public class ExpressionExtractionPoint {
    private ParseNode parent; // XXX: redundant, targetNode.getParent() gives the same!
    private int childIndex;
    private ParseNode targetNode; // expr or expression_without_variable

    public ExpressionExtractionPoint(ParseNode targetNode) {
        if (targetNode.getSymbol() != PhpSymbols.expr
                && targetNode.getSymbol() != PhpSymbols.expr_without_variable) {
            throw new IllegalArgumentException("targetNode must be expr or expr_without_variable");
        }

        this.parent = targetNode.getParent();
        this.targetNode = targetNode;

        childIndex = -1;
        for (int i = 0; i < parent.getNumChildren() && childIndex == -1; ++i) {
            ParseNode child = parent.getChild(i);
            if (child == targetNode) {
                this.childIndex = i;
            }
        }

        if (childIndex == -1) {
            throw new IllegalArgumentException("Expr parameter should be a child of given parent");
        }
    }

    public ExpressionExtractionPoint(ParseNode parent, int childIndex, ParseNode expr) {
        this.parent = parent;
        this.childIndex = childIndex;
        this.targetNode = expr;
    }

    public ParseNode getParent() {
        return parent;
    }

    public void setParent(ParseNode parent) {
        this.parent = parent;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }

    public ParseNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(ParseNode targetNode) {
        this.targetNode = targetNode;
    }
}
