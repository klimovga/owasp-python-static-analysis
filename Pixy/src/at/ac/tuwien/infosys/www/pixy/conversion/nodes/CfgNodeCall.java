package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.*;

// *********************************************************************************
// CfgNodeCall *********************************************************************
// *********************************************************************************

// a function call
public class CfgNodeCall
extends CfgNode {

    // can also be a variable
    private TacPlace functionNamePlace;
    
    private TacFunction callee;
    
    
    // the return variable of the called function
    private Variable retVar;
    // temporary variable to hold the return value
    private Variable tempVar;
    
    
    // a list of actual params (TacActualParam objects)    
    private List<TacActualParam> paramList;
    
    // list of cbr params; description see getCbrParams()
    private List<List<Variable>> cbrParamList;

    // if this is a method call, this field CAN contain the name of the 
    // class that contains the callee method; is null if the name could
    // not be resolved during tac conversion
    private String calleeClassName;
    
    // object upon which this method was invoked;
    // is null if 
    // - this is not a method invocation
    // - or if it is a static one
    // - of it it is a constructor invocation ("new")
    private Variable object;

    
// CONSTRUCTORS ********************************************************************    

    // if you pass "null" for "function", don't forget to call "setFunction" later
    public CfgNodeCall(
        TacPlace functionNamePlace, TacFunction calledFunction, ParseNode node,
        TacFunction enclosingFunction, Variable retVar, TacPlace tempPlace,
        List<TacActualParam> paramList, Variable object) {
        
        super(node);
        this.functionNamePlace = functionNamePlace;
        //this.callee = calledFunction;
        if (calledFunction != null) {
            calledFunction.addCalledFrom(this);
        }
        this.setEnclosingFunction(enclosingFunction);
        
        this.retVar = retVar;
        this.tempVar = (Variable) tempPlace;    // must be a variable
        
        this.paramList = paramList;
        this.cbrParamList = null;
        
        this.calleeClassName = null;
        this.object = object;

    }

// GET *****************************************************************************

    public TacFunction getCallee() {
        return this.callee;
    }

    public TacPlace getFunctionNamePlace() {
        return this.functionNamePlace;
    }

    public List<Variable> getVariables() {
        // only the params are relevant for globals replacement
        List<Variable> retMe = new LinkedList<Variable>();
        for (Iterator iter = this.paramList.iterator(); iter.hasNext();) {
            TacActualParam param = (TacActualParam) iter.next();
            TacPlace paramPlace = param.getPlace();
            if (paramPlace instanceof Variable) {
                retMe.add((Variable) paramPlace);
            } else {
                retMe.add(null);
            }
        }
        return retMe;
    }
    
    public Variable getRetVar() {
        return this.retVar;
    }
    
    public Variable getTempVar() {
        return this.tempVar;
    }
    
    public List<TacActualParam> getParamList() {
        return this.paramList;
    }
    
    // returns a list consisting of two-element-lists consisting of
    // (actual cbr-param, formal cbr-param) (Variable objects)
    public List getCbrParams() {
    
        if (this.cbrParamList != null) {
            return this.cbrParamList;
        }
        
        List actualParams = this.paramList;
        List formalParams = this.getCallee().getParams();
        
        this.cbrParamList = new LinkedList<List<Variable>>();
        
        Iterator actualIter = actualParams.iterator();
        Iterator formalIter = formalParams.iterator();
        
        while (actualIter.hasNext()) {
            
            TacActualParam actualParam = (TacActualParam) actualIter.next();
            TacFormalParam formalParam = (TacFormalParam) formalIter.next();
            
            // if this is a cbr-param...
            if (actualParam.isReference() || formalParam.isReference()) {

                // the actual part of a cbr-param must always be a variable
                if (!(actualParam.getPlace() instanceof Variable)) {
                    throw new RuntimeException("Error in the PHP file!");
                }
                
                Variable actualVar = (Variable) actualParam.getPlace();
                Variable formalVar = formalParam.getVariable();
                                
                // check for unsupported features;
                // none of the variables must be an array or etc.;
                // in such a case, ignore it and continue with the next cbr-param
                boolean supported = AliasAnalysis.isSupported(
                        formalVar, actualVar, true, this.getOrigLineno());
                
                if (!supported) {
                    continue;
                }
                
                List<Variable> pairList = new LinkedList<Variable>();
                pairList.add(actualVar);
                pairList.add(formalVar);
                cbrParamList.add(pairList);
            }
        }

        return cbrParamList;
    }
    
    public String getCalleeClassName() {
        return this.calleeClassName;
    }

    public Variable getObject() {
        return this.object;
    }

// SET *****************************************************************************
    
    public void replaceVariable(int index, Variable replacement) {
        TacActualParam param = (TacActualParam) this.paramList.get(index);
        param.setPlace(replacement);
    }
    
    public void setCallee(TacFunction function) {
        this.callee = function;
        function.addCalledFrom(this);
    }
    
    public void setRetVar(Variable retVar) {
        this.retVar = retVar;
    }
    
    public void setCalleeClassName(String s) {
        this.calleeClassName = s;
    }

}

