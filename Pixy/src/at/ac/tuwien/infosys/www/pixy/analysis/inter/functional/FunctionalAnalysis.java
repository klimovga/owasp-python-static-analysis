package at.ac.tuwien.infosys.www.pixy.analysis.inter.functional;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;


// base class for analysis using the functional approach of Sharir and
// Pnueli; use this if your lattice is finite
public class FunctionalAnalysis 
extends AnalysisType { 

// *********************************************************************************    
// GET *****************************************************************************
// ********************************************************************************* 
    
//  getPropagationContext ***********************************************************
    
    public Context getPropagationContext(CfgNodeCall callNode, Context context) {
        // propagation context = incoming value at the call node under the
        // current context
        LatticeElement inValue = this.enclosedAnalysis.getInterAnalysisInfo().getAnalysisNode(callNode).getPhiValue(context);
        return new FunctionalContext(inValue);
    }
    
//  getReverseTargets ***************************************************************
    
    public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context contextX) {
        
        //System.out.println("call to getReverseTartet!");
        
        List<ReverseTarget> retMe = new LinkedList<ReverseTarget>();
        
        FunctionalContext context = (FunctionalContext) contextX;
        
        // for each call to this function...
        List calls = exitedFunction.getCalledFrom();
        for (Iterator iter = calls.iterator(); iter.hasNext(); ) {
            
            // get call node
            CfgNodeCall callNode = (CfgNodeCall) iter.next();
            
            //System.out.println("found call: " + callNode.getOrigLineno());
            
            // find out possible contexts of the callee;
            // example: caller has two contexts c1 and c2; under both contexts,
            // the incoming value at the call node is e1; for the callee,
            // the context e1 is used for both caller contexts; hence, when
            // returning from the callee, we have to propagate the info back
            // to both caller contexts c1 and c2
            FunctionalAnalysisNode analysisNode = (FunctionalAnalysisNode) 
                this.enclosedAnalysis.getInterAnalysisInfo().getAnalysisNode(callNode);
            if (analysisNode == null) {
                // LATER: might be useful debugging info
                /*
                System.out.println(
                    "Warning: check for unreachable call to " + exitedFunction.getName());
                System.out.println("File: " + callNode.getFileName() + ", line " + callNode.getOrigLineno());
                */
                continue;
            }
            Set<FunctionalContext> calleeContexts = 
                analysisNode.getReversePhiContexts(context.getLatticeElement());
            
            // during this for loop, there is always at least one non-null set, i.e.
            // the following branch is entered at least once (for all exit nodes
            // except that of the _main function)
            if (calleeContexts != null) {
                // found matching contexts for this call node!
                ReverseTarget revTarget = new ReverseTarget(callNode, calleeContexts);
                retMe.add(revTarget);
            } else {
/*
                System.out.println("no matching contexts found!");
*/
            }
        }
        
        return retMe;

    }
    
//  *********************************************************************************    
//  OTHER ***************************************************************************
//  *********************************************************************************

    public InterAnalysisNode makeAnalysisNode(CfgNode cfgNode, TransferFunction tf) {
        return new FunctionalAnalysisNode(cfgNode, tf);
    }

    public boolean useSummaries() {
        return true;
    }

    public Context initContext(InterAnalysis analysis) {
        return new FunctionalContext(analysis.getStartValue());
    }

}



