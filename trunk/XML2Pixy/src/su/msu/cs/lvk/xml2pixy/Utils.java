package su.msu.cs.lvk.xml2pixy;

import at.ac.tuwien.infosys.www.phpparser.*;
import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Cfg;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import su.msu.cs.lvk.xml2pixy.jdom.JdomVisitor;
import su.msu.cs.lvk.xml2pixy.jdom.JdomWalker;
import su.msu.cs.lvk.xml2pixy.jdom.SymbolTableBuilder;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodePrinter;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeVisitor;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeWalker;
import su.msu.cs.lvk.xml2pixy.postproc.*;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.ParseNodeBuilder;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.classes.ClassManager;
import su.msu.cs.lvk.xml2pixy.transform.classes.PyClass;
import su.msu.cs.lvk.xml2pixy.transform.function.Function;
import su.msu.cs.lvk.xml2pixy.transform.function.FunctionManager;
import su.msu.cs.lvk.xml2pixy.transform.function.Method;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utils class. Contains some main methods for building parse trees from .py.xml files and printing
 * result php code.
 */
public class Utils {

    protected static Logger log = Logger.getLogger(Utils.class.getName());

    /**
     * Trims input string. Returns empty string if argument is empty or null.
     * @param str input string
     * @return trimmed string
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Trims input string. Returns null if argument is empty or null.
     * @param str input string
     * @return trimmed string
     */
    public static String trimToNull(String str) {
        str = str == null ? null : str.trim();
        return "".equals(str) ? null : str;
    }

    /**
     * Tests input string if it is null or contains only non-printable characters.
     * @param str input string
     * @return <code>true</code> if input string is blank, <code>false</code> otherwise.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().equals("");
    }

    /**
     * Prints argument parse tree to System.out without any comments. The output is formatted.
     * @param node argument parse tree
     */
    public static void printTree(ParseNode node) {
        printTree(node, null);
    }

    /**
     * Prints argument parse tree to System.out with line-by-line comments. The output is not formatted
     * since comments are source lineno relative.
     * @param node argument parse tree
     * @param comments map lineno -> StringWriter, which contains comment strings.
     */
    public static void printTree(ParseNode node, Map<Integer, StringWriter> comments) {
        ParseNodePrinter pnPrinter = new ParseNodePrinter(System.out, comments);
        ParseNodeWalker pnWalker = new ParseNodeWalker(pnPrinter);
        pnWalker.walk(node);
    }

    /**
     * Calculates lineno of the leftmost lexeme of provided parse node.
     * @param node parse tree
     * @return lineno of the leftmost lexeme or -2 if is is unavailable or <0.
     */
    public static int getLinenoLeft(ParseNode node) {
        return getLinenoLeft(node, -2);
    }

    /**
     * Calculates lineno of the leftmost lexeme of provided parse node.
     * @param node parse tree
     * @param def default value
     * @return lineno of the leftmost lexeme or default value if is is unavailable or <0.
     */
    public static int getLinenoLeft(ParseNode node, int def) {
        ParseTree tree = new ParseTree(node);
        Iterator iter = tree.leafIterator();
        while (iter.hasNext()) {
            ParseNode leaf = (ParseNode) iter.next();
            if (leaf.getLineno() > 0) return leaf.getLineno();
        }

        return def;
    }

    /**
     * Calculates lineno of the rightmost lexeme with positive lineno of provided parse node.
     * Sometimes the rightmost lexeme is T_EPSILON with lineno == -2. If so, the previous lexeme
     * is taken.
     * @param node parse tree
     * @return lineno of the rightmost lexeme or -2 if is is unavailable or <0.
     */
    public static int getLinenoRight(ParseNode node) {
        return getLinenoRight(node, -2);
    }

    /**
     * Calculates lineno of the rightmost lexeme with positive lineno of provided parse node.
     * Sometimes the rightmost lexeme is T_EPSILON with lineno == -2. If so, the previous lexeme
     * is taken.
     * @param node parse tree
     * @param def default value
     * @return lineno of the rightmost lexeme or default value if is is unavailable or <0.
     */
    public static int getLinenoRight(ParseNode node, int def) {
        ParseTree tree = new ParseTree(node);
        int lineno = def;
        Iterator iter = tree.leafIterator();
        while (iter.hasNext()) {
            ParseNode leaf = (ParseNode) iter.next();
            if (leaf.getLineno() > 0) lineno = leaf.getLineno();
        }

        return lineno;
    }

    /**
     * Dumps cfg analysis info into map per line. Is used while printing ParseTree into source code with
     * analysis info in comments. Example:<br/>
     * <code>
     * Map&lt;Integer, StringWriter&gt; analysisInfo = new HashMap&lt;Integer, StringWriter&gt;();<br/>
     * Utils.buildAnnotations(analysisInfo, pixyChecker.literalAnalysis, tac.getMainFunction().getCfg());<br/>
     * Utils.printTree(rootParseNode, analysisInfo);<br/>
     * </code>
     *
     * @param results  - int-to-StringWriter map, used to dump info per line
     * @param analysis - analysis object that was used for analysing CFG
     * @param cfg      - cfg to dump analysis info
     */
    public static void buildAnnotations(Map<Integer, StringWriter> results, InterAnalysis analysis, Cfg cfg) {
        if (cfg == null || results == null) {
            throw new IllegalArgumentException("results and cfg must be not null");
        }

        try {
            ArrayList<CfgNode> cfgNodes = new ArrayList<CfgNode>();
            cfgNodes.add(cfg.getHead());
            for (int i = 0; i < cfgNodes.size(); i++) {
                CfgNode cfgNode = cfgNodes.get(i);
                for (CfgNode successor : cfgNode.getSuccessors()) {
                    if (!cfgNodes.contains(successor)) {
                        cfgNodes.add(successor);
                    }
                }

                int lineno = Utils.getLinenoLeft(cfgNode.getParseNode());
                if (!results.containsKey(lineno)) {
                    results.put(lineno, new StringWriter());
                }

                InterAnalysisNode node = analysis.getAnalysisNode(cfgNode);
                if (node != null) {
                    Dumper.dump(node.getUnrecycledFoldedValue(), results.get(lineno));
                }
            }


        } catch (IOException e) {
            /* cannot be with StringWriter */
        }
    }

    /**
     * Dumps analysis info for all functions into map per line. Is used while printing ParseTree into source code with
     * analysis info in comments. Example:<br/>
     * <code>
     * Map&lt;Integer, StringWriter&gt; analysisInfo = new HashMap&lt;Integer, StringWriter&gt;();<br/>
     * Utils.buildAnnotations(analysisInfo, pixyChecker.literalAnalysis, tac.getAllFunctions());<br/>
     * Utils.printTree(rootParseNode, analysisInfo);<br/>
     * </code>
     *
     * @param results   - int-to-StringWriter map, used to dump info per line
     * @param analysis  - analysis object that was used for analysing CFG
     * @param functions - list of functions to dump cfg info from
     */
    public static void buildAnnotations(Map<Integer, StringWriter> results, InterAnalysis analysis, List<TacFunction> functions) {
        for (TacFunction func : functions) {
            buildAnnotations(results, analysis, func.getCfg());
        }
    }


    /**
     * Builds parse tree from file provided in the argument.
     * @param file filename to parse
     * @return resulting ParseNode treee
     */
    public static ParseNode buildParseTree(String file) {
        return buildParseTree(file, true);
    }

    /**
     * Resets every Builds parse tree from file provided in the argument.
     * @param file filename to parse
     * @param needBuiltins do we need render built-in functions?
     * @return resulting ParseNode treee
     */
    public static ParseNode buildParseTree(String file, boolean needBuiltins) {
        // Reset current state
        ASTVisitor.reset();
        FunctionManager.getInstance().reset();
        ClassManager.getInstance().reset();

        // Get main module name
        String module = new File(file).getName();
        module = module.substring(0, module.indexOf('.'));

        // Build symbol table
        SymbolTable symbolTable = buildSymbolTable(file, null, module);

        // Build parse tree without declarations
        ParseNode woDeclarations = buildParseTree(file, symbolTable, module);
        // Render declarations and add them to the beginning of the code
        ParseNode rootNode = renderDeclarations(woDeclarations, file, needBuiltins);

        // Perform postprocessing
        TransformationScheduler sched = new TransformationScheduler();
        TemporaryVars temporaryVars = new TemporaryVars();
        sched.addTranformer(new FunctionArgExprExtractor(temporaryVars), 0);
        sched.addTranformer(new ArrayTargetExprExtractor(temporaryVars), 0);
        sched.addTranformer(new ArrayInitializerTransformer(temporaryVars), 0);
        sched.addTranformer(new FunctionCallInliner(temporaryVars), 1);

        sched.findAndTransform(rootNode);

        return rootNode;
    }

    /**
     * Resets every Builds parse tree from file provided in the argument using provided symbol table.
     * @param file filename to parse
     * @param symTable symbol table
     * @param module current module name
     * @return resulting ParseNode treee
     */
    public static ParseNode buildParseTree(String file, SymbolTable symTable, String module) {
        // If modulename is blank, calculate if from the filename
        if (isBlank(module)) {
            module = new File(file).getName();
            module = module.substring(0, module.indexOf('.'));
        }
        try {
            // Parse .xml file using JDOM
            SAXBuilder parser = new SAXBuilder();
            Document doc = parser.build(new File(file));
            Element root = doc.getRootElement();

            // Walk through JDOM tree and build parse nodes for every jdom node.
            JdomVisitor jdomVisitor = new ParseNodeBuilder(file, symTable, module);
            JdomWalker jdomWalker = new JdomWalker(jdomVisitor);
            Node rootNode = jdomWalker.walkWideReverse(root);

            return rootNode.getParseNode();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Render function declarations and add them to beginning of the code in parse tree
     * @param S - destination <code>PhpSymbols.S</code> parse node
     * @param file file name used to construct non-terminal parse nodes
     * @param needBuiltins do we need to load and render built-in functions
     * @return S parse node with rendered declarations
     */
    private static ParseNode renderDeclarations(ParseNode S, String file, boolean needBuiltins) {
        Map<String, Function> functions = FunctionManager.getInstance().getFunctions();

        ParseNode top_statement_list = S.getChild(0);
        while (top_statement_list.getNumChildren() > 1) {
            top_statement_list = top_statement_list.getChild(0);
        }

        // Process classes hierarchy
        Map<String, PyClass> classes = ClassManager.getInstance().getClasses();
        Map.Entry<String, PyClass>[] classesArray = new Map.Entry[classes.size()];
        classes.entrySet().toArray(classesArray);
        for (int i = classesArray.length - 1; i >= 0; i--) {
            Map.Entry<String, PyClass> pyClass = classesArray[i];

            for (PyClass baseClass : pyClass.getValue().getBaseClasses()) {
                if (baseClass != null) {
                    for (Method method : baseClass.getMethods()) {
                        if (pyClass.getValue().hasMethod(method.getName())) {
                            continue;
                        }
                        method.addClass(pyClass.getKey(), pyClass.getValue().getSource());
                    }
                }
            }

            pyClass.getValue().makeMainConstructor(file, 1);
        }

        // add builtin functions
        if (needBuiltins) {
            loadBuiltins();
        }

        // render declarations
        Object[] entries = functions.entrySet().toArray();
        for (int i = entries.length - 1; i >= 0; i--) {
            Map.Entry<String, Function> func = (Map.Entry<String, Function>) entries[i];
            if (func.getKey().matches(".*___\\d+")) continue;
            ParseNode top_statement = new ParseNode(
                    PhpSymbols.top_statement, "top_statement", file);
            top_statement.addChild(func.getValue().render());

            ParseNode tmp = new ParseNode(
                    PhpSymbols.top_statement_list, "top_statement_list", file);
            tmp.setParent(top_statement_list.getParent());
            top_statement_list.getParent().getChildren().set(0, tmp);

            tmp.addChild(top_statement_list);
            tmp.addChild(top_statement);

            top_statement_list = tmp;
        }
        return S;
    }

    /**
     * Walk through modules.xml and register all the builtin functions.
     */
    public static void loadBuiltins() {
        // Walk through builtin modules
        for (Object obj : Converter.modulesConfig.getRootElement().getChildren("Module")) {
            Element module = (Element) obj;
            String moduleName = module.getAttributeValue("name");
            StringBuffer buf = new StringBuffer();
            for (String m : moduleName.split("\\.")) {
                if (buf.length() > 0) buf.append('.');
                buf.append(m);
            }

            // Walk through builtin functions
            for (Object func : module.getChildren("Function")) {
                Element function = (Element) func;
                String functionName = function.getAttributeValue("name");
                String fullFunctionName = functionName;
                if (!moduleName.equals(SymbolTable.BUILTIN_MODULE)) {
                    fullFunctionName = moduleName.replaceAll("\\.", "__") + "__" + fullFunctionName;
                }

                if (function.getText() != null && !"".equals(function.getText())) {
                    ParseNode phpCode = null;
                    try {
                        phpCode = loadBuiltinFunc(fullFunctionName, function.getText());
                    } catch (Exception e) {
                        log.error("Error parsing builtin function: " + fullFunctionName, e);
                    }

                    if (phpCode != null) {
                        Function builtinFunc = new Function();
                        builtinFunc.setCurrentFile(SymbolTable.CONFIG_MODULES_XML);
                        builtinFunc.setModule(SymbolTable.BUILTIN_MODULE);
                        builtinFunc.setName(fullFunctionName);
                        builtinFunc.setOriginalName(fullFunctionName);
                        builtinFunc.setCode(phpCode);
                        Attribute inlineAttr = function.getAttribute("inline");
                        builtinFunc.setInline(inlineAttr != null && "true".equals(inlineAttr.getValue()));
                        FunctionManager.getInstance().addFunction(functionName, builtinFunc);
                    }
                }

                // todo: the same, but __builtin____functionName for correct responding on builtin classes ?
/*
                addSymbol(moduleName + "__" + fullFunctionName, new Symbol(
                        functionName, Symbol.Type.FUNCTION, "/config/modules.xml", moduleName, true));
*/
            }
        }
    }

    /**
     * Parses php source code and find declaration_statement of function <code>name</code>
     * @param name name of function to find
     * @param phpCode php source code
     * @return declaration_statement parse node or null if it is not found or function name is not <code>name</code>
     * @throws Exception
     */
    private static ParseNode loadBuiltinFunc(String name, String phpCode) throws Exception {
        PhpLexer phpLexer = new PhpLexer(new StringReader("<?php\n" + phpCode + "\n?>"));
        phpLexer.setFileName(SymbolTable.BUILTIN_MODULE);
        PhpParser phpParser = new PhpParser(phpLexer);
        ParseNode toplevel = (ParseNode) phpParser.parse().value;
        ParseNode declaration = findDeclarationStatement(toplevel);
        if (declaration == null) {
            log.error("Function declaration of " + name + " not found");
            return null;
        } else {
            ParseNode funcNode = declaration.getChild(0).getChild(0);
            ParseNode nameNode = declaration.getChild(0).getChild(2);

            if (funcNode.getSymbol() != PhpSymbols.T_FUNCTION) {
                log.error("First declaration in text of " + name + " is not a function");
                return null;
            }

            if (!nameNode.getLexeme().equals(name)) {
                log.error("Function name of " + name + " must be named: " + name);
                return null;
            }
        }

        return declaration;
    }

    /**
     * Find first declaration_statement node among provided parsenode children.
     * @param node some parse node
     * @return declaration_statement node if found, null otherwise
     */
    private static ParseNode findDeclarationStatement(ParseNode node) {
        if (node.getSymbol() == PhpSymbols.declaration_statement) {
            return node;
        } else {
            ParseNode result;
            for (Object c : node.getChildren()) {
                ParseNode child = (ParseNode) c;
                result = findDeclarationStatement(child);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }
    }

    /**
     * Builds symbol table for provided file.
     * @param file filename of processed file
     * @return built symbol table
     */
    public static SymbolTable buildSymbolTable(String file) {
        return buildSymbolTable(file, null, null);
    }

    /**
     * Builds symbol table for provided file. If <code>symbolTable</code> is provided, adds new lexemes to it.
     * @param file filename of processed file
     * @param symbolTable symbol table to use. If null, new one will be created
     * @param module current module name
     * @return built sybmol table
     */
    public static SymbolTable buildSymbolTable(String file, SymbolTable symbolTable, String module) {
        try {
            SAXBuilder parser = new SAXBuilder();
            Document doc = parser.build(new File(file));
            Element root = doc.getRootElement();

            SymbolTableBuilder symTableBuilder = new SymbolTableBuilder(symbolTable, file, module);
            JdomWalker symWalker = new JdomWalker(symTableBuilder);
            symWalker.walkWide(root);

            return symTableBuilder.getSymbolTable();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Renames all the <code>oldLexemes</code> lexemes in the tree to <code>newLexeme</code>
     * @param tree parse tree walk throug
     * @param oldLexeme old lexeme string
     * @param newLexeme new lexeme string
     */
    public static void renameLexemes(ParseNode tree, final String oldLexeme, final String newLexeme) {
        ParseNodeWalker walker = new ParseNodeWalker(new ParseNodeVisitor() {
            public void visit(ParseNode node) {
                if (node.isToken() && node.getLexeme().equals(oldLexeme)) {
                    node.setLexeme(newLexeme);
                }
            }
        });
        walker.walk(tree);
    }

}
