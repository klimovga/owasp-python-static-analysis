package at.ac.tuwien.infosys.www.pixy.conversion;

public abstract class TacPlace {

    // protected int hashCode = 0;

// *********************************************************************************
// GET *****************************************************************************    
// *********************************************************************************
    
    public boolean isVariable() {
        return (this instanceof Variable);
    }

    public boolean isConstant() {
        return (this instanceof Constant);
    }

    public boolean isLiteral() {
        return (this instanceof Literal);
    }

    public Variable getVariable() {
        return ((Variable) this);
    }

    Constant getConstant() {
        return ((Constant) this);
    }

    Literal getLiteral() {
        return ((Literal) this);
    }

    // shortcut methods
    public abstract String toString();
    
// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public abstract boolean equals(Object obj);

    // CAUTION: hashCode has to be reset if any of the significant attributes are changed;
    // should be included in the corresponding methods
    public abstract int hashCode();
    
    /*
    public void resetHashCode() {
        this.hashCode = 0;
    }
    */
    
    
}
