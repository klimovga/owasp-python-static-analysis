package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class InterAnalysisInfo 
extends AnalysisInfo {

    public InterAnalysisInfo() {
        super();
    }
    
    // folds all analysis nodes (using recycling) and clears the phi maps
    // (=> saves memory)
    public void foldRecycledAndClean(InterAnalysis analysis) {
        for (Iterator iter = this.map.values().iterator(); iter.hasNext();) {
            InterAnalysisNode analysisNode = (InterAnalysisNode) iter.next();
            LatticeElement foldedValue = analysisNode.computeFoldedValue();
            foldedValue = analysis.recycle(foldedValue);
            analysisNode.setFoldedValue(foldedValue);
            analysisNode.clearPhiMap();
        }
    }
    
    // note that not all cfg nodes have an associated analysis node:
    // - nodes inside basic blocks
    // - nodes inside function default cfgs
    // for such nodes, you should query the enclosing basic block, or the
    // entry node of the function default cfg; use the appropriate "get"
    // method of CfgNode to retrieve these nodes
    public InterAnalysisNode getAnalysisNode(CfgNode cfgNode) {
        return (InterAnalysisNode) this.map.get(cfgNode);
    }

    public TransferFunction getTransferFunction (CfgNode cfgNode) {
        AnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }
    
    
}
