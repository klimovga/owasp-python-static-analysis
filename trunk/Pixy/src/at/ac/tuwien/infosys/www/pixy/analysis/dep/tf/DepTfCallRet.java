package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepSet;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallRet;

public class DepTfCallRet
extends TransferFunction {

    private InterAnalysisNode analysisNodeAtCallPrep;
    private TacFunction caller;
    private TacFunction callee;
    private CfgNodeCallPrep prepNode;
    private CfgNodeCallRet retNode;
    private AliasAnalysis aliasAnalysis;
    
    // contains the set of global-likes that have been modified by the callee 
    private Set<TacPlace> calleeMod;
    
    // call-by-reference parameter pairs
    private List cbrParams;
    
    // local variables of the calling function
    private Collection localCallerVars;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    public DepTfCallRet(
            InterAnalysisNode analysisNodeAtCallPrep,
            TacFunction caller, 
            TacFunction callee,
            CfgNodeCallPrep prepNode,
            CfgNodeCallRet retNode,
            AliasAnalysis aliasAnalysis,
            Set<TacPlace> calleeMod) {
        
        this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
        this.caller = caller;
        this.callee = callee;
        
        // call-by-reference parameter pairs
        this.cbrParams = prepNode.getCbrParams();
        
        // local variables of the calling function
        this.localCallerVars = caller.getLocals();
        
        this.aliasAnalysis = aliasAnalysis;
        this.calleeMod = calleeMod;
        
        this.prepNode = prepNode;
        this.retNode = retNode;

    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX, Context context) {
        
        // lattice element entering the call prep node under the current
        // context
        DepLatticeElement origInfo = 
            (DepLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);
        
        // lattice element coming in from the callee (= base for interprocedural info);
        // still contains the callee's locals
        DepLatticeElement calleeIn = (DepLatticeElement) inX;

        // start only with default mappings
        DepLatticeElement outInfo = new DepLatticeElement();

        // contains variables that have been tagged as visited
        Set<Variable> visitedVars = new HashSet<Variable>();
        
        // copy mappings of "global-like" places from calleeIn to outInfo
        // ("global-like": globals, superglobals, and constants)
        if (this.calleeMod == null) {
            outInfo.copyGlobalLike(calleeIn);
        } else {
            // if we have MOD-info for the callee, use it!
            outInfo.copyGlobalLike(calleeIn, origInfo, this.calleeMod);
        }

        
        // LOCAL VARIABLES *************
        
        // no need to do the steps below if the caller is main: 
        // its local variables are global variables; 
        // instead, do the following:
        if (this.caller.isMain()) {

            // don't forget the main function's temporaries
            outInfo.copyMainTemporaries(origInfo);
            
            outInfo.handleReturnValue(this.retNode/*, calleeIn*/);
            
            return outInfo;
        }

        // initialize local variables with the mappings at call-time
        outInfo.copyLocals(origInfo);
        
        // MUST WITH GLOBALS
        
        // for all local variables of the calling function
        for (Iterator iter = localCallerVars.iterator(); iter.hasNext();) {
            Variable localCallerVar = (Variable) iter.next();
            
            // an arbitrary global must-alias of this local
            Variable globalMustAlias = this.aliasAnalysis.getGlobalMustAlias(localCallerVar, this.prepNode);
            if (globalMustAlias == null) {
                continue;
            }
            
            // the shadow of this global must-alias
            Variable globalMustAliasShadow = this.callee.getSymbolTable().getGShadow(globalMustAlias);
            if (globalMustAliasShadow == null) {
                System.out.println(globalMustAlias + " is global? " + globalMustAlias.isGlobal());
                throw new RuntimeException("SNH: " + globalMustAlias);
            }
            
            // set & mark
            outInfo.setLocal(
                    localCallerVar, 
                    calleeIn.getDep(globalMustAliasShadow), 
                    calleeIn.getArrayLabel(globalMustAliasShadow));
            visitedVars.add(localCallerVar);
            
        }
        
        
        // MUST WITH FORMALS

        // for each call-by-reference parameter pair
        for (Iterator iter = this.cbrParams.iterator(); iter.hasNext();) {
            
            List paramPair = (List) iter.next();
            Iterator paramPairIter = paramPair.iterator();
            Variable actualVar = (Variable) paramPairIter.next();
            Variable formalVar = (Variable) paramPairIter.next();
            
            // local must-aliases of the actual parameter (including trivial ones,
            // so this set contains at least one element)
            Set localMustAliases = this.aliasAnalysis.getLocalMustAliases(actualVar, this.prepNode);
            
            for (Iterator lmaIter = localMustAliases.iterator(); lmaIter.hasNext();) {
                Variable localMustAlias = (Variable) lmaIter.next();
                
                // no need to handle visited variables again
                if (visitedVars.contains(localMustAlias)) {
                    continue;
                }
                
                // the formal parameter's f-shadow
                Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);
                
                // set & mark
                outInfo.setLocal(
                        localMustAlias, 
                        calleeIn.getDep(fShadow),
                        calleeIn.getArrayLabel(fShadow));
                visitedVars.add(localMustAlias);
                
            }
        }
        
        
        // MAY WITH GLOBALS
        
        // for each local variable that was not visited yet
        for (Iterator iter = localCallerVars.iterator(); iter.hasNext();) {
            Variable localCallerVar = (Variable) iter.next();
            
            if (visitedVars.contains(localCallerVar)) {
                continue;
            }
            
            // global may-aliases of this variable (at the time of call)
            Set globalMayAliases = this.aliasAnalysis.getGlobalMayAliases(localCallerVar, this.prepNode);
            
            // if there are no such aliases: nothing to do
            if (globalMayAliases.isEmpty()) {
                continue;
            }
            
            // initialize this variable's taint/arrayLabel with its original taint/arrayLabel
            DepSet computedTaint = origInfo.getDep(localCallerVar);
            DepSet computedArrayLabel = origInfo.getArrayLabel(localCallerVar);
            
            // for all these global may-aliases...
            for (Iterator gmaIter = globalMayAliases.iterator(); gmaIter.hasNext();) {
                Variable globalMayAlias = (Variable) gmaIter.next();
                
                // its g-shadow
                Variable globalMayAliasShadow = this.callee.getSymbolTable().getGShadow(globalMayAlias);
                
                // the shadow's taint/label (from flowback-info)
                DepSet shadowTaint = calleeIn.getDep(globalMayAliasShadow);
                DepSet shadowArrayLabel = calleeIn.getArrayLabel(globalMayAliasShadow);
                
                // lub
                computedTaint = DepLatticeElement.lub(computedTaint, shadowTaint);
                computedArrayLabel = DepLatticeElement.lub(computedArrayLabel, shadowArrayLabel);
                
            }
            
            // set the local's taint/label to the computed taint/label in outInfo
            outInfo.setLocal(localCallerVar, computedTaint, computedArrayLabel);
            
            // DON'T mark it as visited
        }

        
        // MAY WITH FORMALS
        
        // for each call-by-reference parameter pair
        for (Iterator iter = this.cbrParams.iterator(); iter.hasNext();) {
            
            List paramPair = (List) iter.next();
            Iterator paramPairIter = paramPair.iterator();
            Variable actualVar = (Variable) paramPairIter.next();
            Variable formalVar = (Variable) paramPairIter.next();
            
            // local may-aliases of the actual parameter (at call-time)
            Set localMayAliases = this.aliasAnalysis.getLocalMayAliases(actualVar, this.prepNode);
            
            // for each such may-alias that was not visited yet
            for (Iterator lmaIter = localMayAliases.iterator(); lmaIter.hasNext();) {
                Variable localMayAlias = (Variable) lmaIter.next();
             
                if (visitedVars.contains(localMayAlias)) {
                    continue;
                }
                
                // the current taint/label of the local may-alias in output-info
                DepSet localTaint = outInfo.getDep(localMayAlias);
                DepSet localArrayLabel = outInfo.getArrayLabel(localMayAlias);
                
                // the formal parameter's f-shadow
                Variable fShadow = this.callee.getSymbolTable().getFShadow(formalVar);
                
                // the shadow's taint/label (from flowback-info)
                DepSet shadowTaint = calleeIn.getDep(fShadow);
                DepSet shadowArrayLabel = calleeIn.getArrayLabel(fShadow);
                
                // lub & set
                DepSet newTaint = DepLatticeElement.lub(localTaint, shadowTaint);
                DepSet newArrayLabel = DepLatticeElement.lub(localArrayLabel, shadowArrayLabel);
                
                outInfo.setLocal(localMayAlias, newTaint, newArrayLabel);
            }
        }


        outInfo.handleReturnValue(this.retNode/*, calleeIn*/);
        
        return outInfo;
        
    }
    
    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public LatticeElement transfer(LatticeElement inX) {
        throw new RuntimeException("SNH");
    }
    
}



