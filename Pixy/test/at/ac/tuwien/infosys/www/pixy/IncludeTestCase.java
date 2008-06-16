package at.ac.tuwien.infosys.www.pixy;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import junit.framework.*;

// Eclipse hint:
// all methods named "testXX" are executed automatically when choosing
// "Run / Run as... / JUnit Test"

public class IncludeTestCase 
extends TestCase {

    private String path;    // complete path to the testfile directory (with trailing slash)
    
    // these are recomputed for every single test
    private DepAnalysis depAnalysis;
    private XSSAnalysis xssAnalysis;
    List<Sink> sinks;
    
//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************
    
    // called automatically
    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/includes/";
    }
    
    
    // call this at the beginning of each test; optionally uses
    // a functional analysis instead of call-string ("functional" param),
    // and uses a dummy literal analysis
    private void mySetUp(String testFile, boolean functional) {
        
        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_A = true;    // perform alias analysis!
        MyOptions.setAnalyses("xss");
        
        // initialize & analyze
        TacConverter tac = checker.initialize().getTac();
        checker.analyzeTaint(tac, functional);
        this.depAnalysis = checker.gta.depAnalysis;
        this.xssAnalysis = (XSSAnalysis) checker.gta.getDepClients().get(0);

        // collect sinks
        this.sinks = this.xssAnalysis.collectSinks();
        /*
        this.sinks = new LinkedList<Sink>();
        for (TacFunction function : depAnalysis.getFunctions().values()) {
            for (Iterator iter = function.getCfg().dfPreOrderIterator(); iter.hasNext(); ) {
                CfgNode cfgNodeX = (CfgNode) iter.next();
                XSSAnalysis.checkForSink(cfgNodeX, function, sinks);
            }
        }
        */
        Collections.sort(sinks);

    }

    // returns the contents of the given file as string
    private String readFile(String fileName) {
        StringBuilder ret = new StringBuilder();
        try {
            FileReader fr = new FileReader(fileName);
            int c;
            ret = new StringBuilder();
            while ((c = fr.read()) != -1) {
                ret.append((char) c);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("File not found: " + fileName);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return ret.toString();
    }

    
    // set "generate" to false if you want to generate graphs
    // (instead of checking against existing graphs) 
    private void performTest(String testNum, int sinkNum, int graphNum, boolean generate) {
        
        performTest(testNum, sinkNum, graphNum, generate, false);
    }

    private void performTest(String testNum, int sinkNum, int graphNum, boolean generate, boolean functional) {
        
        //generate = true;
        
        mySetUp("test" + testNum + ".php", functional);
        
        Assert.assertTrue("Sinks real: " + sinks.size() + ", expected: " 
                + sinkNum, sinks.size() == sinkNum);

        // collect depGraphs
        List<DepGraph> depGraphs = new LinkedList<DepGraph>();
        for (Sink sink : sinks) {
            depGraphs.addAll(depAnalysis.getDepGraph(sink));
        }
        
        Assert.assertTrue("Graphs real: " + depGraphs.size() + ", expected: " 
                + graphNum, depGraphs.size() == graphNum);
        
        int graphCount = 0;
        for (DepGraph depGraph : depGraphs) {
            graphCount++;
            String fileName = "test" + testNum + "_" + graphCount;
            if (generate) {
                depGraph.dumpDotUnique(fileName, this.path);
            } else {
                String encountered = depGraph.makeDotUnique(fileName);
                String expected = this.readFile(this.path + fileName + ".dot");
                Assert.assertEquals(expected, encountered);
            }
        }

        if (generate) {
            // just to make sure that you don't accidentally forget
            // to switch generation off, and turn checking on
            Assert.fail("no check performed");
        }
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    public void test01() {
        String testNum = "01";
        int sinkNum = 1;        // expected number of sinks 
        int graphNum = 1;       // expected number of graphs
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test02() {
        String testNum = "02";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test03() {
        String testNum = "03";
        int sinkNum = 2; 
        int graphNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test04() {
        String testNum = "04";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test05() {
        String testNum = "05";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test06() {
        String testNum = "06";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test07() {
        String testNum = "07";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test08() {
        String testNum = "08";
        int sinkNum = 0; 
        int graphNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test09() {
        String testNum = "09";
        int sinkNum = 3; 
        int graphNum = 3;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test10() {
        String testNum = "10";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test11() {
        String testNum = "11";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test12() {
        String testNum = "12";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test13() {
        String testNum = "13";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test14() {
        String testNum = "14";
        int sinkNum = 0; 
        int graphNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    public void test15() {
        String testNum = "15";
        int sinkNum = 1; 
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }
    
    /*
     * HOW TO ADD NEW TESTS
     * 
     * - write a php testfile and move it to the right directory (see above)
     * - copy one of the existing test methods and adjust the numbers
     *   (for an explanation, see the first test method)
     * - set the fourth parameter of "performTest" to true, and run
     *   the test; this has the effect that dot files for the generated 
     *   graphs are dumped to the filesystem
     * - check if the dot files look as you expected
     * - switch the fourth parameter back to false 
     * 
     */
    
}






