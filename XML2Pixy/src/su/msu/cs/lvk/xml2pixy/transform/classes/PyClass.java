package su.msu.cs.lvk.xml2pixy.transform.classes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.function.Constructor;
import su.msu.cs.lvk.xml2pixy.transform.function.Function;
import su.msu.cs.lvk.xml2pixy.transform.function.FunctionManager;
import su.msu.cs.lvk.xml2pixy.transform.function.Method;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper for python classes
 */
public class PyClass extends ASTVisitor {

    protected static Logger log = Logger.getLogger(PyClass.class.getName());

    private String originalName;
    private String name;
    private String module;
    private List<PyClass> baseClasses;
    private List<String> baseClassesNames;

    private List<Method> methods;
    private List<Method> ownMethods;

    private Node source;
    private Constructor constructor;

    public PyClass(String name, Node node) {
        this.name = name;
        this.originalName = node.getJdomElement().getAttributeValue("name");
        this.source = node;
//        this.baseClasses = new ArrayList<PyClass>();
        this.baseClassesNames = new ArrayList<String>();
        this.methods = new ArrayList<Method>();
        this.ownMethods = new ArrayList<Method>();
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return list of PyClass objects for base classes provided in declaration
     */
    public List<PyClass> getBaseClasses() {
        if (baseClasses == null) {
            baseClasses = new ArrayList<PyClass>();
            for (String name : baseClassesNames) {
                PyClass clazz = ClassManager.getInstance().getPyClass(name);
                baseClasses.add(clazz);
            }
        }
        return baseClasses;
    }

    public void setBaseClasses(List<PyClass> baseClasses) {
        this.baseClasses = baseClasses;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void addBaseClass(String str) {
        baseClassesNames.add(str);
    }

    public List<Method> getOwnMethods() {
        return ownMethods;
    }

    /**
     * @param methodName method name
     * @return true if class or one of its base classes has a method with given name, false otherwise
     */
    public boolean hasMethod(String methodName) {
        for (Method meth : methods) {
            if (meth.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param methodName method name
     * @return true if class itself has a method with given name, false otherwise
     */
    public boolean hasOwnMethod(String methodName) {
        for (Method meth : ownMethods) {
            if (meth.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes function used to create objects.
     * @param currentFile file to bind this function to
     * @param lineno line number to bind this function to
     * @return built Function object
     */
    public Function makeMainConstructor(String currentFile, int lineno) {
        Function function = new Function();
        Node body = new Node();
        Constructor calledConstructor = findConstructor();
        ParseNode[] args = calledConstructor == null ? new ParseNode[1]
                : new ParseNode[calledConstructor.getArguments().length];
        String initName = "__init__" + name + args.length;

        // add classname setter
        // seft["__class_name__"] = "ClassName"
        ParseNode classNameSetter = emptyStatementList();
        classNameSetter = addFirstTopStatement(
                classNameSetter,
                renderClassNameSetter("self", name, currentFile, lineno)
        );

        // make call for this class' found constructor
        if (calledConstructor != null) {
            int i = 0;
            // make arguments for called constructor
            for (String arg : calledConstructor.getArguments()) {
                if (arg.startsWith("&")) {
                    // remove reference sign
                    arg = arg.substring(1);
                } else {
                    throw new IllegalArgumentException("Constructor args should use references only!");
                }
                args[i++] = makeReferenceVariableByName(arg, lineno);
            }

            // add constructor call
            classNameSetter = addLastTopStatement(
                    classNameSetter,
                    makeStatementFromExpr(
                            makeFunctionCall(calledConstructor.getName(), args, null, lineno),
                            lineno)
            );
        }

        // add "return self" statement
        classNameSetter = addLastTopStatement(
                classNameSetter,
                renderReturnSelf("self", currentFile, lineno)
        );

        body.setParseNode(classNameSetter);

        function.setName(initName);
        function.setSource(getFirstChild(getFirstChild(source, "code"), null));
        function.setOriginalName("__init__");
        function.setArguments(calledConstructor == null ? new String[]{"&self"} : calledConstructor.getArguments());
        function.setBody(body);
        function.setModule(module);
        function.setCurrentFile(currentFile);

        FunctionManager.getInstance().addFunction(initName, function);

        return function;
    }

    /**
     * Looks for non-default constructor through base classes left to right deep-firstly. 
     * @return found constructor or null if there's only default constructor
     */
    private Constructor findConstructor() {
        if (constructor != null && !constructor.isGenerated()) {
            return constructor;
        }
        LinkedList<PyClass> baseClasses = new LinkedList<PyClass>(this.baseClasses);
        for (int i = 0; i < baseClasses.size(); i++) {
            PyClass clazz = baseClasses.get(i);
            if (clazz == null) {
//                throw new IllegalArgumentException("null baseClass (" + this.name + ")"); TODO empty base class
                log.warn("WARNING: missing base class: " + this.name);
//                return null;
                continue;
            }
            if (clazz.getConstructor() != null) {
                return clazz.getConstructor();
            } else {
                for (PyClass base : clazz.getBaseClasses()) {
                    if (i < baseClasses.size() - 1) {
                        baseClasses.add(i + 1, base);
                    } else {
                        baseClasses.add(base);
                    }
                }
            }
        }

        return null;
    }

    private ParseNode renderClassNameSetter(String arg, String className, String currentFile, int lineno) {
        ParseNode expr = new ParseNode(
                PhpSymbols.expr, "expr", currentFile);
        ParseNode expr_without_variable = new ParseNode(
                PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
        expr.addChild(expr_without_variable);

        ParseNode cvar = new ParseNode(PhpSymbols.cvar, "cvar", currentFile);
        ParseNode cvar_without_objects = new ParseNode(
                PhpSymbols.cvar_without_objects, "cvar_without_objects", currentFile);
        ParseNode reference_variable = new ParseNode(
                PhpSymbols.reference_variable, "reference_variable", currentFile);
        ParseNode var = new ParseNode(
                PhpSymbols.reference_variable, "reference_variable", currentFile);
        ParseNode compound_variable = new ParseNode(
                PhpSymbols.compound_variable, "compound_variable", currentFile);
        ParseNode dim_offset = new ParseNode(
                PhpSymbols.dim_offset, "dim_offset", currentFile);

        cvar.addChild(cvar_without_objects);
        cvar_without_objects.addChild(reference_variable);
        reference_variable.addChild(var);
        reference_variable.addChild(new ParseNode(
                PhpSymbols.T_OPEN_RECT_BRACES, "T_OPEN_RECT_BRACES", currentFile, "[", lineno));
        reference_variable.addChild(dim_offset);
        reference_variable.addChild(new ParseNode(
                PhpSymbols.T_CLOSE_RECT_BRACES, "T_CLOSE_RECT_BRACES", currentFile, "]", lineno));
        var.addChild(compound_variable);
        compound_variable.addChild(new ParseNode(
                PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile, "$" + arg, lineno));
        dim_offset.addChild(renderStringConstant(lineno, FunctionManager.TYPE_KEY));

        expr_without_variable.addChild(cvar);
        expr_without_variable.addChild(new ParseNode(
                PhpSymbols.T_ASSIGN, "T_ASSIGN", currentFile, "=", lineno));
        expr_without_variable.addChild(renderStringConstant(lineno, className));

        //Wrap expression into statement
        ParseNode statement = new ParseNode(
                PhpSymbols.statement, "statement", currentFile);
        ParseNode unticked_statement = new ParseNode(
                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
        statement.addChild(unticked_statement);

        unticked_statement.addChild(expr);
        unticked_statement.addChild(
                new ParseNode(PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));

        return statement;

    }

    private ParseNode renderReturnSelf(String arg, String currentFile, int lineno) {
        ParseNode statement = new ParseNode(
                PhpSymbols.statement, "statement", currentFile);
        ParseNode unticked_statement = new ParseNode(
                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
        statement.addChild(unticked_statement);

        ParseNode cvar = new ParseNode(
                PhpSymbols.cvar, "cvar", currentFile);
        ParseNode cvar_without_objects = new ParseNode(
                PhpSymbols.cvar_without_objects, "cvar_without_objects", currentFile);
        ParseNode reference_variable = new ParseNode(
                PhpSymbols.reference_variable, "reference_variable", currentFile);
        ParseNode compound_variable = new ParseNode(
                PhpSymbols.compound_variable, "compound_variable", currentFile);
        cvar.addChild(cvar_without_objects);
        cvar_without_objects.addChild(reference_variable);
        reference_variable.addChild(compound_variable);
        compound_variable.addChild(new ParseNode(
                PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile, "$" + arg, lineno));


        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_RETURN, "T_RETURN", currentFile, "return", lineno));
        unticked_statement.addChild(cvar);
        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));

        return statement;
    }

}
