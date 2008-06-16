package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.*;

public class MayAliasPair {

    // a pair of Variable's;
    // EFF: it might be faster if you implement this explicitly with two variables
    // instead of using a hash set
    private Set<Variable> pair;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    public MayAliasPair(Variable a, Variable b) {
        this.pair = new HashSet<Variable>();
        this.pair.add(a);
        this.pair.add(b);
    }
    
    // clones the given pair
    public MayAliasPair(MayAliasPair orig) {
        this.pair = new HashSet<Variable>();
        Set origPair = orig.getPair();
        for (Iterator iter = origPair.iterator(); iter.hasNext(); ) {
            Variable var = (Variable) iter.next();
            this.pair.add(var);
        }
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    public Set<Variable> getPair() {
        return this.pair;
    }

    public int size() {
        return this.pair.size();
    }
    
    // if the given variable is contained in this group and the other member
    // of the group is a global variable, this global variable is returned;
    // null otherwise
    public Variable getGlobalMayAlias(Variable var) {
        Iterator iter = this.pair.iterator();
        Variable firstElement = (Variable) iter.next();
        Variable secondElement = (Variable) iter.next();
        
        if (firstElement == var) {
            if (secondElement.isGlobal()) {
                return secondElement;
            } else {
                return null;
            }
        } else if (secondElement == var) {
            if (firstElement.isGlobal()) {
                return firstElement;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // if the given variable is contained in this group and the other member
    // of the group is a local variable, this local variable is returned;
    // null otherwise
    public Variable getLocalMayAlias(Variable var) {
        Iterator iter = this.pair.iterator();
        Variable firstElement = (Variable) iter.next();
        Variable secondElement = (Variable) iter.next();
        
        if (firstElement == var) {
            if (secondElement.isLocal()) {
                return secondElement;
            } else {
                return null;
            }
        } else if (secondElement == var) {
            if (firstElement.isLocal()) {
                return firstElement;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    // if the given variable is contained in this pair, the other variable
    // from the pair is returned; null otherwise
    public Variable getMayAlias(Variable var) {
        
        Iterator iter = this.pair.iterator();
        Variable firstElement = (Variable) iter.next();
        Variable secondElement = (Variable) iter.next();
        
        if (firstElement == var) {
            return secondElement;
        } else if (secondElement == var) {
            return firstElement;
        } else {
            return null;
        }
        
    }

    // if this pair contains one local and one global variable, an array containing
    // these variables (in this order) is returned; null otherwise
    public Variable[] getLocalGlobal() {

        Iterator iter = this.pair.iterator();
        Variable firstElement = (Variable) iter.next();
        Variable secondElement = (Variable) iter.next();
        
        if (firstElement.isLocal()) {
            if (secondElement.isGlobal()) {
                Variable[] retMe = {firstElement, secondElement};
                return retMe;
            } else {
                return null;
            }
        } else if (firstElement.isGlobal()) {
            if (secondElement.isLocal()) {
                Variable[] retMe = {secondElement, firstElement};
                return retMe;
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("SNH");
        }

    }
    
//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    // returns true if this pair contains one local and one global variable;
    // false otherwise
    public boolean containsLocalAndGlobal() {
        Iterator iter = this.pair.iterator();
        Variable firstVar = (Variable) iter.next();
        Variable secondVar = (Variable) iter.next();
        if (firstVar.isLocal()) {
            if (secondVar.isGlobal()) {
                return true;
            } else {
                return false;
            }
        } else if (firstVar.isGlobal()) {
            if (secondVar.isLocal()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("SNH");
        }
    }
    
    // returns true if at least one of the contained variables is a local
    // variable, and false otherwise
    public boolean containsLocals() {
        for (Iterator iter = this.pair.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isLocal()) {
                return true;
            }
        }
        return false;
    }

    // returns true if at least one of the contained variables is a local
    // variable, and false otherwise
    public boolean containsGlobals() {
        for (Iterator iter = this.pair.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.isGlobal()) {
                return true;
            }
        }
        return false;
    }
    
    // returns true if at least one of the contained variables belongs to
    // the given symbol table, and false otherwise
    public boolean containsVariables(SymbolTable symTab) {
        for (Iterator iter = this.pair.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            if (var.belongsTo(symTab)) {
                return true;
            }
        }
        return false;
    }

    // replaces "replaceMe" by "replaceBy"; expects "replaceMe" to
    // really appear in this pair
    public void replaceBy(Variable replaceMe, Variable replaceBy) {
        if (!this.pair.remove(replaceMe)) {
            throw new RuntimeException("SNH");
        }
        this.pair.add(replaceBy);
    }

    // expects a map Variable -> Variable (replaceMe -> replaceBy)
    public void replace(Map replacements) {
        Set<Variable> newPair = new HashSet<Variable>(this.pair);
        for (Iterator iter = this.pair.iterator(); iter.hasNext(); ) {
            Variable var = (Variable) iter.next();
            Variable replaceBy = (Variable) replacements.get(var);
            if (replaceBy != null) {
                newPair.remove(var);
                newPair.add(replaceBy);
            }
        }
        this.pair = newPair;
    }

    public boolean contains(Variable var) {
        if (this.pair.contains(var)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean equals(Object obj) {
        
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MayAliasPair)) {
            return false;
        }
        MayAliasPair comp = (MayAliasPair) obj;
        return this.pair.equals(comp.getPair());
    }
    
    public int hashCode() {
        return this.pair.hashCode();
    }
    
}
