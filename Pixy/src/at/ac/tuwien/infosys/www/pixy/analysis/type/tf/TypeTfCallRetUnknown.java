package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;


import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallRet;

public class TypeTfCallRetUnknown
extends TransferFunction {

    private CfgNodeCallRet retNode;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    public TypeTfCallRetUnknown(CfgNodeCallRet retNode) {
        this.retNode = retNode;
    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX, Context context) {
        
        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.handleReturnValueUnknown(this.retNode.getTempVar());

        return out;
        
    }
    
    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public LatticeElement transfer(LatticeElement inX) {
        throw new RuntimeException("SNH");
    }
    

}



