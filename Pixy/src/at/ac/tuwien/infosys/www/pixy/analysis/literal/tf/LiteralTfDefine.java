package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeDefine;
import org.apache.log4j.Logger;

public class LiteralTfDefine extends TransferFunction {

    protected static Logger log = Logger.getLogger(LiteralTfDefine.class.getName());

    private TacPlace setMe;
    private TacPlace setTo;
    private TacPlace caseInsensitive;
    
    private ConstantsTable constantsTable;
    private CfgNode cfgNode;
    
// *********************************************************************************    
// CONSTRUCTORS ********************************************************************
// *********************************************************************************     

    public LiteralTfDefine(ConstantsTable table, CfgNodeDefine cfgNode) {
        this.setMe = cfgNode.getSetMe();
        this.setTo = cfgNode.getSetTo();
        this.caseInsensitive = cfgNode.getCaseInsensitive();
        this.constantsTable = table;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************  

    public LatticeElement transfer(LatticeElement inX) {
        
        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // retrieve the literal of the constant to be set
        // (for example: define($foo, 'bla') with $foo == ABC,
        // => constantLit == ABC
        Literal constantLit = in.getLiteral(this.setMe);
        
        // if we can't resolve the constant that is to be set, we can't do
        // anything; example: define($foo, 'bla', true) with unknown $foo;  
        // to be precise, we would have to set all constants to
        // TOP (or better: those that are still undefined), but since this
        // case is rather seldom, we just issue a warning;
        if (constantLit == Literal.TOP) {
            log.warn("Warning: can't resolve constant to be defined" +
                    "- " + cfgNode.getFileName() + ":" + cfgNode.getOrigLineno());
            return out;
        }
        
        // retrieve the literal that the constant shall be set to
        Literal valueLit = in.getLiteral(this.setTo);
        
        // determine the (boolean) literal of the case flag
        Literal caseLit = in.getLiteral(this.caseInsensitive).getBoolValueLiteral();
        
        if (caseLit == Literal.TRUE) {
            // define insensitive constant
            
            // all constants in setMe's insensitivity group have to be set
            List insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
            if (insensGroup != null) {
                for (Iterator iter = insensGroup.iterator(); iter.hasNext();) {
                    Constant constant = (Constant) iter.next();
                    out.defineConstant(constant, valueLit);
                }
            } else {
                // this case happens when the user defines a constant which is never
                // used (not even in a case-insensitive way);
                log.warn("Warning: a constant is defined, but never used" +
                        "- name:    " + constantLit.toString() +
                        "- defined: " + this.cfgNode.getLoc());

            }

        } else if (caseLit == Literal.FALSE) {
            // define sensitive constant
            
            Constant constant = this.constantsTable.getConstant(constantLit.toString());
            if (constant == null) {
                // happens if the constant being defined is never used
                log.warn( "Warning: a constant is defined, but never used" +
                        "- name:    " + constantLit.toString() +
                        "- defined: " + this.cfgNode.getLoc());
            } else {
                out.defineConstant(constant, valueLit);
            }

        } else if (caseLit == Literal.TOP) {
            // we don't know the exact value of this flag;
            // hence, we perform a strong update for the immediate constant in
            // question, and a weak update for all constants in its insensitivity group
            
            Constant constant = this.constantsTable.getConstant(constantLit.toString());
            if (constant == null) {
                // happens if the constant being defined is never used
                log.warn("Warning: a constant is defined, but never used" +
                        "- name:    " + constantLit.toString() +
                        "- defined: " + this.cfgNode.getLoc());
            } else {
                out.defineConstant(constant, valueLit);
            }

            // all constants in setMe's insensitivity group have to undergo a weak update
            // (except setMe itself)
            List insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
            if (insensGroup != null) {
                for (Iterator iter = insensGroup.iterator(); iter.hasNext();) {
                    Constant weakConstant = (Constant) iter.next();
                    if (!weakConstant.equals(constant)) {
                        out.defineConstantWeak(weakConstant, valueLit);
                    }
                }
            } else {
                // this case happens when the user defines a constant which is never
                // used (not even in a case-insensitive way);
                log.warn("Warning: a constant is defined, but never used" +
                        "- name:    " + constantLit.toString() +
                        "- defined: " + this.cfgNode.getLoc());
            }

            
        } else {
            log.error("Unknown literal: '" + caseLit + "'");
            throw new RuntimeException("SNH");
        }
        
        return out;
    }


}
