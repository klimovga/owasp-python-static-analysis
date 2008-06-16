package at.ac.tuwien.infosys.www.pixy.analysis.alias.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

public class AliasTfCallPrep 
extends TransferFunction {

    private List cbrParams;
    private TacFunction caller;
    private AliasAnalysis aliasAnalysis;
    private CfgNode cfgNode;
    
//  *********************************************************************************    
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************     

    public AliasTfCallPrep(TacFunction caller, AliasAnalysis aliasAnalysis, CfgNodeCallPrep cfgNode) {
        
        this.cfgNode = cfgNode;
        this.cbrParams = cfgNode.getCbrParams();
        this.caller = caller;
        this.aliasAnalysis = aliasAnalysis;
    }

//  *********************************************************************************    
//  OTHER ***************************************************************************
//  *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX) {
        
        AliasLatticeElement in = (AliasLatticeElement) inX;
        AliasLatticeElement out = new AliasLatticeElement(in);

        // see alias analysis tutorial for an explanation how this works
        
        if (!this.cbrParams.isEmpty()) {

            // note: alias analysis does not have to care about default params
            
            // note: at this point, it should already be ensured that there
            // are at least as many formal params as actual params
            
            SymbolTable placeHolderSymTab = new SymbolTable("_placeHolder");
            // placeholder -> real formal
            Map<Variable,Variable> replacements = new HashMap<Variable,Variable>();  
            
            // for all cbr-params...
            for (Iterator iter = this.cbrParams.iterator(); iter.hasNext(); ) {
                
                List pairList = (List) iter.next();
                Iterator pairListIter = pairList.iterator();
                Variable actualVar = (Variable) pairListIter.next();
                Variable formalVar = (Variable) pairListIter.next();
                
                Variable formalPlaceHolder = 
                    new Variable(formalVar.getName(), placeHolderSymTab);
                replacements.put(formalPlaceHolder, formalVar);
                
                // add the formal's placeholder to the actual's must-alias-group
                out.addToGroup(formalPlaceHolder, (Variable) actualVar);
                
                // see the function in MayAliases.java for an explanation
                out.createAdjustedPairCopies(actualVar, formalPlaceHolder);
            }

            // remove all local variables that belong to the symbol table of the
            // caller; shortcut: if the caller is main, we don't have to do
            // this (since there are no real local variables in the main function) 
            SymbolTable callerSymTab = this.caller.getSymbolTable();
            if (!callerSymTab.isMain()) {
                out.removeVariables(callerSymTab);
            }

            // replace the placeholders by the formals of the callee
            out.replace(replacements);
            
        } else {
            // there are no cbr params; hence, we can simply remove
            // all local variables of the caller
            // (note: at this point, the only locals that can appear
            // in the analysis info are those of the caller, so this
            // reduces to removing all local variables)
            out.removeLocals();
        }
        
        // recycle
        out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);
        
        return out;
    }

}
