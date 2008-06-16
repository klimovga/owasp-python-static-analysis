package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CSContext;

// maps from position to position for a certain call node
public class ConnectorFunction {

    // CSContext -> CSContext
    private Map<CSContext,CSContext> pos2pos;
    
    // reverse mapping of pos2pos:
    // CSContext -> Set of CSContexts
    private Map<CSContext,Set<CSContext>> reverse;
    
    // creates an empty connector function
    public ConnectorFunction() {
        this.pos2pos = new HashMap<CSContext,CSContext>();
        this.reverse = new HashMap<CSContext,Set<CSContext>>();
    }
    
    // adds the given mapping
    public void add(int from, int to) {
        
        CSContext fromInt = new CSContext(from);
        CSContext toInt = new CSContext(to);
        
        this.pos2pos.put(fromInt, toInt);
        
        // maintain reverse mapping
        
        Set<CSContext> reverseSet = this.reverse.get(toInt);
        if (reverseSet == null) {
            // there was no such reverse mapping:
            // create it together with a new set
            reverseSet = new HashSet<CSContext>();
            reverseSet.add(fromInt);
            this.reverse.put(toInt, reverseSet);
        } else {
            // add to already existing reverse mapping set
            reverseSet.add(fromInt);
        }
    }
    
    // applies this connector function to the given input value
    public CSContext apply(int input) {
        CSContext output = (CSContext) this.pos2pos.get(new CSContext(input));
        return output;
    }
    
    // reverse application: returns a set of inputs (CSContext's) for the given output
    // (might be null if there is no such output)
    public Set<CSContext> reverseApply(int output) {
        return this.reverse.get(new CSContext(output));
    }
    
    public String toString() {
        if (this.pos2pos.isEmpty()) {
            return "<empty>";
        }
        StringBuilder myString = new StringBuilder();
        for (Iterator iter = this.pos2pos.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            CSContext from = (CSContext) entry.getKey();
            CSContext to = (CSContext) entry.getValue();
            myString.append(from);
            myString.append(" -> ");
            myString.append(to);
            myString.append(System.getProperty("line.separator"));
        }
        return myString.substring(0, myString.length() - 1);
    }
}
