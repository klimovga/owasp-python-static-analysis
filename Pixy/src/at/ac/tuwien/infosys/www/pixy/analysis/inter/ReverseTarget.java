package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

public class ReverseTarget {

    private CfgNodeCall callNode;
    
    // a set of Contexts
    private Set<? extends Context> contexts;
    
//  *********************************************************************************    
//  CONSTRUCTORS ********************************************************************
//  ********************************************************************************* 

    public ReverseTarget(CfgNodeCall callNode, Set<? extends Context> contexts) {
        this.callNode = callNode;
        this.contexts = contexts;
    }

//  *********************************************************************************    
//  GET *****************************************************************************
//  ********************************************************************************* 

    public CfgNodeCall getCallNode() {
        return this.callNode;
    }
    
    public Set<? extends Context> getContexts() {
        return this.contexts;
    }
}
