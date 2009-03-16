package su.msu.cs.lvk.xml2pixy.simple;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.Visitor;
import su.msu.cs.lvk.xml2pixy.ast.Walker;
import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.jdom.JdomVisitor;
import su.msu.cs.lvk.xml2pixy.jdom.JdomWalker;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleHandler;
import su.msu.cs.lvk.xml2pixy.simple.classes.ClassUtils;
import su.msu.cs.lvk.xml2pixy.simple.classes.Method;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.io.File;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 07.10.2008
 * Time: 22:51:30
 */
public class ProcessingUtils {

    public static final String GENERATOR_NEXT = "next";

    private static final Logger logger = Logger.getLogger(ProcessingUtils.class);

    private static int tempVarCount = 0;
    public static final String DELETE_FLAG = "OP_DELETE";
    public static final String ASSIGN_FLAG = "OP_ASSIGN";
    public static final String APPLY_FLAG = "OP_APPLY";

    public static String getNextTempVar() {
        return "__t_" + (++tempVarCount);
    }

    public static String getNextLambda() {
        return "__lambda_" + (++tempVarCount);
    }

    public static String getNextTempFunction(String prefix) {
        return prefix + (++tempVarCount);
    }

    public static ModuleNode parseFile(String file) throws ProcessingException {
        try {
            SAXBuilder parser = new SAXBuilder();
            Document doc = parser.build(Utils.getFileStream(file));
            Element root = doc.getRootElement();


            Node rootNode = new JdomWalker(new JdomVisitor() {
                public void visit(Node node) throws VisitorException {
                    node.setAstNode(PythonNode.makeNode(node));
                }
            }).walkWideReverse(root);
            return (ModuleNode) rootNode.getAstNode();//PythonNode.makeNode(root);

        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    public static void processScopes(ScopeNode root, String module) {
        if (!root.isFinished()) {
            // finish all children scopes for preventing multiple processings
            new Walker(new Visitor() {
                public boolean visit(ASTNode node) throws VisitorException {
                    if (node instanceof ScopeNode) {
                        ScopeNode scope = (ScopeNode) node;
                        scope.finishSymbolTable();
                    }
                    return true;
                }
            }).walkDeep(root);

            ScopeVisitor visitor = new ScopeVisitor();
            visitor.setModule(module);
            if (root instanceof ModuleNode) {
                root.setName(module);
            }
            Walker walker = new Walker(visitor);
            walker.walkDeep(root);
        }
    }

    public static void processScopes(ModuleNode root) {
        processScopes(root, getModuleNameByFile(root.getFileName()));
    }

    public static ModuleNode simplifyPython(ModuleNode root, boolean renderDeclarations) {
        Visitor visitor = new Simplifier();
        Walker walker = new Walker(visitor);
        ModuleNode after = (ModuleNode) walker.walkWideReverse(root);

        if (renderDeclarations) {
            root.getStmt().getNodes().addAll(0, ModuleHandler.inlineModules(root.getName()));

            int i = 0;
            List<FunctionNode> functions = ClassUtils.renderMethods();
            for (FunctionNode function : functions) {
                after.getStmt().addNode(i++, function);
            }

        }

        // replace '.' with '__' in names
        visitor = new Visitor() {
            public boolean visit(ASTNode node) throws VisitorException {
                if (node instanceof NameNode) {
                    NameNode name = (NameNode) node;
                    name.setName(getMangledName(name.getName()));
                }
                return true;
            }
        };
        walker = new Walker(visitor);
        after = (ModuleNode) walker.walkDeep(after);

        return after;
    }

    public static ModuleNode ssaTransform(ModuleNode root) {
        Visitor visitor = new SSATransformer();
        Walker walker = new Walker(visitor);
        return (ModuleNode) walker.walkWideReverse(root);
    }

    public static ModuleNode simplifyGenerators(ModuleNode root) {
        Visitor visitor = new GeneratorSimplifier();
        Walker walker = new Walker(visitor);
        return (ModuleNode) walker.walkWideReverse(root);
    }

    public static String getModuleNameByFile(String fileName) {
        File file = new File(fileName);

        String module = StringUtils.substringBefore(file.getName(), ".");
        if (ModuleHandler.INIT_FILE_NAME.equals(module)) {
            module = file.getAbsoluteFile().getParentFile().getName();
        }
        logger.trace("module name found: " + module);
        return module;
    }


    public static Symbol trySymbol(PythonNode node) {

        if (node instanceof NameNode) {
            NameNode name = (NameNode) node;
            return node.getScope().getSymbol(name.getName());
        } else if (node instanceof GetattrNode) {
            GetattrNode getattr = (GetattrNode) node;
            Symbol symbol = trySymbol(getattr.getExpr());
            if (symbol != null && (symbol.isClass() || symbol.isModule())) {
                ScopeNode exprScope = (ScopeNode) symbol.getSource();
                return exprScope.getSymbol(getattr.getAttrName());
            }
        }

        return null;
    }

    public static Symbol trySymbol(ScopeNode scope, String symbolPath) {
        String moduleName = scope.getGlobalScope().getName() + ".";
        if (symbolPath.startsWith(moduleName)) {
            symbolPath = StringUtils.substringAfter(symbolPath, moduleName);
        }
        Symbol symbol = null;
        ScopeNode currentScope = scope;
        for (String name : symbolPath.split("\\.")) {
            symbol = currentScope == null ? null : currentScope.getSymbol(name);
            if (symbol != null && (symbol.isClass() || symbol.isModule())) {
                currentScope = (ScopeNode) symbol.getSource();
            } else {
                currentScope = null;
            }
        }

        return symbol;
    }

    public static void tryDefineSymbol(Symbol symbol, PythonNode assign) {
        if (assign instanceof AssignNode) {
            AssignNode assignment = (AssignNode) assign;
            PythonNode expr = assignment.getExpr();
            if (expr.getClass() == NameNode.class || expr.getClass() == GetattrNode.class) {
                Symbol assignee = trySymbol(assignment.getExpr());
                if (assignee != null) {
                    symbol.setSource(assignee.getSource());
                    symbol.setType(assignee.getType());
                }
            } else if (expr.getClass() == CallFuncNode.class) { // if we call generator function, then mark symbol as generator
                CallFuncNode callFunc = (CallFuncNode) expr;
                PythonNode callee = callFunc.getNode();
                Symbol calleeSymbol = trySymbol(callee);
                if (calleeSymbol != null && calleeSymbol.isGeneratorFunction()) {
                    symbol.setType(Symbol.Type.GENERATOR);
                    symbol.setSource(calleeSymbol.getSource());
                }
            }
        }
    }

    public static String getFullName(ScopeNode scopeNode) {
        if (scopeNode == null) return null;

        String fullName = scopeNode.getName();

        ScopeNode parent = scopeNode.getParentScope();
        while (parent != null) {
            fullName = parent.getName() + '.' + fullName;
            parent = parent.getParentScope();
        }

        return fullName;
    }

    public static String getFullName(NameNode node) {
        String name = node.getName();
        Symbol symbol = node.getScope().getSymbol(name);
        if (symbol != null) {
            if (symbol.isModule()) {
                return ((ModuleNode) symbol.getSource()).getName();
            } else if (symbol.isBuiltin()) {
                return name;
            }
        }
        return getFullName(node.getScope()) + '.' + node.getName();
    }

    public static String getMangledName(ScopeNode node) {
        return getMangledName(getFullName(node));
    }

    public static String getMangledName(NameNode node) {
        String name = node.getName();
        Symbol symbol = node.getScope().getSymbol(name);
        if (symbol != null && symbol.isModule()) {
            return ((ModuleNode) symbol.getSource()).getName();
        }
        return getMangledName(node.getScope(), node.getName());
    }

    public static String getMangledName(ScopeNode node, String name) {
        return concatMangled(getMangledName(node), name);
    }

    public static String getMangledName(String name) {
        return StringUtils.replace(name, ".", "__");
    }

    public static String concatMangled(String str1, String str2) {
        return getMangledName(str1) + "__" + getMangledName(str2);
    }

    public static String getMethodFullName(String methodName, FunctionNode method) {
        return getMethodFullName(methodName, method.getArgNames().size());
    }

    public static String getMethodFullName(String methodName, int argNumber) {
        return Method.DISPATCHER_PREFIX + methodName + "__" + argNumber;
    }

    public static String compare(ParseNode expected, ParseNode found) {
        if (expected.isToken() && found.isToken()) {
            if (!expected.getLexeme().trim().equals(found.getLexeme().trim())) {
                return error("lexeme", expected.getLexeme(), found.getLexeme(), expected.getLineno());
            }
            if (expected.getSymbol() != found.getSymbol()) {
                return error("token", expected.getName(), found.getName(), expected.getLineno());
            }
            return null;
        } else if (!expected.isToken() && !found.isToken()) {
            if (expected.getSymbol() != found.getSymbol()) {
                return error("non-terminal", expected.getName(), found.getName(), Utils.getLinenoLeft(expected));
            }
            List eChildren = expected.getChildren(), fChildren = found.getChildren();
            if (eChildren.size() != fChildren.size()) {
                return error("child number in " + expected.getName(),
                        expected.getChildren().size(),
                        found.getChildren().size(),
                        Utils.getLinenoLeft(expected));
            }
            for (int i = 0; i < eChildren.size(); i++) {
                ParseNode eChild = (ParseNode) eChildren.get(i), fChild = (ParseNode) fChildren.get(i);
                String res = compare(eChild, fChild);
                if (res != null) return res;
            }
            return null;
        } else {
            return error("", expected.isToken() ? "token" : "non-terminal",
                    found.isToken() ? "token" : "non-terminal",
                    Utils.getLinenoLeft(expected));
        }
    }

    public static String error(String diff, Object expected, Object found) {
        return "ERROR : Different " + diff +
                " (expected: " + expected + ", found: " + found + ")";
    }

    public static String error(String diff, Object expected, Object found, int lineno) {
        return "ERROR : Different " + diff +
                " (expected: " + expected + ", found: " + found + ", lineno: " + lineno + ")";
    }
}
