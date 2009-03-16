package su.msu.cs.lvk.xml2pixy.simple.classes;

import su.msu.cs.lvk.xml2pixy.ast.python.*;

import java.util.*;

/**
 * User: klimov
 * Date: 12.01.2009
 */
public class Method {

    public static final String DISPATCHER_PREFIX = "__method__";

    private String name;
    private Map<String, FunctionNode> implementations;

    public Method(String name) {
        this.name = name;
        implementations = new HashMap<String, FunctionNode>();
    }

    public void addClass(PyClass clazz, FunctionNode impl) {
        implementations.put(clazz.getFullName(), impl);
    }

    public Map<String, FunctionNode> getImplementations() {
        return implementations;
    }

    public void setImplementations(Map<String, FunctionNode> implementations) {
        this.implementations = implementations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionNode renderPython() {
        FunctionNode function = new FunctionNode(false);

        String functionName = name;

        function.setName(functionName);

        StmtNode code = new StmtNode();
        function.setCode(code);

        boolean first = true;
        for (Map.Entry<String, FunctionNode> entry : implementations.entrySet()) {
            String className = entry.getKey();
            FunctionNode impl = entry.getValue();

            if (first) {
                function.setVarArg(impl.getVarArg());
                function.setKwArg(impl.getKwArg());
                function.setArgNames(impl.getArgNames());
                first = false;
            }

            code.addNode(renderIf(impl.getName(), className, function.getArgNames(), function.getVarArg(), function.getKwArg()));
        }


        return function;
    }

    protected PythonNode renderIf(String implName, String className, List<String> argNames, String varArg, String kwArg) {

        String self = argNames.get(0);
        PythonNode test = new CompareNode(
                new SubscriptNode(new NameNode(self), new ConstNode(PyClass.CLASS_NAME_FIELD)),
                "==",
                new ConstNode(className)
        );

        PythonNode call = new CallFuncNode(
                new NameNode(implName),
                prepareArguments(argNames),
                varArg == null ? null : new NameNode(varArg),
                kwArg == null ? null : new NameNode(kwArg)
        );
        StmtNode stmt = new StmtNode(call);

        return new IfNode(Arrays.asList(test), Arrays.asList(stmt), null);
    }

    protected List<PythonNode> prepareArguments(List<String> argNames) {
        List<PythonNode> args = new ArrayList<PythonNode>();

        for (String name : argNames) {
            args.add(new NameNode(name));
        }

        return args;
    }

}
