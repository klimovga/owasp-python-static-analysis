package at.ac.tuwien.infosys.www.pixy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphOpNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphSccNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphUninitNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.automaton.Automaton;
import at.ac.tuwien.infosys.www.pixy.automaton.Transition;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;
import org.apache.log4j.Logger;

// extracts strings used in file access functions 
public class FileAnalysis extends DepClient {

    protected static Logger log = Logger.getLogger(DepClient.class.getName());

    public FileAnalysis(DepAnalysis depAnalysis) {
        super(depAnalysis);
    }
    

    public List<Integer> detectVulns() {
        
        log.debug("Performing file analysis");
        
        List<Integer> retMe = new LinkedList<Integer>();
        
        // collect sinks
        List<Sink> sinks = this.collectSinks();

        log.debug("Creating DepGraphs for " + sinks.size() + " sinks...");
        Collection<DepGraph> depGraphs = depAnalysis.getDepGraphs(sinks);
        
        log.debug("File Capab Analysis Output");

        int graphcount = 0;
        for (DepGraph depGraph : depGraphs) {
            graphcount++;

            DepGraph stringGraph = new DepGraph(depGraph);
            DepGraphNormalNode root = depGraph.getRoot();
            CfgNode cfgNode = root.getCfgNode();

            depGraph = null;    // don't touch this one
            
            Automaton auto = this.toAutomaton(stringGraph);
            
            // if we wanted to, we could also report taint values here
            /*
            if (auto.hasDirectlyTaintedTransitions()) {
                System.out.println("directly tainted!");
                tainted = true;
            } else { 
                System.out.println("not tainted");
            } 
            */
            
            String fileName = cfgNode.getFileName();
            if (MyOptions.optionB) {
                fileName = Utils.basename(fileName);
            }
            log.debug("Line:  " + cfgNode.getOrigLineno());
            log.debug("File:  " + fileName);
            log.debug("Graph: file" + graphcount);

            this.dumpDotAuto(auto, "file" + graphcount, MyOptions.graphPath);
        }
        
        // initial sink count and final graph count may differ (e.g., if some sinks
        // are not reachable)
        if (MyOptions.optionV)
            log.debug("Total Graph Count: " + graphcount);

        log.debug("Performing file analysis: finished");
        
        return retMe;

    }

    public VulnInfo detectAlternative() {
        log.error("This feature is not yet implemented");
        throw new RuntimeException("This feature is not yet implemented");
    }

    
//  ********************************************************************************
    
    private void dumpDotAuto(Automaton auto, String graphName, String path) {
        
        String filename = graphName + ".dot";
        (new File(path)).mkdir();

        try {
            Writer outWriter = new FileWriter(path + "/" + filename);
            String autoDot = auto.toDot();
            outWriter.write(autoDot);
            outWriter.close();
        } catch (IOException e) {
            log.error("Error writing file '" + path + "/" + filename + "', e");
            return;
        }

        if (MyOptions.option_P) {
            if (auto.isFinite()) {
                Set<String> finiteStringsSet = auto.getFiniteStrings();
                List<String> finiteStrings = new LinkedList<String>(finiteStringsSet);
                Collections.sort(finiteStrings);
                log.debug("IS FINITE");
                for (String finite : finiteStrings) {
                    System.out.println("Finite BEGIN");
                    System.out.println(finite);
                    System.out.println("Finite END");
                    System.out.println();
                }
            }
            
            System.out.println("Prefix BEGIN");
            System.out.println(auto.getCommonPrefix());
            System.out.println("Prefix END");
            System.out.println();
            //System.out.println("as regex: " + auto.toRegExp().toString());
            
            System.out.println("Suffix BEGIN");
            System.out.println(auto.getCommonSuffix());
            System.out.println("Suffix END");
            System.out.println();
        }

    }
    
//  ********************************************************************************
    
    // returns the automaton representation of the given dependency graph;
    // is done by decorating the nodes of the graph with automata bottom-up,
    // and returning the automaton that eventually decorates the root;
    // BEWARE: this also eliminates cycles!
    private Automaton toAutomaton(DepGraph depGraph) {
        depGraph.eliminateCycles();
        DepGraphNode root = depGraph.getRoot();
        Map<DepGraphNode,Automaton> deco = new HashMap<DepGraphNode,Automaton>();
        Set<DepGraphNode> visited = new HashSet<DepGraphNode>();
        this.decorate(root, deco, visited, depGraph);
        Automaton rootDeco = deco.get(root).clone();
        // BEWARE: minimization can lead to an automaton that is less human-readable
        //rootDeco.minimize(); 
        return rootDeco;
    }

//  ********************************************************************************
    
    // decorates the given node (and all its successors) with an automaton
    private void decorate(DepGraphNode node, Map<DepGraphNode,Automaton> deco,
            Set<DepGraphNode> visited, DepGraph depGraph) {
        
        visited.add(node);
        
        // if this node has successors, decorate them first (if not done yet)
        List<DepGraphNode> successors = depGraph.getSuccessors(node);
        if (successors != null && !successors.isEmpty()) {
            for (DepGraphNode succ : successors) {
                if (!visited.contains(succ) && deco.get(succ) == null) {
                    decorate(succ, deco, visited, depGraph);
                }
            }
        }
        
        // now that all successors are decorated, we can decorate this node
        
        Automaton auto = null;
        if (node instanceof DepGraphNormalNode) {
            DepGraphNormalNode normalNode = (DepGraphNormalNode) node;
            if (successors == null || successors.isEmpty()) {
                // this should be a string leaf node
                TacPlace place = normalNode.getPlace();
                if (place.isLiteral()) {
                    auto = Automaton.makeString(place.toString());
                } else {
                    // this case should not happen any longer (now that
                    // we have "uninit" nodes, see below)
                    log.error("Cant't happen " +normalNode.getCfgNode().getLoc());
                    throw new RuntimeException("SNH: " + place);
                }
            } else {
                // this is an interior node, not a leaf node;
                // the automaton for this node is the union of all the
                // successor automatas
                for (DepGraphNode succ : successors) {
                    Automaton succAuto = deco.get(succ);
                    if (auto == null) {
                        auto = succAuto;  // cloning not necessary here
                    } else {
                        auto = auto.union(succAuto);
                    }
                }
            }
            
        } else if (node instanceof DepGraphOpNode) {
            auto = this.makeAutoForOp((DepGraphOpNode) node, deco, depGraph);
            
        } else if (node instanceof DepGraphSccNode) {
            // conservative decision for SCCs
            auto = Automaton.makeAnyString(Transition.Taint.Directly);
            
        } else if (node instanceof DepGraphUninitNode) {
            
            // retrieve predecessor
            Set<DepGraphNode> preds = depGraph.getPredecessors(node);
            if (preds.size() != 1) {
                throw new RuntimeException("SNH");
            }
            DepGraphNode pre = preds.iterator().next();
            
            
            if (pre instanceof DepGraphNormalNode) {
                DepGraphNormalNode preNormal = (DepGraphNormalNode) pre;
                
                switch (this.initiallyTainted(preNormal.getPlace())) {
                case ALWAYS:
                case IFRG:
                    auto = Automaton.makeAnyString(Transition.Taint.Directly);
                    break;
                case NEVER:
                    auto = Automaton.makeAnyString(Transition.Taint.Untainted);
                    break;
                default:
                    throw new RuntimeException("SNH");
                }

            } else if (pre instanceof DepGraphOpNode) {
                throw new RuntimeException("not yet");
            } else if (pre instanceof DepGraphSccNode) {
                // conservative decision for SCCs
                auto = Automaton.makeAnyString(Transition.Taint.Directly);
            } else {
                throw new RuntimeException("SNH");
            }
            
        } else {
            throw new RuntimeException("SNH");
        }
        
        if (auto == null) {
            throw new RuntimeException("SNH");
        }
        
        deco.put(node, auto);
        
    }

//  ********************************************************************************
    
    // returns an automaton for the given operation node
    private Automaton makeAutoForOp(DepGraphOpNode node, Map<DepGraphNode,Automaton> deco,
            DepGraph depGraph) {
        
        List<DepGraphNode> successors = depGraph.getSuccessors(node);
        if (successors == null) {
            successors = new LinkedList<DepGraphNode>();
        }
        
        Automaton retMe = null;
        
        String opName = node.getName();
        if (opName.equals(".")) {
            
            // CONCAT
            for (DepGraphNode succ : successors) {
                Automaton succAuto = deco.get(succ);
                if (retMe == null) {
                    retMe = succAuto;
                } else {
                    retMe = retMe.concatenate(succAuto);
                }
            }
            
        } else {
            // conservative decision for operations that have not been
            // modeled yet: .*
            retMe = Automaton.makeAnyString(Transition.Taint.Directly);
        }
        
        return retMe;
    }

//  ********************************************************************************
    
    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected void checkForSink(CfgNode cfgNodeX, TacFunction traversedFunction,
            List<Sink> sinks) {
        
        if (cfgNodeX instanceof CfgNodeCallBuiltin) {
        
            // builtin function sinks
            
            CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
            
            /* these functions were retrieved from the PHP Manual;
             * note: those file functions that work with an existing file
             * handle are not of interest here; we are only interested in
             * those that take a filename string
             */
            
            /*
             
            if (functionName.equals("fopen")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("file")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("unlink")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("file_get_contents")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("readfile")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("file_put_contents")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first argument is of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("copy")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first two arguments are of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                sink.addSensitivePlace(paramList.get(1).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            } else if (functionName.equals("rename")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first two arguments are of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                sink.addSensitivePlace(paramList.get(1).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
                */
            
                /* what about this one?
            } else if (functionName.equals("move_uploaded_file")) {
                Sink sink = new Sink(cfgNode, traversedFunction);
                // the first two arguments are of interest
                List<TacActualParam> paramList = cfgNode.getParamList();
                sink.addSensitivePlace(paramList.get(0).getPlace());
                sink.addSensitivePlace(paramList.get(1).getPlace());
                // add this sink to the list of sensitive sinks
                sinks.add(sink);
            }
            */

        } else if (cfgNodeX instanceof CfgNodeCallPrep) {
            
            // user-defined custom sinks
            
            CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
            String functionName = cfgNode.getFunctionNamePlace().toString();
            
            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);

        } else {
            // not a sink
        }
    }
    
//  ********************************************************************************
    
    private void checkForSinkHelper(String functionName, CfgNode cfgNode, 
            List<TacActualParam> paramList, TacFunction traversedFunction, List<Sink> sinks) {
        
        if (this.dci.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            for (Integer param : this.dci.getSinks().get(functionName)) {
                if (paramList.size() > param) {
                    sink.addSensitivePlace(paramList.get(param).getPlace());
                    // add this sink to the list of sensitive sinks
                    sinks.add(sink);
                }
            }
        } else {
            // not a sink
        }

    }

    /*
    protected boolean isStrongSanit(String opName) {
        return false;
    }

    protected boolean isWeakSanit(String opName, List<Integer> indices) {
        return false;
    }

    protected boolean isEvil(String opName) {
        return false;
    }

    protected boolean isMulti(String opName, List<Integer> indices) {
        return false;
    }

    protected boolean isInverseMulti(String opName, List<Integer> indices) {
        return false;
    }
    */

    
}
