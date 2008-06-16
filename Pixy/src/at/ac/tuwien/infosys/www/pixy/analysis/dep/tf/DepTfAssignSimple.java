package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepSet;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// transfer function for simple assignment nodes
public class DepTfAssignSimple
extends TransferFunction {

    private Variable left;
    private TacPlace right;
    private Set mustAliases;
    private Set mayAliases;
    private CfgNode cfgNode;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    public DepTfAssignSimple(TacPlace left, TacPlace right, 
            Set mustAliases, Set mayAliases, CfgNode cfgNode) {
        
        this.left = (Variable) left;  // must be a variable
        this.right = right;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
        this.cfgNode = cfgNode;

    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX) {

        //System.out.println("assignsimple: " + left + " = " + right);
        
        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // let the lattice element handle the details
        out.assign(left, mustAliases, mayAliases, cfgNode);

        // WORKAROUND for BUG #1083 (ikonv)
        //
        // $x = array();
        // $x[0] = TAINTED;
        // $y = $x;
        //
        // $q = "";
        // $for ($i = 0; $i < count($y); $i++) {
        //   $q = $q . $y[$i];
        // }
        // sink($q); # produces no vulnerability!
        //
        // SOLUTION: not only include dependencies of enclosing array,
        // but also include the elements of right hand side.

        if (right.isVariable() && ((Variable) right).isArray()) {
            DepSet depSet = out.getArrayLabel(left);
            depSet = getElementsDeps(in, right, depSet);
            out.handleElementsDeps(left, depSet);
        }

        return out;
    }

    private DepSet getElementsDeps(DepLatticeElement in, TacPlace rhs, DepSet depSet) {
        if (rhs.isVariable() && ((Variable) rhs).isArray()) {
            Variable rightArray = (Variable) rhs;
            DepSet newDepSet = depSet;
            for (Variable v : rightArray.getElements()) {
                DepSet varDep = in.getDep(v);
                newDepSet = DepSet.lub(newDepSet, varDep);
                getElementsDeps(in, v, newDepSet);
            }

            return newDepSet;
        }

        return depSet;
    }
}
