package at.ac.tuwien.infosys.www.pixy.sanit;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import org.apache.log4j.Logger;

// helper class responsible for performing transductions
// using FSA Utilities
public class FSAUtils {
    protected static Logger log = Logger.getLogger(FSAUtils.class.getName());

    private static String mohri = MyOptions.fsa_home + "/Examples/MohriSproat96/ops.pl";
    
    public static FSAAutomaton reg_replace(FSAAutomaton phpPatternAuto, 
            FSAAutomaton replaceAuto, FSAAutomaton subjectAuto, boolean preg,
            CfgNode cfgNode) {

        // get a finite string from the pattern
        List<String> finitePattern = phpPatternAuto.getFiniteString();
        if (finitePattern == null) {
            // the pattern is not a finite string, not supported
            return FSAAutomaton.makeAnyString();
        }
        if (finitePattern.isEmpty()) {
            // the pattern is empty for some reason;
            // e.g., happens if the $pattern argument to preg_replace is an array
            // => be conservative
            return FSAAutomaton.makeAnyString();
        }
        
        // convert the PHP pattern into a prolog regexp
        boolean approximate = false;
        FSAAutomaton prologPatternAuto = null;
        try {
            prologPatternAuto = FSAAutomaton.convertPhpRegex(finitePattern, preg);
        } catch (UnsupportedRegexException e) {
            // if the regex is not supported yet: just return .*
            log.error("Unsupported regexp: '" + cfgNode.getLoc() +"'");
            approximate = true;
        } catch (Exception e) {
            // if anything else goes wrong: 
            // - also return .*
            // - but generate a different warning
            log.error("Error in regexp conversion at line " + cfgNode.getLoc() + "'", e);
            approximate = true;
        }

        FSAAutomaton retMe;
        if (approximate) {
            retMe = FSAAutomaton.makeAnyString();
        } else {
            String patternFile = prologPatternAuto.toFile("temp1.auto");
            String replaceFile = replaceAuto.toFile("temp2.auto");
            String subjectFile = subjectAuto.toFile("temp3.auto");
            
            String c = MyOptions.fsa_home + "/" +
                "fsa -aux "+mohri+" -r compose(file('"+subjectFile+"'),replace(file('"+patternFile+"'),file('"+replaceFile+"'))) ";
            String autoString = Utils.exec(c);
            retMe = new FSAAutomaton(autoString);
            
            // projection to the output side (turning a transducer into a recognizer)
            retMe = retMe.projectOut();
            
//            System.out.println(retMe.getString());
//            Utils.quickDot(retMe.toDot());
//            Utils.e();

        }

        
        return retMe;

    }
    
    public static FSAAutomaton str_replace(FSAAutomaton searchAuto, 
            FSAAutomaton replaceAuto, FSAAutomaton subjectAuto, CfgNode cfgNode) {
        
        // current approximation:
        // if the search automaton does not encode a finite string, 
        // we use .* for the replace automaton;
        // alternatively (and more precisely), we could also use the following for
        // the replace automaton:
        // replace = union(replace, search)
        // which covers the possibility that something that matches the search does
        // not have to be replaced in every case
        if (searchAuto.getFiniteString() == null) {
            log.warn("Warning: search automaton is not finite!" + "- " + cfgNode.getLoc());
            replaceAuto = FSAAutomaton.makeAnyString();
        }

        String searchFile = searchAuto.toFile("temp1.auto");
        String replaceFile = replaceAuto.toFile("temp2.auto");
        String subjectFile = subjectAuto.toFile("temp3.auto");
        
        String c = MyOptions.fsa_home + "/" +
        "fsa -aux "+mohri+" -r compose(file('"+subjectFile+"'),replace(file('"+searchFile+"'),file('"+replaceFile+"'))) ";
        
        String autoString = Utils.exec(c);
        
        FSAAutomaton retMe = new FSAAutomaton(autoString);
        
        // projection to the output side (turning a transducer into a recognizer)
        retMe = retMe.projectOut();

//        System.out.println(c);
//        System.out.println(autoString);
//        System.out.println(retMe.getString());
//        if (true) throw new RuntimeException();
        
        return retMe;
        
    }
    
    public static FSAAutomaton addslashes(FSAAutomaton subjectAuto, CfgNode cfgNode) {
        
        /*
        FSAAutomaton searchAuto = FSAAutomaton.makeString("b");
        FSAAutomaton replaceAuto = FSAAutomaton.makeString("bb");
        subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
        
        //System.out.println(searchAuto.getString());
        //System.out.println(replaceAuto.getString());
        //System.out.println(subjectAuto.getString());
        if (true) throw new RuntimeException();
        */
        
        /*
        // ' -> \'
        searchAuto = FSAAutomaton.makeString("q");
        replaceAuto = FSAAutomaton.makeString("bq");
        subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
        */

        
        // the easy way: addslashes is the same as applying str_replace
        // several times:
        // \ -> \\
        FSAAutomaton searchAuto = FSAAutomaton.makeString("\\");
        FSAAutomaton replaceAuto = FSAAutomaton.makeString("\\\\");
        subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
        // ' -> \'
        searchAuto = FSAAutomaton.makeString("'");
        replaceAuto = FSAAutomaton.makeString("\\'");
        subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
        // " -> \"
        searchAuto = FSAAutomaton.makeString("\"");
        replaceAuto = FSAAutomaton.makeString("\\\"");
        subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
        
        return subjectAuto;
        
        /*
        String subjectFile = subjectAuto.toFile("temp1.auto");
        String transducerFile = FSAAutomaton.addslashes().toFile("temp2.auto");
        
        String c = MyOptions.fsa_home + "/" +
        "fsa -aux "+mohri+" -r compose(file('"+subjectFile+"'),file('"+transducerFile+"')) ";
        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);
        
        // projection to the output side (turning a transducer into a recognizer)
        retMe = retMe.projectOut();
        
        return retMe;
        */
        
    }
    

    
}
