package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.*;
import at.ac.tuwien.infosys.www.pixy.analysis.Analysis;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;
import org.apache.log4j.Logger;

// base class for interprocedural analyses (Sharir and Pnueli);
// can be used for the functional and the call-string approach;
// the different approaches have to implement the following abstract
// methods:
// - getPropagationContext
// - getReverseTargets
// the concrete analyses derived from these approaches have to
// - implement the remaining abstract methods:
//   - initLattice
//   - evalIf
// - override those transfer function generators that shall return
//   transfer functions other than the ID transfer function
// - call initGeneral()

public abstract class InterAnalysis extends Analysis {

    protected static Logger log = Logger.getLogger(InterAnalysis.class.getName());

    // INPUT ***********************************************************************
    
    // functional or CS analysis
    protected AnalysisType analysisType;
    
    // OUTPUT **********************************************************************

    // analysis information (maps each CfgNode to an InterAnalysisNode)
    protected InterAnalysisInfo interAnalysisInfo;
    
    // OTHER ***********************************************************************
    
    // the main function
    protected TacFunction mainFunction;

    // context for the main function
    protected Context mainContext;
    
    // worklist consisting of pairs (Cfg node, lattice element)
    InterWorkList workList;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// ********************************************************************************* 

// initGeneral *********************************************************************
    
    // general initialization work; taken out of the constructor to bypass the
    // restriction that superclass constructors have to be called first;
    // the "functions" map has to map function name -> TacFunction object
    protected void initGeneral(List<TacFunction> functions, TacFunction mainFunction, 
            AnalysisType analysisType, InterWorkList workList) {

        this.analysisType = analysisType;
        this.analysisType.setAnalysis(this);
        this.functions = functions;

        // determine Cfg of main function: start analysis here
        this.mainFunction = mainFunction;
        Cfg mainCfg = this.mainFunction.getCfg();
        CfgNode mainHead = mainCfg.getHead();
        
        // initialize carrier lattice
        this.initLattice();
        
        // initialize main context
        this.mainContext = this.analysisType.initContext(this);

        // initialize worklist
        this.workList = workList;
        this.workList.add(mainHead, this.mainContext);

        // initialize analysis nodes
        // this.analysisInfo = new AnalysisNode[maxNodeId + 1];
        this.interAnalysisInfo = new InterAnalysisInfo();
        this.genericAnalysisInfo = interAnalysisInfo;
        // assign transfer functions
        this.initTransferFunctions();

        // initialize PHI map for start node
        InterAnalysisNode startAnalysisNode = 
            (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(mainHead);
        startAnalysisNode.setPhiValue(this.mainContext, this.startValue);
    }
    
//  initTransferFunctions ***********************************************************

    // controls the assignment of transfer functions to analysis nodes by calling
    // traverseCfg()
    void initTransferFunctions() {

        // handle default CFGs (for default parameters) first;
        // for all functions...
        for (TacFunction function : this.functions) {
            
            // extract params
            List params = function.getParams();
            
            // for each param...
            for (Iterator iter2 = params.iterator(); iter2.hasNext(); ) {
                
                TacFormalParam param = (TacFormalParam) iter2.next();
                
                // if this param has a default value, it also has a small CFG;
                // traverse it as well...;
                // NOTE: default CFGs will not be associated with analysis information,
                // see transfer functions for CfgNodeCallPrep; analogous to the 
                // contents of basic blocks
                if (param.hasDefault()) {
                    Cfg defaultCfg = param.getDefaultCfg();
                    this.traverseCfg(defaultCfg, function);
                }
            }
        }

        // now handle "normal" CFGs;
        // for all functions...
        for (TacFunction function : this.functions) {
            // extract and traverse CFG
            this.traverseCfg(function.getCfg(), function);
        }
    }

// *********************************************************************************    
// GET *****************************************************************************
// ********************************************************************************* 

//  getPropagationContext **********************************************************
    
    // returns the context to which interprocedural propagation shall
    // be conducted (used at call nodes)
    public Context getPropagationContext(CfgNodeCall callNode, Context context) {
        return this.analysisType.getPropagationContext(callNode, context);
    }
    
//  getReverseTargets **************************************************************
    
    // returns a set of ReverseTarget objects to which interprocedural
    // propagation shall be conducted (used at exit nodes)
    public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context context) {
        return this.analysisType.getReverseTargets(exitedFunction, context);
    }
    
//  getTransferFunction ************************************************************

    public TransferFunction getTransferFunction(CfgNode cfgNode) {
        return this.interAnalysisInfo.getTransferFunction(cfgNode);
    }

//  getAnalysisInfo *****************************************************************

    public InterAnalysisInfo getInterAnalysisInfo() {
        return this.interAnalysisInfo;
    }

//  getAnalysisNode ****************************************************************

    public InterAnalysisNode getAnalysisNode(CfgNode cfgNode) {
        return (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(cfgNode);
    }
    

// *********************************************************************************    
// OTHER ***************************************************************************
// ********************************************************************************* 

//  makeAnalysisNode ***************************************************************
    
    // creates and returns an analysis node for the given parameters that is
    // appropriate for the analysis type (functional / call-string)
    protected AnalysisNode makeAnalysisNode(CfgNode cfgNode, TransferFunction tf) {
        return this.analysisType.makeAnalysisNode(cfgNode, tf);
    }

//  evalIf **************************************************************************

    // returns Boolean.TRUE, Boolean.FALSE, or null if it can't be evaluated
    protected abstract Boolean evalIf(CfgNodeIf ifNode, LatticeElement inValue);
    
//  useSummaries *******************************************************************
    
    // indicates whether to use function summaries during the analysis or not
    // (works for functional approach, but would lead to wrong results for
    // call string analysis; there is a test case for literal analysis that
    // demonstrates this)
    protected boolean useSummaries() {
        return this.analysisType.useSummaries();
    }

//  ********************************************************************************
    
    private static void debug(String s) {
        if (false) {
            System.out.println(s);
        }
    }
    
//  analyze ************************************************************************

    // this method applies the worklist algorithm
    public void analyze() {

        int steps = 0;
        
        // for each element in the worklist...
        // (each worklist element is a pair of CFG node & context lattice element)
        while (this.workList.hasNext()) {

            steps++;
            if (steps % 10000 == 0) log.debug("Steps so far: " + steps); 
            
            // remove the element from the worklist
            InterWorkListElement element = this.workList.removeNext();

            // extract information from the element
            CfgNode node = element.getCfgNode();
            Context context = element.getContext();

            //debug("  " + node.toString() + " (" + node.getOrigLineno() + ")");

            // get incoming value at node n (you need to understand the PHI table :)
            InterAnalysisNode analysisNode = (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(node); 
            LatticeElement inValue = analysisNode.getPhiValue(context);
            if (inValue == null) {
                throw new RuntimeException("SNH");
            }

            try {
            
            // distinguish between various types of CFG nodes
            if (node instanceof CfgNodeCall) {
                
                CfgNodeCall callNode = (CfgNodeCall) node;
                
                //System.out.println("Call: " + callNode.getFunctionNamePlace());
                //System.out.println("Line: " + node.getOrigLineno());

                // get necessary function information (= called function)
                TacFunction function = callNode.getCallee();
                CfgNodeCallRet callRet = (CfgNodeCallRet) node.getOutEdge(0).getDest();

                if (function == null) {
                    // callee could not be determined yet;
                    // the search for a function summary doesn't make
                    // sense; simply go on to the return node;
                    // the concrete analysis is responsible for handling
                    // calls to unknown functions in the transfer functions
                    // for CallPrep and CallRet

                    // note: even though calls to unknown functions will be
                    // replaced with a special cfg node at the end of tac conversion,
                    // this case might still occur *during* tac conversion
                    // (especially during include file resolution)
                    
                    propagate(context, inValue, callRet);
                    continue;
                }
                
                Cfg functionCfg = function.getCfg();

                //System.out.println("CALLING: " + function.getName());
                //System.out.println("NAME_IS: " + callNode.getFunctionNamePlace());
                
                CfgNode exitNode = functionCfg.getTail();
                // the tail of the function's CFG has to be an exit node
                if (!(exitNode instanceof CfgNodeExit)) {
                    throw new RuntimeException("SNH");
                }
                
                Context propagationContext = this.getPropagationContext(callNode, context);
                
                // look if the exit node's PHI map has an entry under the context
                // resulting from this call
                InterAnalysisNode exitAnalysisNode = (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(exitNode);
                if (exitAnalysisNode == null) {
                    // this can only mean that there is no way to reach the
                    // function's natural exit node, i.e. there is something like
                    // die() on each path to the natural exit node; in this
                    // case, we simply enter the function; this can lead to
                    // redundant computations, but it is simpler than a
                    // special, more efficient treatment of this rare case
                    CfgNode entryNode = functionCfg.getHead();
                    propagate(propagationContext, inValue, entryNode);
                    continue;
                }
                
                LatticeElement exitInValue = exitAnalysisNode.getPhiValue(propagationContext);
                
                if (this.useSummaries() && exitInValue != null) {
                    
                    // previously computed function summary can be used;
                    // determine successor node (unique) of this call node
                    CfgEdge[] outEdges = callNode.getOutEdges();
                    CfgNode succ = outEdges[0].getDest();
                    propagate(context, exitInValue, succ);
                    
                } else {
                    
                    // there is no function summary yet (or we don't want to
                    // use summaries)

                    // necessary for call-string analyses
                    // EFF: think about additional conditions to add here
                    if ((this.analysisType instanceof CSAnalysis) && exitInValue != null) {
                        this.workList.add(exitNode, propagationContext);
                    }
                    
                    // there is no function summary yet (or we don't want to
                    // use summaries), so compute it now by entering the function
                    CfgNode entryNode = functionCfg.getHead();
                    propagate(propagationContext, inValue, entryNode);
                }
                
            // calls to a builtin function are simply treated by invoking
            // the corresponding transfer function; covered by the catch-all below
            //} else if (node instanceof CfgNodeCallBuiltin) {
                
            } else if (node instanceof CfgNodeExit) {

                CfgNodeExit exitNode = (CfgNodeExit) node;
                
                // the function to this exit node
                TacFunction function = exitNode.getEnclosingFunction();

                // no need to proceed if this is the exit node of the
                // main function
                if (function == this.mainFunction) {
                    continue;
                }
                
                // the exit node gets a special treatment: pass incoming value
                // in a lazy manner
                // LatticeElement outValue = this.analysisInfo[node.getId()].transfer(inValue);
                LatticeElement outValue = inValue;

                // get targets that we have to return to
                List reverseTargets = this.getReverseTargets(function, context);

                // for each target
                for (Iterator iter = reverseTargets.iterator(); iter.hasNext();) {
                    ReverseTarget reverseTarget = (ReverseTarget) iter.next();
                    
                    // extract target call node
                    CfgNodeCall callNode = reverseTarget.getCallNode();
                    
                    //debug("reverse target: " + callNode.getOrigLineno());

                    // determine successor node (unique) of the call node
                    CfgEdge[] outEdges = callNode.getOutEdges();
                    CfgNodeCallRet callRetNode = (CfgNodeCallRet) outEdges[0].getDest();
                    
                    // determine predecessor node (unique) of the call node
                    CfgNodeCallPrep callPrepNode = callRetNode.getCallPrepNode();
                    
                    // extract set of target contexts
                    Set contextSet = reverseTarget.getContexts();
                    for (Iterator contextIter = contextSet.iterator(); contextIter.hasNext();) {
                        Context targetContext = (Context) contextIter.next();

                        // if the incoming value at the callprep node is undefined, this means
                        // that the analysis hasn't made the call under this context
                        // (can happen for call-string analysis);
                        // => don't propagate
                        //if (this.analysisInfo[callPrepNode.getId()].getPhiValue(targetContext) == null) {
                        InterAnalysisNode callPrepANode = (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(callPrepNode);
                        if (callPrepANode.getPhiValue(targetContext) == null) {
                            // don't propagate
                        } else {
                            // propagate!
                            propagate(targetContext, outValue, callRetNode);
                        }
                    }
                }
                
            } else if (node instanceof CfgNodeIf) {
                
                CfgNodeIf ifNode = (CfgNodeIf) node;
                
                // System.out.println("If node");

                LatticeElement outValue = this.interAnalysisInfo.getAnalysisNode(node).transfer(inValue);
                CfgEdge[] outEdges = node.getOutEdges();

                // try to evaluate the "if" condition
                Boolean eval = this.evalIf(ifNode, inValue);

                if (eval == null) {
                    // static evaluation of if condition failed, continue
                    // analysis along both outgoing edges
                    // System.out.println("Can't evaluate 'if' statically");

                    propagate(context, outValue, outEdges[0].getDest());
                    propagate(context, outValue, outEdges[1].getDest());

                } else if(eval == Boolean.TRUE) {
                    // continue analysis along true edge
                    //System.out.println("evaluated 'if' to true! " + node.getFileName() + ", line " + node.getOrigLineno());
                    //System.out.println(Dumper.makeCfgNodeName(ifNode));
                    propagate(context, outValue, outEdges[1].getDest());
                } else {
                    // continue analysis along false edge
                    //System.out.println("evaluated 'if' to false! line: " + node.getFileName() + ", line " + node.getOrigLineno());
                    //System.out.println(Dumper.makeCfgNodeName(ifNode));
                    propagate(context, outValue, outEdges[0].getDest());
                }

            } else if (node instanceof CfgNodeCallRet) {
                
                // a call return node is to be handled just as a normal node,
                // with the exception that it also needs to know about the
                // current context

                // apply transfer function to incoming value
                InterAnalysisNode aNode = (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(node);
                LatticeElement outValue = aNode.transfer(inValue, context);

                // for each outgoing edge...
                CfgEdge[] outEdges = node.getOutEdges();
                for (int i = 0; i < outEdges.length; i++) {
                    if (outEdges[i] != null) {
                        
                        // determine the successor
                        CfgNode succ = outEdges[i].getDest();

                        // propagate the result of applying the transfer function
                        // to the successor (under the current context) 
                        propagate(context, outValue, succ);
                    }
                }

            } else {
                
                // apply transfer function to incoming value
                LatticeElement outValue;
                outValue = this.interAnalysisInfo.getAnalysisNode(node).transfer(inValue);

                // for each outgoing edge...
                CfgEdge[] outEdges = node.getOutEdges();
                for (int i = 0; i < outEdges.length; i++) {
                    if (outEdges[i] != null) {
                        
                        // determine the successor
                        CfgNode succ = outEdges[i].getDest();

                        // propagate the result of applying the transfer function
                        // to the successor (under the current context)
                        //System.out.println("propagating...: " + node + " -> " + succ);
                        propagate(context, outValue, succ);
                    }
                }
            }
            
            } catch (RuntimeException ex) {
                System.out.println("File:" + node.getFileName() + ", Line: " + node.getOrigLineno());
                throw ex;
            }
        }

        if (!MyOptions.optionB && MyOptions.optionV) {
            System.out.println("Steps total: " + steps);
        }
        // worklist algorithm finished!
    }

// propagate ***********************************************************************
    
    // helper method for analyze();
    // propagates a value under the given context to the target node
    void propagate(Context context, LatticeElement value, CfgNode target) {
        
        //System.out.println("propagating to " + target);
        //value.dump();
        
        // analysis information for the target node
        InterAnalysisNode analysisNode = (InterAnalysisNode) this.interAnalysisInfo.getAnalysisNode(target);
        
        if (analysisNode == null) {
            System.out.println(Dumper.makeCfgNodeName(target));
            throw new RuntimeException("SNH: " + target.getClass());
        }

        if (analysisNode == null) {
            System.out.println(target.getOrigLineno());
            throw new RuntimeException("SNH");
        }
        
        // determine the target's old PHI value
        LatticeElement oldPhiValue = analysisNode.getPhiValue(context);
        if (oldPhiValue == null) {
            // initial value of this analysis
            oldPhiValue = this.initialValue;
        }
        
        // speedup: if incoming value and target value are exactly the same
        // object, then the result certainly can't change
        if (value == oldPhiValue) {
            log.debug("exact match!");
            return;
        }

        // the new PHI value is computed as usual (with lub)
        LatticeElement newPhiValue = this.lattice.lub(value, oldPhiValue);

        // if the PHI value changed...
        if (!oldPhiValue.equals(newPhiValue)) {
            
            /*System.out.println(target);
            System.out.println("old phi value:");
            oldPhiValue.dump();
            System.out.println("new phi value:");
            newPhiValue.dump();*/
            
            // update analysis information
            analysisNode.setPhiValue(context, newPhiValue);

            // add this node (under the current context) to the worklist
            // System.out.println("adding " + target.getId() +  ") to worklist");
            this.workList.add(target, context);
            
        } /*else {

            System.out.println("EQUALS:");
            System.out.println(target);
            System.out.println("old phi value: " + oldPhiValue);
            oldPhiValue.dump();
            System.out.println("new phi value: " + newPhiValue);
            newPhiValue.dump();
        }*/
    }
    

}



