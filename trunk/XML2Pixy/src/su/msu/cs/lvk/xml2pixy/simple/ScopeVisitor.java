package su.msu.cs.lvk.xml2pixy.simple;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.Visitor;
import su.msu.cs.lvk.xml2pixy.ast.Walker;
import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleHandler;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleNotFoundException;
import su.msu.cs.lvk.xml2pixy.simple.classes.ClassUtils;
import su.msu.cs.lvk.xml2pixy.simple.classes.PyClass;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.lang.reflect.Method;

/**
 * User: KlimovGA
 * Date: 07.12.2008
 */
public class ScopeVisitor implements Visitor {

    private static final Logger logger = Logger.getLogger(ScopeVisitor.class);

    private String module;

    public boolean visit(ASTNode node) throws VisitorException {
        PythonNode pyNode = (PythonNode) node;
/*
        if (pyNode.getScope().isFinished()) {
            return true;
        }
*/

        String nodeClassName = pyNode.getClass().getName();
        nodeClassName = nodeClassName.substring(nodeClassName.lastIndexOf('.') + 1);
        try {
            Method method = getClass().getMethod("visit" + nodeClassName, PythonNode.class);
            method.invoke(this, pyNode);
        } catch (NoSuchMethodException e) {
            defaultVisit(pyNode);
        } catch (Exception e) {
            throw new VisitorException(e);
        }
        return true;
    }

    public void defaultVisit(PythonNode node) {
        logger.trace(node.getNodeName());
    }

    public void visitModuleNode(PythonNode node) {
        ModuleNode currentModule = (ModuleNode) node;
        ModuleHandler.addModule(currentModule.getName(), currentModule);
    }

    public void visitImportNode(PythonNode node) throws ModuleNotFoundException {
        ImportNode importNode = (ImportNode) node;

        for (String moduleName : importNode.getNames()) {
            ScopeNode currentScope = importNode.getScope();

            String[] modules = moduleName.split("\\.");

            boolean useGlobal = true; // we should allow global modules at first level
            // we should import every module in mod1.mod2.mod3. i.e. import mod2 in mod1
            for (String localModule : modules) {
                ModuleNode module;
                try {
                    module = ModuleHandler.importModule(currentScope.getGlobalScope(), localModule, useGlobal);
                } catch (ModuleNotFoundException e) {
                    throw new ModuleNotFoundException("Couldn't find module " + moduleName + " from " + importNode.getLocation(), e);
                }
                ProcessingUtils.processScopes(module, module.getName());

                Symbol symbol = new Symbol(localModule, Symbol.Type.MODULE, node.getFileName(), this.module, node.getLineno());
                symbol.setSource(module);

                currentScope.addSymbol(localModule, symbol);

                currentScope = module;

                useGlobal = false; // prohibit searching global module 'ast' when importing 'compiler.ast'
            }
        }

    }

    public void visitFromNode(PythonNode node) throws ModuleNotFoundException {
        FromNode from = (FromNode) node;

        String moduleName = from.getModule();
        ScopeNode currentScope = from.getScope();

        String[] modules = moduleName.split("\\.");

        // we should import every module in mod1.mod2.mod3. i.e. import mod2 in mod1
        boolean useGlobal = true;
        for (String localModule : modules) {
            ModuleNode module;
            try {
                module = ModuleHandler.importModule(currentScope.getGlobalScope(), localModule, useGlobal);
            } catch (ModuleNotFoundException e) {
                throw new ModuleNotFoundException("Couldn't find module " + moduleName + " from " + from.getLocation(), e);
            }
            ProcessingUtils.processScopes(module, module.getName());

            currentScope = module;

            useGlobal = false; // prohibit searching global module 'ast' when importing 'compiler.ast'
        }
        // after this we have the target module in currentScope variable

        boolean all = from.getNames().size() == 1 && "*".equals(from.getNames().get(0));
        // iterate over symbols in target module and copy needed ones.
        for (Symbol symbol : currentScope.getSymbolTable().getAllSymbols().values()) {
            if (all || from.getNames().contains(symbol.getName())) {
                try {
                    Symbol newSymbol = symbol.clone();
                    newSymbol.setFile(from.getFileName()); // set current fileName
                    newSymbol.setLineno(from.getLineno()); // set current lineno
                    newSymbol.setModule(this.module); // set current module

                    from.getScope().addSymbol(newSymbol.getName(), newSymbol);
                } catch (CloneNotSupportedException e) {
                    logger.error(e);
                }

            }
        }
    }

    public void visitGlobalNode(PythonNode node) {
        GlobalNode global = (GlobalNode) node;
        ScopeNode scope = global.getScope();
        for (String globalVar : global.getNames()) {
            Symbol symbol = new Symbol(globalVar, global.getFileName(), global.getLineno());
            scope.addGlobal(globalVar, symbol);
        }
    }

    public void visitClassNode(PythonNode node) {
        ClassNode classNode = (ClassNode) node;
        String className = classNode.getName();
        ScopeNode scope = classNode.getParent().getScope();
        Symbol symbol = new Symbol(className, Symbol.Type.CLASS, node.getFileName(), this.module, node.getLineno());
        symbol.setSource(node);
        scope.addSymbol(className, symbol);

        ClassUtils.addClass(new PyClass(classNode));
    }

    public void visitFunctionNode(PythonNode node) throws ProcessingException {
        FunctionNode functionNode = (FunctionNode) node;
        String functionName = functionNode.getName();
        ScopeNode scope = functionNode.getParentScope();
        Symbol.Type type = testIfGenerator(functionNode);
        Symbol symbol = new Symbol(functionName, type, node.getFileName(), this.module, node.getLineno());
        symbol.setSource(node);
        scope.addSymbol(functionName, symbol);

        // if function belongs to class, register it as class method
        if (scope instanceof ClassNode) {
            ClassUtils.registerMethod((ClassNode) scope, functionNode);
        }
    }

    public void visitAssNameNode(PythonNode node) {
        AssNameNode assNameNode = (AssNameNode) node;
        if (!assNameNode.isDelete()) {
            String varName = assNameNode.getName();
            ScopeNode scope = assNameNode.getParent().getScope();
            Symbol symbol = new Symbol(varName, node.getFileName(), node.getLineno());
            symbol.setModule(this.module);

            ProcessingUtils.tryDefineSymbol(symbol, assNameNode.getParent());
            if (symbol.getType() == null) {
                setVariable(symbol, node);
            }

            scope.addSymbol(varName, symbol);

            if (scope instanceof ClassNode && symbol.isFunction()) {
                ClassUtils.registerMethod((ClassNode) scope, symbol);
            }
        }
    }

    public void visitAssAttrNode(PythonNode node) {
        AssAttrNode assAttrNode = (AssAttrNode) node;
        Symbol scopeSymbol = ProcessingUtils.trySymbol(assAttrNode.getExpr());
        if (scopeSymbol != null && (scopeSymbol.isClass() || scopeSymbol.isModule())) {
            ScopeNode scope = (ScopeNode) scopeSymbol.getSource();
            Symbol symbol = new Symbol(assAttrNode.getAttrName(), node.getFileName(), node.getLineno());
            symbol.setModule(scope.getGlobalScope().getName());

            ProcessingUtils.tryDefineSymbol(symbol, assAttrNode.getParent());
            if (symbol.getType() == null) {
                setVariable(symbol, node);
            }

            scope.addSymbol(symbol.getName(), symbol);

            if (scope instanceof ClassNode && symbol.isFunction()) {
                ClassUtils.registerMethod((ClassNode) scope, symbol);
            }
        }
    }

    public void visitLambdaNode(PythonNode node) {
        LambdaNode lambda = (LambdaNode) node;

        String functionName = ProcessingUtils.getNextLambda();
        ScopeNode scope = lambda.getScope();
        Symbol symbol = new Symbol(functionName, Symbol.Type.FUNCTION, node.getFileName(), this.module, node.getLineno());
        symbol.setSource(node);
        scope.addSymbol(functionName, symbol);
        lambda.setGeneratedName(functionName);

    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    private void setVariable(Symbol symbol, PythonNode node) {
        symbol.setSource(node);
        symbol.setType(Symbol.Type.VARIABLE);
    }

    private Symbol.Type testIfGenerator(FunctionNode function) throws ProcessingException {
        GeneratorFinder visitor = new GeneratorFinder();
        Walker walker = new Walker(visitor);
        walker.walkDeep(function);

        if (visitor.isGenerator()) {
            if (visitor.isFunction()) {
                throw new ProcessingException("SyntaxError: Generator function with return with arguments (" +
                        function.getLocation() + ")");
            }
            return Symbol.Type.GENERATOR_FUNCTION;
        }

        return Symbol.Type.FUNCTION;
    }

    private static class GeneratorFinder implements Visitor {
        private boolean generator;
        private boolean function;

        public boolean visit(ASTNode node) throws VisitorException {
            if (node instanceof YieldNode) {
                generator = true;
            } else if (node instanceof ReturnNode) {
                ReturnNode returnNode = (ReturnNode) node;
                if (returnNode.getValue() != null) {
                    function = true;
                }
            }
            return true;
        }

        public boolean isGenerator() {
            return generator;
        }

        public boolean isFunction() {
            return function;
        }
    }

}
