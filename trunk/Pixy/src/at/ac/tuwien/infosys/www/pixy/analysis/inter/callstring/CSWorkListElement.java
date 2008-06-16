package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public final class CSWorkListElement {

    private final CfgNode cfgNode;
    private final int position;

// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************    
    
    CSWorkListElement(CfgNode cfgNode, int position) {
        this.cfgNode = cfgNode;
        this.position = position;
    }
    
// *********************************************************************************    
// GET *****************************************************************************
// *********************************************************************************    
    
    CfgNode getCfgNode() {
        return this.cfgNode;
    }

    int getPosition() {
        return this.position;
    }
}
