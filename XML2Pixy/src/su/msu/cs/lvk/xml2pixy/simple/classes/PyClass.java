package su.msu.cs.lvk.xml2pixy.simple.classes;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;

import java.util.*;

/**
 * User: klimov
 * Date: 11.01.2009
 */
public class PyClass {

    public static final String CONSTRUCTOR_NAME = "__init__";
    public static final String CLASS_NAME_FIELD = "__class_name__";

    private final static Logger logger = Logger.getLogger(PyClass.class);

    private String fullName;
    private String name;
    private String module;

    private ClassNode source;
    private Map<String, FunctionNode> methods;
    private List<PyClass> baseClasses;
    private FunctionNode constructor;

    public PyClass(PythonNode node) {
        if (!(node instanceof ClassNode))
            throw new IllegalArgumentException("ClassNode expected (" + node.getLocation() + ")");

        baseClasses = new ArrayList<PyClass>();
        methods = new HashMap<String, FunctionNode>();

        source = (ClassNode) node;
        name = source.getName();
        module = source.getGlobalScope().getName();
        fullName = ClassUtils.getFullClassName(source);

        for (PythonNode baseNode : source.getBases()) {
            Symbol baseSym = ProcessingUtils.trySymbol(baseNode);
            if (baseSym != null) {
                if (!baseSym.isClass()) {
                    logger.warn("");
                } else {
                    String fullName = ClassUtils.getFullClassName((ClassNode) baseSym.getSource());
                    PyClass baseClass = ClassUtils.getPyClass(fullName);
                    if (baseClass != null) {
                        baseClasses.add(baseClass);
                        addBaseMethods(baseClass);
                    } else {
                        logger.warn("Couldn't find base class " + fullName + " at " + node.getLocation());
                    }
                }
            } else {
                logger.warn("Couldn't find symbol " + baseNode.toString() + " at " + source.getLocation());
            }
        }
    }

    protected void addBaseMethods(PyClass baseClass) {
        for (Map.Entry<String, FunctionNode> entry : baseClass.getMethods().entrySet()) {
            ClassUtils.registerMethod(this, entry.getKey(), entry.getValue());
        }
    }

    public void addMethod(String name, FunctionNode method) {
        if (CONSTRUCTOR_NAME.equals(name)) {
            constructor = method;
        } else {
            methods.put(name, method);
        }
    }

    public FunctionNode renderConstructor() {
        FunctionNode function = new FunctionNode();

        function.setName(ProcessingUtils.concatMangled(CONSTRUCTOR_NAME, ProcessingUtils.getMangledName(fullName)));

        FunctionNode factConstructor = findConstructor();
        String self = "self";

        StmtNode code = new StmtNode();
        code.addNode(new AssignNode(
                new AssNameNode(self),
                new DictNode(
                        new ArrayList<PythonNode>(Arrays.asList(new ConstNode(CLASS_NAME_FIELD))),
                        new ArrayList<PythonNode>(Arrays.asList(new ConstNode(fullName)))
                )
        )); // self = { '__class_name__' : className }

        if (factConstructor != null) {
            function.setArgNames(new ArrayList<String>(factConstructor.getArgNames()));
            function.getArgNames().remove(0); // make constructor without 'self' parameter, we create array here
            function.setDefaults(factConstructor.getDefaults());
            function.setKwArg(factConstructor.getKwArg());
            function.setVarArg(factConstructor.getVarArg());

            List<PythonNode> factArgs = new ArrayList<PythonNode>();
            factArgs.add(new NameNode(self));
            for (String arg : function.getArgNames()) {
                factArgs.add(new NameNode(arg));
            }
            PythonNode starArgs = factConstructor.getVarArg() == null ? null : new NameNode(factConstructor.getVarArg());
            PythonNode dstarArgs = factConstructor.getKwArg() == null ? null : new NameNode(factConstructor.getKwArg());

            code.addNode(new CallFuncNode(
                    new NameNode(factConstructor.getName()),
                    factArgs,
                    starArgs,
                    dstarArgs
            )); // module__class____init__(self, ...)
        } else {
            function.setArgNames(Collections.<String>emptyList());
        }

        code.addNode(new ReturnNode(new NameNode(self))); // return self;

        function.setCode(code);
        return function;
    }

    public FunctionNode findConstructor() {
        if (this.constructor != null) return this.constructor;

        for (PyClass baseClass : getBaseClasses()) {
            FunctionNode constructor = baseClass.findConstructor();
            if (constructor != null) return constructor;
        }

        return null;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public ClassNode getSource() {
        return source;
    }

    public void setSource(ClassNode source) {
        this.source = source;
    }

    public Map<String, FunctionNode> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, FunctionNode> methods) {
        this.methods = methods;
    }

    public List<PyClass> getBaseClasses() {
        return baseClasses;
    }

    public void setBaseClasses(List<PyClass> baseClasses) {
        this.baseClasses = baseClasses;
    }

    public FunctionNode getConstructor() {
        return constructor;
    }

    public void setConstructor(FunctionNode constructor) {
        this.constructor = constructor;
    }

    public String toString() {
        return getFullName() + " (" + getSource().getLocation() + ")";
    }
}
