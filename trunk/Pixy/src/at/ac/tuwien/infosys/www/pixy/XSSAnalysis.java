package at.ac.tuwien.infosys.www.pixy;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphNormalNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphOpNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphUninitNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallPrep;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeEcho;
import at.ac.tuwien.infosys.www.pixy.sanit.SanitAnalysis;
import org.apache.log4j.Logger;

// XSS detection
public class XSSAnalysis extends DepClient {

    protected static Logger log = Logger.getLogger(XSSAnalysis.class.getName());


    public XSSAnalysis(DepAnalysis depAnalysis) {
        super(depAnalysis);
    }
    

    // how it works:
    // - extracts the "relevant subgraph" (see there for an explanation)
    // - this relevant subgraph has the nice property that we can check for
    //   a vulnerability by simply looking at the remaining <uninit> nodes:
    //   if all these nodes belong to variables that are initially harmless,
    //   everything is OK; otherwise, we have a vulnerability
    public List<Integer> detectVulns() {
        
        log.debug("Performing XSS Analysis");

        List<Integer> retMe = new LinkedList<Integer>();
        
        // collect sinks
        List<Sink> sinks = this.collectSinks();
        Collections.sort(sinks);
        
        log.info("Number of sinks: " + sinks.size());

        // for the web interface
        StringBuilder sink2Graph = new StringBuilder();
        StringBuilder quickReport = new StringBuilder();
        
        String fileName = MyOptions.entryFile.getName();
        
        int graphcount = 0;
        int vulncount = 0;
        for (Sink sink : sinks) {
            
            Collection<DepGraph> depGraphs = depAnalysis.getDepGraph(sink);
            
            for (DepGraph depGraph : depGraphs) {

                graphcount++;
                
                String graphNameBase = "xss_" + fileName + "_" + graphcount;
                
                if (!MyOptions.optionW) {
                    depGraph.dumpDot(graphNameBase + "_dep", MyOptions.graphPath, this.dci);
                }
                
                // create the relevant subgraph
                DepGraph relevant = this.getRelevant(depGraph);
                
                // find those uninit nodes that are dangerous
                Map<DepGraphUninitNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);
                
                // if there are any dangerous uninit nodes...
                if (!dangerousUninit.isEmpty()) {
                    
                    // make the relevant subgraph smaller
                    relevant.reduceWithLeaves(dangerousUninit.keySet());
                    
                    Set<? extends DepGraphNode> fillUs;
                    if (MyOptions.option_V) {
                        relevant.removeTemporaries();
                        fillUs = relevant.removeUninitNodes();
                    } else {
                        fillUs = dangerousUninit.keySet();
                    }
                    
                    vulncount++;
                    DepGraphNormalNode root = depGraph.getRoot();
                    CfgNode cfgNode = root.getCfgNode();
                    retMe.add(cfgNode.getOrigLineno());

                    String vulnMessage = "Vulnerability detected in '" + cfgNode.getLoc() + "'";
                    vulnMessage += ". See graph: xss" + graphcount + " for details";
                    System.out.println(vulnMessage);

                    log.info("Vulnerability detected!");
                    if (dangerousUninit.values().contains(InitialTaint.ALWAYS)) {
                        log.info("- unconditional");
                    } else {
                        log.info("- conditional on register_globals=on");
                    }
                    log.info("- " + cfgNode.getLoc());

                    log.info("- Graph: xss" + graphcount);
                    relevant.dumpDot(graphNameBase + "_min", MyOptions.graphPath, fillUs, this.dci);

                    if (MyOptions.optionW) {
                        sink2Graph.append(sink.getLineNo());
                        sink2Graph.append(":");
                        sink2Graph.append(graphNameBase + "_min");
                        sink2Graph.append("\n");
                        
                        quickReport.append("Line ");
                        quickReport.append(sink.getLineNo());
                        quickReport.append("\nSources:\n");
                        for (DepGraphNode leafX : relevant.getLeafNodes()) {
                            quickReport.append("  ");
                            if (leafX instanceof DepGraphNormalNode) {
                                DepGraphNormalNode leaf = (DepGraphNormalNode) leafX;
                                quickReport.append(leaf.getLine());
                                quickReport.append(" : ");
                                quickReport.append(leaf.getPlace());
                            } else if (leafX instanceof DepGraphOpNode) {
                                DepGraphOpNode leaf = (DepGraphOpNode) leafX;
                                quickReport.append(leaf.getLine());
                                quickReport.append(" : ");
                                quickReport.append(leaf.getName());
                            }
                            quickReport.append("\n");
                        }
                        quickReport.append("\n");
                    }
                }
            }
        }
        
        // initial sink count and final graph count may differ (e.g., if some sinks
        // are not reachable)
        if (MyOptions.optionV) {
            log.debug("Total Graph Count: " + graphcount);
        }
        log.info("Total Vuln Count: " + vulncount);
        
        if (MyOptions.optionW) {
            Utils.writeToFile(sink2Graph.toString(), MyOptions.graphPath + "/xssSinks2Urls.txt");
            Utils.writeToFile(quickReport.toString(), MyOptions.graphPath + "/xssQuickReport.txt");
        }
        
        return retMe;
    }
    
//  ********************************************************************************
    
    // alternative to detectVulns;
    // returns those depgraphs for which a vulnerability was detected
    public VulnInfo detectAlternative() {

        // will contain depgraphs for which a vulnerability was detected
        VulnInfo retMe = new VulnInfo();
        
        // collect sinks
        List<Sink> sinks = this.collectSinks();
        Collections.sort(sinks);
        
        int graphcount = 0;
        int totalPathCount = 0;
        int basicPathCount = 0;
        int hasCustomSanitCount = 0;
        int customSanitThrownAwayCount = 0;
        for (Sink sink : sinks) {
            
            Collection<DepGraph> depGraphs = depAnalysis.getDepGraph(sink);
            
            for (DepGraph depGraph : depGraphs) {

                graphcount++;
                
                // create the relevant subgraph
                DepGraph relevant = this.getRelevant(depGraph);
                
                // find those uninit nodes that are dangerous
                Map<DepGraphUninitNode, InitialTaint> dangerousUninit = this.findDangerousUninit(relevant);
                
                // if there are any dangerous uninit nodes...
                boolean tainted = false;
                if (!dangerousUninit.isEmpty()) {
                    tainted = true;
                    
                    // make the relevant subgraph smaller
                    relevant.reduceWithLeaves(dangerousUninit.keySet());

                    retMe.addDepGraph(depGraph, relevant);
                }
                
                if (MyOptions.countPaths) {
                    int pathNum = depGraph.countPaths();
                    totalPathCount += pathNum;
                    if (tainted) {
                        basicPathCount += pathNum;
                    }
                }
                
                if (!SanitAnalysis.findCustomSanit(depGraph).isEmpty()) {
                    hasCustomSanitCount++;
                    if (!tainted) {
                        customSanitThrownAwayCount++;
                    }
                }
            }
            
        }
        
        retMe.setInitialGraphCount(graphcount);
        retMe.setTotalPathCount(totalPathCount);
        retMe.setBasicPathCount(basicPathCount);
        retMe.setCustomSanitCount(hasCustomSanitCount);
        retMe.setCustomSanitThrownAwayCount(customSanitThrownAwayCount);
        return retMe;

    }

//  ********************************************************************************
    
    // checks if the given node (inside the given function) is a sensitive sink;
    // adds an appropriate sink object to the given list if it is a sink
    protected void checkForSink(CfgNode cfgNodeX, TacFunction traversedFunction,
            List<Sink> sinks) {
        
        if (cfgNodeX instanceof CfgNodeEcho) {
            
            // echo() or print()
            CfgNodeEcho cfgNode = (CfgNodeEcho) cfgNodeX;

            // create sink object for this node
            Sink sink = new Sink(cfgNode, traversedFunction);
            sink.addSensitivePlace(cfgNode.getPlace());
            
            // add it to the list of sensitive sinks
            sinks.add(sink);

        } else if (cfgNodeX instanceof CfgNodeCallBuiltin) {
            
            // builtin function sinks
            
            CfgNodeCallBuiltin cfgNode = (CfgNodeCallBuiltin) cfgNodeX;
            String functionName = cfgNode.getFunctionName();

            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);

        } else if (cfgNodeX instanceof CfgNodeCallPrep) {
            
            CfgNodeCallPrep cfgNode = (CfgNodeCallPrep) cfgNodeX;
            String functionName = cfgNode.getFunctionNamePlace().toString();
            
            // user-defined custom sinks
                
            checkForSinkHelper(functionName, cfgNode, cfgNode.getParamList(), traversedFunction, sinks);
            
        } else {
            // not a sink
        }
    }
    
//  ********************************************************************************
    
    // LATER: this method looks very similar in all client analyses;
    // possibility to reduce code redundancy
    private void checkForSinkHelper(String functionName, CfgNode cfgNode, 
            List<TacActualParam> paramList, TacFunction traversedFunction, List<Sink> sinks) {
        
        if (this.dci.getSinks().containsKey(functionName)) {
            Sink sink = new Sink(cfgNode, traversedFunction);
            Set<Integer> indexList = this.dci.getSinks().get(functionName);
            if (indexList == null) {
                // special treatment is necessary here
                if (functionName.equals("printf"))  {
                    // none of the arguments to printf must be tainted
                    for (Iterator iter = paramList.iterator(); iter.hasNext();) {
                        TacActualParam param = (TacActualParam) iter.next();
                        sink.addSensitivePlace(param.getPlace());
                    }
                    sinks.add(sink);
                }
            } else {
                for (Integer index : indexList) {
                    if (paramList.size() > index) {
                        sink.addSensitivePlace(paramList.get(index).getPlace());
                        // add this sink to the list of sensitive sinks
                        sinks.add(sink);
                    }
                }
            }
        } else {
            // not a sink
        }

    }

//  ********************************************************************************
    
    /*
    protected boolean isStrongSanit(String opName) {
        
        if (opName.equals("htmlspecialchars") ||
                opName.equals("htmlentities") ||
                opName.equals("intval") ||
                opName.equals("floor") ||
                opName.equals("mysql_num_rows") ||
                opName.equals("mysql_error") ||
                opName.equals("count") ||
                opName.equals("sizeof") ||
                opName.equals("urlencode") ||
                opName.equals("rawurlencode") ||
                opName.equals("mt_rand") ||
                opName.equals("rand") ||
                opName.equals("strlen") ||
                opName.equals("phpversion") ||
                opName.equals("round") ||
                opName.equals("microtime") ||
                opName.equals("time") ||
                opName.equals("ob_get_clean") ||
                opName.equals("chr") ||
                opName.equals("getimagesize") ||
                opName.equals("filesize") ||
                opName.equals("strrpos") ||
                opName.equals("ini_get") ||
                opName.equals("gzcompress") ||
                opName.equals("pack") ||
                opName.equals("md5") ||
                opName.equals("hexdec") ||
                opName.equals("posix_getpwuid") ||
                opName.equals("posix_getgrgid") ||
                // "clean database" policy
                opName.equals("mysql_fetch_array") ||
                opName.equals("mysql_fetch_row") ||
                opName.equals("mysql_fetch_assoc") ||
                opName.equals("mysql_query") ||
                // "clean files" policy
                opName.equals("file") ||
                opName.equals("readdir") ||
                opName.equals("fread") ||
                opName.equals("fgets") ||
                opName.equals("opendir") ||
                // "clean environment" policy
                opName.equals("getenv") ||
                // harmless operators
                opName.equals("+") || // both binary and unary 
                opName.equals("-") || // both binary and unary
                opName.equals("*") ||
                opName.equals("/") ||
                opName.equals("%") ||
                opName.equals("&&") ||
                opName.equals("==") ||
                opName.equals("!=") ||
                opName.equals("<") ||
                opName.equals(">") ||
                opName.equals("<=") ||
                opName.equals(">=") ||
                opName.equals("===") ||
                opName.equals("!==") ||
                opName.equals("<<") ||
                opName.equals(">>") ||
                opName.equals("!") ||
                opName.equals("(int)") ||
                opName.equals("(double)") ||
                opName.equals("(bool)") ||
                opName.equals("(unset)") ||
                // Pixy's suppression function
                opName.equals(InternalStrings.suppression)) {
            return true;
        } else {
            return false;
        }
    }
    */
    
//  ********************************************************************************
    
    /*
    protected boolean isWeakSanit(String opName, List<Integer> indices) {
        // no weak sanitization for XSS
        return false;
    }*/
    
//  ********************************************************************************
    
    /*
    protected boolean isEvil(String opName) {
        if (opName.equals("urldecode") ||
                opName.equals("rawurldecode") ||
                opName.equals("strip_tags")
                ) {
            return true;
        } else {
            return false;
        }
    }*/
    
//  ********************************************************************************
    
    /*
    // if the given operation is a multi-dependency operation, it returns true
    // and fills the given indices list with the appropriate index numbers
    protected boolean isMulti(String opName, List<Integer> indices) {
        if (
                opName.equals("addslashes") ||
                opName.equals("base64_decode") ||
                opName.equals("basename") ||
                opName.equals("date") ||
                opName.equals("dirname") ||
                opName.equals("gmdate") ||
                opName.equals("ltrim") ||
                opName.equals("nl2br") ||
                opName.equals("pathinfo") ||
                opName.equals("realpath") ||
                opName.equals("rtrim") ||
                opName.equals("serialize") ||
                opName.equals("str_repeat") ||
                opName.equals("strftime") ||
                opName.equals("stripcslashes") ||
                opName.equals("stripslashes") ||
                opName.equals("strstr") ||
                opName.equals("strtolower") ||
                opName.equals("strtoupper") ||
                opName.equals("substr") ||
                opName.equals("trim") ||
                opName.equals("ucfirst") ||
                opName.equals("uniqid") ||
                opName.equals("var_export") ||
                opName.equals("~") ||
                opName.equals("(string)") ||
                opName.equals("(array)") ||
                opName.equals("(object)")
                ) {
            indices.add(0);
            return true;
        } else if (
                opName.equals("explode") ||
                opName.equals("split")
                ) {
            indices.add(1);
            return true;
        } else if (
                opName.equals("implode") ||
                opName.equals(".") ||
                opName.equals("|") ||
                opName.equals("&") ||
                opName.equals("^")
                ) {
            indices.add(0);
            indices.add(1);
            return true;
        }else if (
                opName.equals("ereg_replace") ||
                opName.equals("eregi_replace") || 
                opName.equals("preg_replace") || 
                opName.equals("str_replace")
                ) {
            indices.add(1);
            indices.add(2);
            return true;
        } else if (
                opName.equals("number_format") 
                ) {
            indices.add(0);
            indices.add(2);
            indices.add(3);
            return true;
        } else if (
                opName.equals("array_keys") ||
                opName.equals("array_reverse") ||
                opName.equals("array_values") ||
                opName.equals("each")
                ) {
            indices.add(0);
            return true;
        } else {
            return false;
        }
    }*/
    
//  ********************************************************************************
    
    /*
    protected boolean isInverseMulti(String opName, List<Integer> indices) {
        if (
                opName.equals("sprintf") ||
                opName.equals("max") ||
                opName.equals("min")
                ) {
            // all params are relevant, so don't add anything to the list
            return true;
        } else {
            return false;
        }
    }*/
    
}
