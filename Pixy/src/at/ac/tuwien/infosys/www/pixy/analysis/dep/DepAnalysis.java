package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.tf.*;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.mod.ModAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;
import org.apache.log4j.Logger;

// dependency analysis
public class DepAnalysis extends InterAnalysis {

    protected static Logger log = Logger.getLogger(DepAnalysis.class.getName());

    private TacConverter tac;
    private List<TacPlace> places;
    private ConstantsTable constantsTable;
    private SymbolTable superSymbolTable;
    private Variable memberPlace;
    private GenericRepos<LatticeElement> repos;

    private AliasAnalysis aliasAnalysis;
    private LiteralAnalysis literalAnalysis;
    private ModAnalysis modAnalysis;

    // has detectVulns() already been called?
    private boolean finishedDetection;
    
    
    
//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ExTaintAnalysis ****************************************************************
    
    public DepAnalysis(TacConverter tac, 
            AliasAnalysis aliasAnalysis,
            LiteralAnalysis literalAnalysis, 
            AnalysisType analysisType,
            InterWorkList workList,
            ModAnalysis modAnalysis) {
        
        this.tac = tac;
        this.places = tac.getPlacesList();
        this.constantsTable = tac.getConstantsTable();
        this.superSymbolTable = tac.getSuperSymbolTable();
        this.memberPlace = tac.getMemberPlace();
        this.repos = new GenericRepos<LatticeElement>();
        
        this.aliasAnalysis = aliasAnalysis;
        this.literalAnalysis = literalAnalysis;
        this.modAnalysis = modAnalysis;
        
        this.finishedDetection = false;
        
        this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(), 
                analysisType, workList);
        
    }
    
//  initLattice ********************************************************************
    
    protected void initLattice() {
        
        // initialize lattice
        this.lattice = new DepLattice(
            this.places, this.constantsTable, this.functions, 
            this.superSymbolTable, this.memberPlace);
        
        // initialize start value: a lattice element that adds no information to 
        // the default lattice element
        this.startValue = new DepLatticeElement();

        // initialize initial value
        this.initialValue = this.lattice.getBottom();
        
    }
    
//  applyInsideBasicBlock **********************************************************
    
    // takes the given invalue and applies transfer functions from the start of the
    // basic block until the beginning of "untilHere" (i.e., the transfer function
    // of this node is NOT applied);
    // only required by DepGraph
    public DepLatticeElement applyInsideBasicBlock(
            CfgNodeBasicBlock basicBlock, CfgNode untilHere, DepLatticeElement invalue) {
        
        DepLatticeElement outValue = new DepLatticeElement((DepLatticeElement) invalue);
        List containedNodes = basicBlock.getContainedNodes();
        CompositeTransferFunction ctf = (CompositeTransferFunction) this.getTransferFunction(basicBlock);
        
        Iterator nodesIter = containedNodes.iterator();
        Iterator tfIter = ctf.iterator();
        
        while (nodesIter.hasNext() && tfIter.hasNext()) {
            CfgNode node = (CfgNode) nodesIter.next();
            TransferFunction tf = (TransferFunction) tfIter.next();
            if (node == untilHere) {
                break;
            }
            outValue = (DepLatticeElement) tf.transfer(outValue);
        }
            
        return outValue;
    }

//  applyInsideDefaultCfg **********************************************************
    
    // takes the given invalue and applies transfer functions from the start of the
    // default cfg (given by "defaultNode") until the beginning of "untilHere" 
    // (i.e., the transfer function of this node is NOT applied); 
    // only required by DepGraph
    public DepLatticeElement applyInsideDefaultCfg(CfgNode defaultNode,
            CfgNode untilHere, DepLatticeElement invalue) {
        
        DepLatticeElement out = new DepLatticeElement((DepLatticeElement) invalue);
        
        while (defaultNode != untilHere) {
            TransferFunction tf = this.getTransferFunction(defaultNode);
            out = (DepLatticeElement) tf.transfer(out);
            defaultNode = defaultNode.getSuccessor(0);
        }
        
        return out;
        
    }

//  ********************************************************************************
//  TRANSFER FUNCTION GENERATORS ***************************************************
//  ********************************************************************************

    // returns a transfer function for an AssignSimple cfg node;
    // aliasInNode: 
    // - if cfgNodeX is not inside a basic block: the same node
    // - else: the basic block
    protected TransferFunction assignSimple(CfgNode cfgNodeX, CfgNode aliasInNode) {
        
        CfgNodeAssignSimple cfgNode = (CfgNodeAssignSimple) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new DepTfAssignSimple(
                        left, 
                        cfgNode.getRight(),
                        mustAliases,
                        mayAliases,
                        cfgNode);
    }

    protected TransferFunction assignUnary(CfgNode cfgNodeX, CfgNode aliasInNode) {
        
        CfgNodeAssignUnary cfgNode = (CfgNodeAssignUnary) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new DepTfAssignUnary(
                        left, 
                        cfgNode.getRight(),
                        cfgNode.getOperator(),
                        mustAliases,
                        mayAliases,
                        cfgNode);
    }

    protected TransferFunction assignBinary(CfgNode cfgNodeX, CfgNode aliasInNode) {
        
        CfgNodeAssignBinary cfgNode = (CfgNodeAssignBinary) cfgNodeX;
        Variable left = (Variable) cfgNode.getLeft();
        Set mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
        Set mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

        return new DepTfAssignBinary(
                        left, 
                        cfgNode.getLeftOperand(),
                        cfgNode.getRightOperand(),
                        cfgNode.getOperator(),
                        mustAliases,
                        mayAliases,
                        cfgNode);
    }

    protected TransferFunction assignRef(CfgNode cfgNodeX) {
        CfgNodeAssignRef cfgNode = (CfgNodeAssignRef) cfgNodeX;
        return new DepTfAssignRef(
                cfgNode.getLeft(),
                cfgNode.getRight(),
                cfgNode);
    }

    protected TransferFunction unset(CfgNode cfgNodeX) {
        CfgNodeUnset cfgNode = (CfgNodeUnset) cfgNodeX;
        return new DepTfUnset(cfgNode.getOperand(), cfgNode);
    }
    
    protected TransferFunction assignArray(CfgNode cfgNodeX) {
        CfgNodeAssignArray cfgNode = (CfgNodeAssignArray) cfgNodeX;
        return new DepTfAssignArray(cfgNode.getLeft(), cfgNode);
    }
    
    protected TransferFunction callPrep(CfgNode cfgNodeX, TacFunction traversedFunction) {

        CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
        TacFunction calledFunction = cfgNode.getCallee();
        TacFunction callingFunction = traversedFunction;
        
        // call to an unknown function;
        // should be prevented in practice (all functions should be
        // modeled in the builtin functions file), but if
        // it happens: assume that it doesn't do anything to
        // taint information;
        // NOTE: in the extended taint analysis, all builtin functions
        // are unknown functions! modeling is done only at the point where
        // a taint graph is constructed
        // NOTE: in the actual version of Pixy, builtin functions are
        // represented by their own special cfg node (see callBuiltin())
        if (calledFunction == null) {
            
            // at this point, unknown functions should already be represented
            // by their own special cfg node
            log.error("Called function is null:" + cfgNodeX.getFileName() + ", " +
                    cfgNodeX.getOrigLineno() + " " + cfgNode.getFunctionNamePlace());
            throw new RuntimeException("SNH");
            
            // how this works:
            // - propagate with ID transfer function to CfgNodeCall
            // - the analysis algorithm propagates from CfgNodeCall
            //   to CfgNodeCallRet with ID transfer function
            // - we propagate from CfgNodeCallRet to the following node
            //   with a special transfer function that only assigns the
            //   taint of the unknown function's return variable to
            //   the temporary catching the function's return value
            //System.out.println("unknown function: " + cfgNode.getFunctionNamePlace());
            //return TransferFunctionId.INSTANCE;
        }

        // extract actual and formal params
        List actualParams = cfgNode.getParamList();
        List formalParams = calledFunction.getParams();

        // the transfer function to be assigned to this node
        TransferFunction tf = null;

        if (actualParams.size() > formalParams.size()) {
            // more actual than formal params; either a bug or a varargs
            // occurrence;
            // note that cfgNode.getFunctionNamePlace() returns a different
            // result than function.getName() if "function" is 
            // the unknown function
            throw new RuntimeException(
                "More actual than formal params for function " + 
                cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOrigLineno());
            
        } else {
            tf = new DepTfCallPrep(actualParams, formalParams, 
                    callingFunction, calledFunction, this, cfgNode);
        }
            
        return tf;

    }

    protected TransferFunction entry(TacFunction traversedFunction) {
        return new DepTfEntry(traversedFunction);
    }
    
    protected TransferFunction callRet(CfgNode cfgNodeX, TacFunction traversedFunction) {
        
        CfgNodeCallRet cfgNodeRet = (CfgNodeCallRet) cfgNodeX;
        CfgNodeCallPrep cfgNodePrep = cfgNodeRet.getCallPrepNode();
        TacFunction callingFunction = traversedFunction;
        TacFunction calledFunction = cfgNodePrep.getCallee();
        
        // call to an unknown function;
        // for explanations see above (handling CfgNodeCallPrep)
        TransferFunction tf;
        if (calledFunction == null) {
            
            throw new RuntimeException("SNH");
            // tf = new DepTfCallRetUnknown(cfgNodeRet, cfgNodePrep.getFunctionNamePlace());
            
        } else {

            Set<TacPlace> modSet = null;
            if (this.modAnalysis != null) {
                modSet = this.modAnalysis.getMod(calledFunction);
            }
            
            // quite powerful transfer function, does many things
            tf = new DepTfCallRet(
                    this.interAnalysisInfo.getAnalysisNode(cfgNodePrep),
                    callingFunction,
                    calledFunction,
                    cfgNodePrep,
                    cfgNodeRet,
                    this.aliasAnalysis,
                    modSet);
        }
        
        return tf;
    }

    protected TransferFunction callBuiltin(CfgNode cfgNodeX, TacFunction traversedFunction) {
        CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
        return new DepTfCallBuiltin(cfgNode);
    }

    protected TransferFunction callUnknown(CfgNode cfgNodeX, TacFunction traversedFunction) {
        CfgNodeCallUnknown cfgNode = (CfgNodeCallUnknown) cfgNodeX;
        return new DepTfCallUnknown(cfgNode);
    }
    
    protected TransferFunction global(CfgNode cfgNodeX) {
        
        // "global <var>";
        // equivalent to: creating a reference to the variable in the main function with
        // the same name
        CfgNodeGlobal cfgNode = (CfgNodeGlobal) cfgNodeX;
        
        // operand variable of the "global" statement
        Variable globalOp = cfgNode.getOperand();
        
        // retrieve the variable from the main function with the same name
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        Variable realGlobal = mainSymTab.getVariable(globalOp.getName());
        
        // trying to declare something global that doesn't occur in the main function?
        if (realGlobal == null) {
            
            /*
            System.out.println("Warning: accessing non-existent global with 'global' keyword: " + globalOp.getName());
            System.out.println("File: " + cfgNodeX.getFileName());
            System.out.println("Line: " + cfgNodeX.getOrigLineno());
            */
            
            // we must not simply ignore this case, since the corresponding
            // local's taint/label would remain harmless/harmless (the default value for locals);
            // => approximate by assigning unknown/unknown to the operand
            // LATER: this, on the other hand, is unsound, but it reduces the false positive rate:
            return TransferFunctionId.INSTANCE;

        } else {
            // found existent global
            return new DepTfAssignRef(globalOp, realGlobal, cfgNode);
        }
    }
    
    protected TransferFunction isset(CfgNode cfgNodeX) {
        
        CfgNodeIsset cfgNode = (CfgNodeIsset) cfgNodeX;
        return new DepTfIsset(
                cfgNode.getLeft(), 
                cfgNode.getRight(),
                cfgNode);
    }

    protected TransferFunction define(CfgNode cfgNodeX) {
        CfgNodeDefine cfgNode = (CfgNodeDefine) cfgNodeX;
        return new DepTfDefine(this.constantsTable, this.literalAnalysis, cfgNode);
    }

    protected TransferFunction tester(CfgNode cfgNodeX) {
        // special node that only occurs inside the builtin functions file
        CfgNodeTester cfgNode = (CfgNodeTester) cfgNodeX;
        return new DepTfTester(cfgNode);
    }
    
    protected TransferFunction echo(CfgNode cfgNodeX, TacFunction traversedFunction) {
        return TransferFunctionId.INSTANCE;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  evalIf *************************************************************************
    
    protected Boolean evalIf(CfgNodeIf ifNode, LatticeElement inValue) {
        return this.literalAnalysis.evalIf(ifNode);
    }

//  getDepGraph ********************************************************************
    
    // returns the dependency graphs for the given sink
    public List<DepGraph> getDepGraph(Sink sink) {
        
        List<DepGraph> retMe = new LinkedList<DepGraph>();
        
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        
        // get vulnerabilities for this sink
        List<SinkProblem> problems;
        problems = sink.getSinkProblems();
        
        // if this sink has no problems: done!
        if (problems.isEmpty()) {
            return retMe;
        }
        
        // create dependency graphs for all sensitive places in this sink
        for (SinkProblem problem : problems) {
            
            DepGraph depGraph = DepGraph.create(problem.getPlace(),
                    sink.getNode(), this.interAnalysisInfo, mainSymTab, this);
            
            // a null depGraph is returned if this sink is unreachable
            if (depGraph == null) {
                continue;
            }
            
            retMe.add(depGraph);
        }
            
        return retMe;
    }

//  *********************************************************************************
    
    public TacConverter getTac() {
        return this.tac;
    }
    
// getDepGraphs ********************************************************************
    
    // returns the dependency graphs for all given sinks;
    // this (older) function collects some nice statistics
    public List<DepGraph> getDepGraphs(List<Sink> sinks) {

        List<DepGraph> retMe = new LinkedList<DepGraph>();
        
        // sort sinks by line 
        Collections.sort(sinks);
        
        // whether to collect & print statistics
        // (can be quite time-consuming for large programs)
        boolean statistics = false;
        
        // graph shape counters
        int treeCount = 0;
        int dagCount = 0;
        int cycleCount = 0;
        
        // leaf stringness counters
        // note: this is only counted for trees and dags
        int stringLeafs = 0;
        int nonStringLeafs = 0;
        
        TacFunction mainFunc = this.mainFunction;
        SymbolTable mainSymTab = mainFunc.getSymbolTable();
        
        // map operation name -> number of occurrences
        Map<String,Integer> opMap = new HashMap<String,Integer>();
        
        // for each sink...
        int i = 0;
        for (Sink sink : sinks) {
            i++;

            // get vulnerabilities for this sink
            List<SinkProblem> problems;
            problems = sink.getSinkProblems();
            
            // if this sink has no problems: continue with the next one
            // (probably happens if a function's sensitive parameter is optional)
            if (problems.isEmpty()) {
                continue;
            }
            
            // create dependency graphs for all sensitive places in this sink
            for (SinkProblem problem : problems) {
                
                DepGraph depGraph = DepGraph.create(problem.getPlace(),
                        sink.getNode(), this.interAnalysisInfo, mainSymTab, this);
                
                // a null depGraph is returned if this sink is unreachable
                if (depGraph == null) {
                    continue;
                }
                
                retMe.add(depGraph);
                
                if (statistics) {
                
                    // shape statistics and leaf stringness
                    log.debug("Dep Graph Shape Info");
                    if (depGraph.isTree()) {
                        log.debug("is a tree");
                        treeCount++;
                        if (depGraph.leafsAreStrings()) {
                            log.debug("leafs are strings");
                            stringLeafs++;
                        } else {
                            log.debug("leafs are not strings");
                            nonStringLeafs++;
                        }
                    } else if (depGraph.hasCycles()) {
                        log.debug("has cycles");
                        cycleCount++;
                    } else {
                        log.debug("is a dag");
                        dagCount++;
                        if (depGraph.leafsAreStrings()) {
                            log.debug("leafs are strings");
                            stringLeafs++;
                        } else {
                            log.debug("leafs are not strings");
                            nonStringLeafs++;
                        }
                    }
                    log.debug("");
                    
                    // operation counters
                    log.debug("Dep Graph Operation Counters");
                    Map<String,Integer> singleOpMap = depGraph.getOpMap();
                    for (Map.Entry<String, Integer> entry : singleOpMap.entrySet()) {
                        String opName = entry.getKey();
                        Integer opCount = entry.getValue();
                        log.debug(opName + ": " + opCount);
                        // update totals map
                        Integer totalCount = opMap.get(opName);
                        if (totalCount == null) {
                            totalCount = new Integer(opCount);
                        } else {
                            totalCount = new Integer(totalCount.intValue() + opCount);
                        }
                        opMap.put(opName, totalCount);
                    }
                    log.debug("");
                
                }
                
                /*
                // print call nodes
                List callNodes = problem.getCallNodes();
                if (!callNodes.isEmpty()) {
                    System.out.println("called by: ");
                    for (Iterator callsIter = callNodes.iterator(); callsIter.hasNext();) {
                        CfgNodeCall callNode = (CfgNodeCall) callsIter.next();
                        System.out.println(callNode.getFileName() + ", line " + callNode.getOrigLineno());
                    }
                } 
                
                  // print include chain
                   List includeChain = IncDomAnalysis.computeChain(sink.getFunction(), sink.getNode());
                   if (!includeChain.isEmpty()) {
                   System.out.println("include chain:");
                   for (Iterator iterator = includeChain.iterator(); iterator
                   .hasNext();) {
                   CfgNode includeNode = (CfgNode) iterator.next();
                   System.out.println(includeNode.getFileName() + ", " + includeNode.getOrigLineno());
                   }
                   }
                   */
            }
            //System.out.println(i);
        }
        
        this.finishedDetection = true;
        if (false) {
            log.debug("cleaning dependency analysis...");
            this.clean();
        } else {
            log.debug("skipping clean-up");
        }
        
        if (statistics) {
        
            // print graph shape statistics
            String message = "Total Shape Statistics " + "\n" +
                "------------------------" + "\n" +
                "trees:        " + treeCount + "\n" +
                "dags:         " + dagCount +  "\n" +
                "cycle graphs: " + cycleCount + "\n" +
            
            // leaf stringness
                "with string leafs:     " + stringLeafs + "\n" +
                "with non-string leafs: " + nonStringLeafs ;
            log.debug(message);
            
            // print total operation counters
            String m = "Total Op Statistics" + "\n" + "---------------------";
            for (Map.Entry<String, Integer> entry : opMap.entrySet()) {
                String opName = entry.getKey();
                Integer opCount = entry.getValue();
                m += "\n" + opName + ": " + opCount;
            }
            log.debug(m);
        
        }
            
        return retMe;

    }
    
//  detectVulns *********************************************************************
    
    // call this method when the analysis is finished to detect vulnerabilities
    public void detectVulns() {
        
        if (true)
        throw new RuntimeException("dummy method");
        
        /*
        // sort sinks by line; 
        // EFF: only sort sinks that have a reported vulnerability
        Collections.sort(this.sinks);
        
        // total vulnerability counter
        int vulnCount = 0;
        
        // graph shape counters
        int treeCount = 0;
        int dagCount = 0;
        int cycleCount = 0;
        
        // leaf stringness counters
        // note: this is only counted for trees and dags
        int stringLeafs = 0;
        int nonStringLeafs = 0;
        
        // map operation name -> number of occurrences
        Map<String,Integer> opMap = new HashMap<String,Integer>();

        // where to dump the taint graphs...
        String graphPath = this.graphDirectory();
        
        // taint graph will need this
        Variable taintedVar = this.tac.getSuperGlobal("$_TAINTED");
        
        // for each sink...
        List<ExTaintSink> reportedSinks = new LinkedList<ExTaintSink>();
        for (Iterator iter = this.sinks.iterator(); iter.hasNext(); ) {
            
            ExTaintSink sink = (ExTaintSink) iter.next();

            // get vulnerabilities for this sink
            TacFunction mainFunc = (TacFunction) this.functions.get(InternalStrings.mainFunctionName);
            SymbolTable mainSymTab = mainFunc.getSymbolTable();
            List<ExTaintProblem> vulns;
            vulns = sink.detectVulns(
                    this.analysisInfo, mainSymTab, this.analysisType, taintedVar, this, this.type);
            
            // if this sink has no vulnerabilities: continue with the next one
            if (vulns.isEmpty()) {
                continue;
            }
            
            reportedSinks.add(sink);
            
            // report vulnerabilities for this sink
            for (Iterator vulnsIter = vulns.iterator(); vulnsIter.hasNext(); ) {

                // increase vuln counter
                vulnCount++;
                
                ExTaintProblem vuln = (ExTaintProblem) vulnsIter.next();
                System.out.println();
                System.out.println("Tainted variable " + vuln.getPlace() + 
                        " entering sensitive sink in " + sink.getFileName() + 
                        ", line " + sink.getLineNo());
                List callNodes = vuln.getCallNodes();
                
                // dump taint graph for this variable
                vuln.dumpGraph("tg" + vulnCount, graphPath);
                
                if (this.type.equals("string") || this.type.equals("stringfile")) {
                    
                    StringGraph stringGraph = vuln.getStringGraph();

                    // shape statistics and leaf stringness
                    if (stringGraph.isTree()) {
                        System.out.println("is a tree");
                        treeCount++;
                        if (stringGraph.leafsAreStrings()) {
                            System.out.println("leafs are strings");
                            stringLeafs++;
                        } else {
                            System.out.println("leafs are not strings");
                            nonStringLeafs++;
                        }
                    } else if (stringGraph.hasCycles()) {
                        System.out.println("has cycles");
                        cycleCount++;
                    } else {
                        System.out.println("is a dag");
                        dagCount++;
                        if (stringGraph.leafsAreStrings()) {
                            System.out.println("leafs are strings");
                            stringLeafs++;
                        } else {
                            System.out.println("leafs are not strings");
                            nonStringLeafs++;
                        }
                    }
                    
                    // operation counters
                    Map<String,Integer> singleOpMap = stringGraph.getOpMap();
                    for (Map.Entry<String, Integer> entry : singleOpMap.entrySet()) {
                        String opName = entry.getKey();
                        Integer opCount = entry.getValue();
                        System.out.println(opName + ": " + opCount);
                        // update totals map
                        Integer totalCount = opMap.get(opName);
                        if (totalCount == null) {
                            totalCount = new Integer(opCount);
                        } else {
                            totalCount = new Integer(totalCount.intValue() + opCount);
                        }
                        opMap.put(opName, totalCount);
                    }
                    
                    // dump automaton; this also eliminates cycles!
                    stringGraph.dumpDotAuto("auto" + vulnCount, graphPath);

                }
                
                // print call nodes
                if (!callNodes.isEmpty()) {
                    System.out.println("called by: ");
                    for (Iterator callsIter = callNodes.iterator(); callsIter.hasNext();) {
                        CfgNodeCall callNode = (CfgNodeCall) callsIter.next();
                        System.out.println(callNode.getFileName() + ", line " + callNode.getOrigLineno());
                    }
                }

                // print include chain
                List includeChain = IncDomAnalysis.computeChain(sink.getFunction(), sink.getNode());
                if (!includeChain.isEmpty()) {
                    System.out.println("include chain:");
                    for (Iterator iterator = includeChain.iterator(); iterator
                            .hasNext();) {
                        CfgNode includeNode = (CfgNode) iterator.next();
                        System.out.println(includeNode.getFileName() + ", " + includeNode.getOrigLineno());
                    }
                }
                
            }
        }
        
        this.finishedDetection = true;
        if (false) {
            System.out.println("cleaning taint analysis...");
            this.clean();
        } else {
            System.out.println("skipping clean-up");
        }

        if (this.type.equals("string") || this.type.equals("stringfile")) {
            
            // print graph shape statistics
            System.out.println();
            System.out.println("trees:        " + treeCount);
            System.out.println("dags:         " + dagCount);
            System.out.println("cycle graphs: " + cycleCount);
            
            // leaf stringness
            System.out.println();
            System.out.println("with string leafs:     " + stringLeafs);
            System.out.println("with non-string leafs: " + nonStringLeafs);

            // print total operation counters
            System.out.println();
            for (Map.Entry<String, Integer> entry : opMap.entrySet()) {
                String opName = entry.getKey();
                Integer opCount = entry.getValue();
                System.out.println(opName + ": " + opCount);
            }
        }

        System.out.println("\nDetected " + vulnCount + " vulnerabilities");
        
        */
    }

//  recycle ************************************************************************
    
    public LatticeElement recycle(LatticeElement recycleMe) {
        return this.repos.recycle(recycleMe);
    }

//  getMainFunction ****************************************************************
    
    public TacFunction getMainFunction() {
        return this.tac.getMainFunction();
    }
    
//  clean **************************************************************************
    
    // performs post-analysis cleanup operations to save memory
    public void clean() {
        // although we don't perform recycling during the analysis, we
        // do perform recycling for folding & cleaning; otherwise, cleaning
        // could result in bigger memory consumption than before
        if (this.finishedDetection) {
            this.interAnalysisInfo.foldRecycledAndClean(this);
        } else {
            throw new RuntimeException("Refusing to clean extaint analysis: no detection yet");
        }
    }
    
//  ********************************************************************************
    
    // checks if the callgraph contains unreachable code (i.e., nodes that have not
    // been associated with analysis information)
    public void checkReachability() {
        
        if (!(this.analysisType instanceof CSAnalysis)) {
            // in this case, we do not have a callgraph, and can't check for
            // unreachable code
            log.warn("We don't have callgraph. Can't check reachability");
            return;
        }
        
        ConnectorComputation cc = ((CSAnalysis) this.analysisType).getConnectorComputation();
        CallGraph callGraph = cc.getCallGraph();

        // for each function in the call graph...
        for (TacFunction f : callGraph.getFunctions()) {
            CfgNode head = f.getCfg().getHead();
            
            // make a depth-first traversal
            
            // the number of contexts for this function
            int numContexts = cc.getNumContexts(f);
            
            // auxiliary stack and visited set
            LinkedList<CfgNode> stack = new LinkedList<CfgNode>();
            Set<CfgNode> visited = new HashSet<CfgNode>();

            // visit head
            CfgNode current = head;
            visited.add(current);
            if (!this.isReachable(head, numContexts)) {
                this.warnUnreachable(current);
            } else {
                stack.add(current);
            }

            // how it works:
            // while there is something on the stack:
            // - mark the top stack element as visited
            // - try to get an unvisited successor of this element
            // - if there is such a successor: push it on the stack and continue
            // - else: pop the stack
            while (!stack.isEmpty()) {
                
                // inspect the top stack element
                current = stack.getLast();
                
                // we will try to get an unvisited successor element
                CfgNode next = null;
                for (int i = 0; (i < 2) && (next == null); i++) {
                    CfgEdge outEdge = current.getOutEdge(i);
                    if (outEdge != null) {
                        next = outEdge.getDest();
                        if (visited.contains(next)) {
                            // try another one
                            next = null;
                        } else {
                            // found it!
                        }
                    }
                }
            
                if (next == null) {
                    // pop from stack
                    stack.removeLast();
                } else {
                    // visit next
                    visited.add(next);
                    
                    if (!this.isReachable(next, numContexts)) {
                        this.warnUnreachable(next);
                    } else {
                        stack.add(next);
                    }
                }
            }
        }
    }
    
    private boolean isReachable(CfgNode cfgNode, int numContexts) {
        Map<Context, LatticeElement> phi = this.interAnalysisInfo.getAnalysisNode(cfgNode).getPhi();
        if (phi.size() == 0) {
            // there is not a single context for this node
            return false;
        }
        // old, over-conservative check which could lead to an unnecessarily
        // large number of warnings
        /*
        if (phi.size() != numContexts) {
            // there are missing contexts in this phi map
            return false;
        }
        */
        for (LatticeElement elem : phi.values()) {
            if (elem == null) {
                // a null lattice element?
                throw new RuntimeException("SNH");
                //return false;
            }
        }
        return true;
    }
    
    private void warnUnreachable(CfgNode cfgNode) {
        log.warn("Warning: Unreachable code " + cfgNode.getLoc());
        if (cfgNode instanceof CfgNodeCallRet) {
            CfgNodeCallRet callRet = (CfgNodeCallRet) cfgNode;
            log.debug("- return from: " + callRet.getCallNode().getFunctionNamePlace());
        } else {
            log.debug("- " + cfgNode);
        }
    }
    
}
