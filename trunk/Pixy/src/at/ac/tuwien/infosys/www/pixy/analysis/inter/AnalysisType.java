package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

// functional or call-string analysis
public abstract class AnalysisType {

     protected InterAnalysis enclosedAnalysis;
     
     // returns the context to which interprocedural propagation shall
     // be conducted (used at call nodes)
     public abstract Context getPropagationContext(CfgNodeCall callNode, Context context);
     
     // returns a set of ReverseTarget objects to which interprocedural
     // propagation shall be conducted (used at exit nodes)
     public abstract List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context contextX);
     
     // sets the enclosed analysis
     public void setAnalysis(InterAnalysis enclosedAnalysis) {
         this.enclosedAnalysis = enclosedAnalysis;
     }
     
     // creates an appropriate AnalysisNode
     public abstract InterAnalysisNode makeAnalysisNode(CfgNode cfgNode, TransferFunction tf);
     
     // use function summaries?
     public abstract boolean useSummaries();
     
     public abstract Context initContext(InterAnalysis analysis);

}
