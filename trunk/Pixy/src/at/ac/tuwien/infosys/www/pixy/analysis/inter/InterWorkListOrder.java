package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

// computes a reverse postorder for the whole, interprocedural cfg;
// currently only works for call-string analysis
public class InterWorkListOrder {
    
    // this is what we want to compute: a mapping of interprocedural
    // worklist elements to some number (order)
    private Map<InterWorkListElement,Integer> order;
    
//  ********************************************************************************
    
    public InterWorkListOrder(TacConverter tac, ConnectorComputation cc) {
        
        this.order = new HashMap<InterWorkListElement,Integer>();
        
        TacFunction mainFunction = tac.getMainFunction();
        CfgNode startNode = mainFunction.getCfg().getHead();
        
        Map<TacFunction,ECS> function2ECS = cc.getFunction2ECS();
        ECS mainECS = function2ECS.get(mainFunction);
        if (mainECS.size() != 1) {
            throw new RuntimeException("SNH");
        }

        InterWorkListElement start = new InterWorkListElement(startNode, new CSContext(0)); 
        LinkedList<InterWorkListElement> postorder = this.getPostorder(start, cc);
        
        // get *reverse* postorder
        ListIterator iter = postorder.listIterator(postorder.size());
        int i = 0;
        while (iter.hasPrevious()) {
            InterWorkListElement iwle = (InterWorkListElement) iter.previous();
            //CfgNode cfgNode = iwle.getCfgNode();
            //System.out.println(cfgNode.toString());
            this.order.put(iwle, i);
            i++;
        }
    }

//  ********************************************************************************
    
    // non-recursive postorder
    private LinkedList<InterWorkListElement> getPostorder(
            InterWorkListElement start, ConnectorComputation cc) {

        // this is what we want to compute
        LinkedList<InterWorkListElement> postorder = new LinkedList<InterWorkListElement>();
        
        // auxiliary stack and visited set
        LinkedList<InterWorkListElement> stack = new LinkedList<InterWorkListElement>();
        Set<InterWorkListElement> visited = new HashSet<InterWorkListElement>();

        // begin with start element
        stack.add(start);

        // how it works:
        // while there is something on the stack:
        // - mark the top stack element as visited
        // - try to get an unvisited successor of this element
        // - if there is such a successor: push it on the stack and continue
        // - else: pop the stack and add the popped element to the postorder list
        while (!stack.isEmpty()) {
            
            // mark the top stack element as visited
            InterWorkListElement element = stack.getLast();
            visited.add(element);
            
            // interior of this element
            CfgNode cfgNode = element.getCfgNode();
            CSContext context = (CSContext) element.getContext();
            
            // we will try to get an unvisited successor element
            InterWorkListElement nextElement = null;
            
            if (cfgNode instanceof CfgNodeCall) {
                
                // in case of a call node, we have to distinguish between
                // unknown calls (no callee available) and known calls
                
                CfgNodeCall callNode = (CfgNodeCall) cfgNode;
                TacFunction callee = callNode.getCallee();
                if (callee == null) {
                    
                    // for unknown calls:                    
                    // simply move on to the callret node; context stays the same
                    
                    CfgNode retNode = callNode.getSuccessor(0);
                    nextElement = new InterWorkListElement(retNode, context);
                    
                    if (visited.contains(nextElement)) {
                        nextElement = null;
                    }
                    
                } else {
                    
                    // for normal calls:
                    // enter function under corresponding context
                    
                    CfgNodeEntry entryNode = (CfgNodeEntry) callee.getCfg().getHead();
                    Context propagationContext = cc.getTargetContext(callNode, context.getPosition());
                    if (propagationContext == null) {
                        throw new RuntimeException("SNH: " + callNode.getLoc());
                    }
                    nextElement = new InterWorkListElement(entryNode, propagationContext);
                    
                    if (visited.contains(nextElement)) {
                        nextElement = null;
                    }

                }
                
            } else if (cfgNode instanceof CfgNodeExit) {
                
                CfgNodeExit exitNode = (CfgNodeExit) cfgNode;
                TacFunction exitedFunction = exitNode.getEnclosingFunction();

                // only proceed if this is not the exit node of the main function
                if (!exitedFunction.isMain()) {
                    
                    // an exit node can have several "reverse targets";
                    // a reverse target consists of one call node and one or more contexts
                    
                    Iterator<ReverseTarget> revTargetsIter = cc.getReverseTargets(exitedFunction, context.getPosition()).iterator();
                    while ((nextElement == null) && revTargetsIter.hasNext()) {

                        ReverseTarget revTarget = revTargetsIter.next();
                        CfgNodeCall revCall = revTarget.getCallNode();
                        CfgNode revRet = revCall.getSuccessor(0);
                        Iterator<? extends Context> reverseContextsIter = revTarget.getContexts().iterator();
                        
                        while ((nextElement == null) && reverseContextsIter.hasNext()) {
                            
                            Context reverseContext = reverseContextsIter.next();
                            nextElement = new InterWorkListElement(revRet, reverseContext);
                            
                            if (visited.contains(nextElement)) {
                                // try the next one
                                nextElement = null;
                            } else {
                                // found it!
                            }
                        }
                    }
                }
                
            } else {
                
                // handle successors
                for (int i = 0; (i < 2) && (nextElement == null); i++) {
                    CfgEdge outEdge = cfgNode.getOutEdge(i);
                    if (outEdge != null) {
                        CfgNode succNode = outEdge.getDest();
                        nextElement = new InterWorkListElement(succNode, context);
                        if (visited.contains(nextElement)) {
                            // try next one
                            nextElement = null;
                        } else {
                            // found it!
                        }
                    }
                }
            }
            
            if (nextElement == null) {
                // pop from stack and add it to the postorder list
                postorder.add(stack.removeLast());
            } else {
                // push to stack
                stack.add(nextElement);
            }
        }
        
        return postorder;
        
        
        
    }
//  ********************************************************************************
    
    // this was the old, recursive implementation: could make the stack too deep
    
    /*
    private LinkedList<InterWorkListElement> getPostorder(
            InterWorkListElement start, ConnectorComputation cc) {
        LinkedList<InterWorkListElement> postorder = new LinkedList<InterWorkListElement>();
        Set<InterWorkListElement> visited = new HashSet<InterWorkListElement>();
        dfIteratorHelper(postorder, start, visited, cc);
        return postorder;
    }

//  ********************************************************************************
    
    private void dfIteratorHelper(List<InterWorkListElement> postorder, 
            InterWorkListElement element, Set<InterWorkListElement> visited,
            ConnectorComputation cc) {

        // mark this node as visited
        visited.add(element);
        
        CfgNode cfgNode = element.getCfgNode();
        CSContext context = (CSContext) element.getContext();
        
        if (cfgNode instanceof CfgNodeCall) {
            
            CfgNodeCall callNode = (CfgNodeCall) cfgNode;
            TacFunction callee = callNode.getCallee();
            if (callee == null) {
                // simply move on to the callret node
                CfgNode retNode = callNode.getSuccessor(0);
                InterWorkListElement nextElement = new InterWorkListElement(retNode, context);
                if (!visited.contains(nextElement)) {
                    dfIteratorHelper(postorder, nextElement, visited, cc);
                }
            } else {
                CfgNodeEntry entryNode = (CfgNodeEntry) callee.getCfg().getHead();
                Context propagationContext = cc.getTargetContext(callNode, context.getPosition());
                InterWorkListElement nextElement = new InterWorkListElement(entryNode, propagationContext);
                if (!visited.contains(nextElement)) {
                    dfIteratorHelper(postorder, nextElement, visited, cc);
                }
            }
            
        } else if (cfgNode instanceof CfgNodeExit) {
            
            CfgNodeExit exitNode = (CfgNodeExit) cfgNode;
            
            TacFunction exitedFunction = exitNode.getEnclosingFunction();

            // no need to proceed if this is the exit node of the
            // main function
            if (!exitedFunction.isMain()) {
                for (ReverseTarget reverseTarget : cc.getReverseTargets(exitedFunction, context.getPosition())) {
                    CfgNodeCall reverseCall = reverseTarget.getCallNode();
                    CfgNode reverseRet = reverseCall.getSuccessor(0);
                    Set<? extends Context> reverseContexts = reverseTarget.getContexts();
                    for (Context reverseContext : reverseContexts) {
                        InterWorkListElement nextElement = new InterWorkListElement(reverseRet, reverseContext);
                        if (!visited.contains(nextElement)) {
                            dfIteratorHelper(postorder, nextElement, visited, cc);
                        }
                    }
                }
            }
            
        } else {
            // handle successors
            for (int i = 0; i < 2; i++) {
                CfgEdge outEdge = cfgNode.getOutEdge(i);
                if (outEdge != null) {
                    CfgNode succNode = outEdge.getDest();
                    InterWorkListElement nextElement = new InterWorkListElement(succNode, context);
                    if (!visited.contains(nextElement)) {
                        dfIteratorHelper(postorder, nextElement, visited, cc);
                    }
                }
            }
        }
        
        // add it to the postorder list
        postorder.add(element);
    }
    */
    
//  ********************************************************************************

    public Integer getReversePostOrder(InterWorkListElement element) {
        return this.order.get(element);
    }
    

}
