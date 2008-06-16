package su.msu.cs.lvk.xml2pixy.parser;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;


/**
 * Helper class for adding children for ParseNodes
 */
public class NodeBinding {
    private ParseNode parent;

    public NodeBinding(ParseNode parent) {
        this.parent = parent;
    }

    public NodeBinding addChild(ParseNode child) {
        parent.addChild(child);

        return this;
    }

    public ParseNode getParent() {
        return parent;
    }

    public void setParent(ParseNode parent) {
        this.parent = parent;
    }
}
