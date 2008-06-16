package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.*;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;



// *********************************************************************************
// CfgNodeEmpty ********************************************************************
// *********************************************************************************


public class CfgNodeEmpty
extends CfgNode {
    
// CONSTRUCTORS ********************************************************************    

    public CfgNodeEmpty() {
        super();
        // empty CFG nodes will be deleted from the CFG, so their ID's can be
        // recycled; TOO DANGEROUS TO DO IT HERE! better: additional pass over
        // all CFGs
        // maxId--;
    }
    
    public CfgNodeEmpty(ParseNode parseNode) {
        super(parseNode);
    }
    
//  GET ****************************************************************************
    
    public List<Variable> getVariables() {
        return Collections.emptyList();
    }
    
//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
    }

}

