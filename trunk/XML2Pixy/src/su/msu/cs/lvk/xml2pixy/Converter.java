package su.msu.cs.lvk.xml2pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.Checker;
import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.ProgramConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

// TODO comment everything

/**
 * Main class used to run taint analysis on .py.xml and .php files.
 */
public class Converter {
    public static final String LOG_CONFIG_FILE = "log4j.properties";
    public static String mainFile;
    public static Document modulesConfig;
    private static Logger log = Logger.getLogger(Converter.class.getName());

    /**
     * Read modules.xml file, containing headers for builtin modules, functions, classes
     *
     * @param cfgFile config file name (<code>modules.xml</code>)
     */
    public static void readModulesConfig(String cfgFile) {
        try {
            SAXBuilder parser = new SAXBuilder();
            modulesConfig = parser.build(new File(cfgFile));
        } catch (Exception e) {
            log.error("Couldn't read modules config file: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        log.info("Starting analysis");

        Options cliOptions = new Options();
        cliOptions.addOption("a", false, "print analysis details");
        cliOptions.addOption("t", false, "print resulting php ParseTree");
        cliOptions.addOption("A", "alias", false, "print resulting php ParseTree");

        CommandLineParser cliParser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = cliParser.parse(cliOptions, args);
        } catch (ParseException e) {
            usage(cliOptions);
        }

        if (cmd.getArgs().length < 1) {
            usage(cliOptions);
        }

        checkArg(cmd.getArgs()[0]);
        mainFile = new File(cmd.getArgs()[0]).getAbsolutePath();

//        MyOptions.option_A = false;
//        MyOptions.option_L = true;
        Checker pixy = new Checker(cmd.getArgs()[0]);
        MyOptions.entryFile = new File(cmd.getArgs()[0]);

        MyOptions.optionA = false;
        MyOptions.option_A = cmd.hasOption("A");
        MyOptions.setAnalyses("xss:sql");
        MyOptions.graphPath = "./graph";

        // Create clean grahPath
        File graphPath = new File(MyOptions.graphPath);
        if (!graphPath.exists()) {
            graphPath.mkdirs();
        } else {
            for (File file : graphPath.listFiles()) {
                file.delete();
            }
        }

        // Build ParseNode tree. From php source file, if it is .php, or from python source
        // if it is .py.xml
        ParseNode rootParseNode = !PixyConverter.isPhpFile(mainFile)
                ? Utils.transformPython(mainFile)
                : new PixyConverter(false, true).parse(mainFile).getRoot();

        // Print parse node
        if (cmd.hasOption("t")) {
            log.debug("Printing parse tree");
            Utils.printTree(rootParseNode);
            log.debug("Printing parse tree finished");
        }

        // Convert to pixy three addess code (TAC)
        ProgramConverter pcv = new PixyConverter(false, true);
        pcv = pixy.initialize(pcv);
        TacConverter tac = pcv.getTac();

        // Perform analysis
        pixy.analyzeTaint(tac, !MyOptions.optionA); // why functional analysis == false?
        pixy.literalAnalysis = null;
        pixy.aliasAnalysis = null;

        log.info("Searching for vulnerabilities");
        pixy.gta.detectVulns();
        log.info("Searching for vulnerabilities finished");

        if (cmd.hasOption("a")) {
            Map<Integer, StringWriter> results = new HashMap<Integer, StringWriter>();

            //Try pixy.literalAnalysis, pcv.typeAnalysis (the latter returns empty info for some reason)
            Utils.buildAnnotations(results, pixy.gta.depAnalysis, tac.getAllFunctions());
            Utils.printTree(rootParseNode, results);

            for (TacFunction tacFunction : tac.getUserFunctions().values()) {
                Dumper.dumpDot(tacFunction, MyOptions.graphPath, true);
            }
        }

    }

    public static void usage(Options cliOptions) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("xml2pixy [options] file", cliOptions);
        System.exit(1);
    }

    /**
     * Checks whether the main argument file exists and is file (not directory).
     *
     * @param arg main argument file name
     * @return File object, associated with the main argument
     */
    public static File checkArg(String arg) {
        boolean failed = false;

        File mainArg = new File(arg);
        if (!mainArg.exists()) {
            log.error("File " + arg + " doesn't exist");
            failed = true;
        }
        if (!(mainArg.isFile())) {
            log.error(arg + " must be file");
            failed = true;
        }

        if (failed) System.exit(0);
        return mainArg;
    }

}
