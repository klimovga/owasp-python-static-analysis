package su.msu.cs.lvk.xml2pixy.transform.function;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.classes.ClassManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class FunctionManager {

    protected static Logger log = Logger.getLogger(FunctionManager.class.getName());

    public static String METHOD_PREFIX = "__method__";
    public static String TYPE_KEY = "__class_name__";

    private Map<String, Function> functions;

    private FunctionManager() {
        this.reset();
    }

    public void reset() {
        functions = new LinkedHashMap<String, Function>();
    }

    private static FunctionManager instance = null;

    public static FunctionManager getInstance() {
        return instance == null ? (instance = new FunctionManager()) : instance;
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public Function getFunction(String name) {
        return functions.get(name);
    }

    public void addFunction(String name, Function function) {
        if (functions.containsKey(name)) {
//            throw new RuntimeException("Function already exists: " + name);
            log.warn("WARNING: Function already exists: " + name);
        }
        functions.put(name, function);
    }

    /**
     * Registers new method and binds provided class to it. If there's already a method with this name
     * binds provided class to old method (used to render method call declaration).
     * Also makes function for static calling this method (ClassName__method&lt;argsnum&gt;)
     * @param name call-method function name (__method__methodName) 
     * @param origName original method name
     * @param node owner class Node
     * @param className owner class name
     * @param currentFile file where method is declared
     * @param module module containing owner class
     */
    public void registerMethod(String name, String origName, Node node, String className, String currentFile, String module) {
        // find already defined method with this name
        Method meth = (Method) functions.get(name);
        Node body;
        String[] args;
        if (meth == null) {
            // if not found, register new method/function
            meth = new Method(origName);
            meth.setSource(node);
            body = meth.findMethod(node, origName);
            String argString = body.getChildren("argnames").get(0).getJdomElement().getTextTrim();
            args = argString.split(",");
            if (args.length == 1 && "".equals(args[0])) args = new String[0];
            meth.setArguments(args);

            meth.setName(METHOD_PREFIX + meth.getOriginalName() + args.length);

            if (args.length > 0) functions.put(meth.getName(), meth);
        } else {
            body = meth.findMethod(node, origName);
            String argString = body.getChildren("argnames").get(0).getJdomElement().getTextTrim();
            args = argString.split(",");
            if (args.length == 1 && "".equals(args[0])) args = new String[0];
        }

        // add given class for this method
        if (args.length > 0) {
            meth.addClass(className, node);
            ClassManager.getInstance().getPyClass(className).getMethods().add(meth);
            ClassManager.getInstance().getPyClass(className).getOwnMethods().add(meth);
        }

        // make static method
        String staticMethodName = className + "__" + origName + args.length;

        String[] newArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = '&' + args[i]; // make all params references
        }
        Function staticMethod = new Function();
        staticMethod.setArguments(newArgs);
        staticMethod.setBody(meth.findMethodBody(node, origName));
        staticMethod.setCurrentFile(currentFile);
        staticMethod.setName(staticMethodName);
        staticMethod.setOriginalName(origName);
        staticMethod.setSource(body);
        staticMethod.setModule(module);

        addFunction(staticMethodName, staticMethod);
    }

}
