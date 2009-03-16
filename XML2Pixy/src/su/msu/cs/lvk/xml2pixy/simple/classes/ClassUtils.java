package su.msu.cs.lvk.xml2pixy.simple.classes;

import su.msu.cs.lvk.xml2pixy.ast.python.ClassNode;
import su.msu.cs.lvk.xml2pixy.ast.python.FunctionNode;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;

import java.util.*;

/**
 * User: klimov
 * Date: 11.01.2009
 */
public class ClassUtils {

    private static Map<String, PyClass> classes;
    private static Map<String, Method> methods;

    public static void init() {
        classes = new HashMap<String, PyClass>();
        methods = new HashMap<String, Method>();
    }

    public static List<FunctionNode> renderMethods() {
        List<FunctionNode> meths = new ArrayList<FunctionNode>();

        for (Method method : methods.values()) {
            meths.add(method.renderPython());
        }

        List<FunctionNode> constructors = renderConstructors();

        meths.addAll(constructors);

        return meths;
    }


    public static List<FunctionNode> renderConstructors() {
        List<FunctionNode> constructors = new ArrayList<FunctionNode>();

        for (PyClass clazz : classes.values()) {
            constructors.add(clazz.renderConstructor());
        }

        return constructors;
    }

    public static String getFullClassName(ClassNode clazz) {
        return ProcessingUtils.getFullName(clazz);
    }

    public static void addClass(PyClass clazz) {
        classes.put(clazz.getFullName(), clazz);
    }

    public static PyClass getPyClass(String fullName) {
        return classes.get(fullName);
    }

    public static Collection<PyClass> getAllClasses() {
        return classes.values();
    }

    public static void registerMethod(ClassNode classNode, FunctionNode method) {
        PyClass pyClass = getPyClass(getFullClassName(classNode));
        registerMethod(pyClass, method.getName(), method);
    }

    public static void registerMethod(ClassNode classNode, Symbol method) {
        if (!method.isFunction()) throw new IllegalArgumentException("Cannot register non-function as method (" + method + ")");
        PyClass pyClass = getPyClass(getFullClassName(classNode));
        registerMethod(pyClass, method.getName(), (FunctionNode) method.getSource());
    }

    public static void registerMethod(PyClass pyClass, String methodName, FunctionNode impl) {
        if (pyClass != null) {
            pyClass.addMethod(methodName, impl);
            processMethod(methodName, pyClass, impl);
        }
    }

    private static void processMethod(String methodName, PyClass clazz, FunctionNode impl) {
        String fullMethodName = ProcessingUtils.getMethodFullName(methodName, impl);
        Method method;
        if (methods.containsKey(fullMethodName)) {
            method = methods.get(fullMethodName);
        } else {
            method = new Method(fullMethodName);
            methods.put(fullMethodName, method);
        }

        method.addClass(clazz, impl);
    }

    

}
