package at.ac.tuwien.infosys.www.pixy.analysis;


public class LatticeElementBottom
extends LatticeElement {

    public static final LatticeElementBottom INSTANCE = new LatticeElementBottom();
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    // singleton class
    private LatticeElementBottom() {
    }
    
    public LatticeElement cloneMe() {
        throw new RuntimeException("SNH");
    }

    public void lub(LatticeElement element) {
        throw new RuntimeException("SNH");
    }

    public boolean structureEquals(Object compX) {
        throw new RuntimeException("SNH");
    }

    public int structureHashCode() {
        throw new RuntimeException("SNH");
    }
    
    public void dump() {
        System.out.println("<bottom>");
    }
    

    /*
    public boolean equals(Object obj) {
        // since this is a singleton class, it can only be equal to itself
        return (obj == LatticeElementBottom.INSTANCE ? true : false);
    }

    public int hashCode() {
        // use the object's "natural" hashcode:
        // this doesn't work
        return (((Object) this).hashCode());
    }
    */

}
