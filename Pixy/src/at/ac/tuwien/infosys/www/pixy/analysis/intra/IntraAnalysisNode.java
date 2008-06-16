package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;



// an AnalysisNode holds analysis-specific information for a certain CFGNode
public class IntraAnalysisNode 
extends AnalysisNode {

    // input lattice element at current CFG node
    LatticeElement inValue;

// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// ********************************************************************************* 
    
    protected IntraAnalysisNode(TransferFunction tf) {
        super(tf);
        this.inValue = null;
    }
    
// *********************************************************************************
// GET *****************************************************************************    
// *********************************************************************************
    
    public LatticeElement getInValue() {
        return this.inValue;
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    protected void setInValue(LatticeElement inValue) {
        this.inValue = inValue;
    }
    
// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

}

