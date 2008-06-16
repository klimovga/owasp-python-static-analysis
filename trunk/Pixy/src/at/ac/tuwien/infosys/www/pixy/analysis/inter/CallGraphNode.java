package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

public class CallGraphNode {

    private TacFunction function;
    
    // contained call nodes -> target call graph node
    private Map<CfgNodeCall,CallGraphNode> outEdges;
    
    // call nodes from callers -> caller's call graph node
    private Map<CfgNodeCall,CallGraphNode> inEdges;
    
//  ********************************************************************************
    
    CallGraphNode(TacFunction function) {
        this.function = function;
        //this.successors = new HashSet<CallGraphNode>();
        //this.predecessors = new HashSet<CallGraphNode>();
        this.outEdges = new HashMap<CfgNodeCall,CallGraphNode>();
        this.inEdges = new HashMap<CfgNodeCall,CallGraphNode>();
    }
    
//  ********************************************************************************
    
    public TacFunction getFunction() {
        return this.function;
    }
    
    Collection<CallGraphNode> getSuccessors() {
        return this.outEdges.values();
    }
    
    Collection<CallGraphNode> getPredecessors() {
        return this.inEdges.values();
    }
    
    Set<CfgNodeCall> getCallsTo() {
        return this.inEdges.keySet();
    }
    
//  ********************************************************************************
    
    public boolean equals(Object compX) {
        
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof CallGraphNode)) {
            return false;
        }
        CallGraphNode comp = (CallGraphNode) compX;

        return this.function.equals(comp.function);
    }

//  ********************************************************************************
    
    public int hashCode() {
        return this.function.hashCode();
    }


    public void addCallee(CfgNodeCall callNode, CallGraphNode calleeNode) {
        //this.successors.add(calleeNode);
        this.outEdges.put(callNode, calleeNode);
    }


    public void addCaller(CfgNodeCall callNode, CallGraphNode callerNode) {
        //this.predecessors.add(callerNode);
        this.inEdges.put(callNode, callerNode);
    }
    

}
