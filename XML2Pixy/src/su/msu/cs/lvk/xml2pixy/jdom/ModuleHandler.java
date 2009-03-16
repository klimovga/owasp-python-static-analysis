package su.msu.cs.lvk.xml2pixy.jdom;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Converter;
import su.msu.cs.lvk.xml2pixy.ast.python.AssNameNode;
import su.msu.cs.lvk.xml2pixy.ast.python.FunctionNode;
import su.msu.cs.lvk.xml2pixy.ast.python.ModuleNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingException;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

import java.io.File;
import java.util.*;

/**
 * User: klimov
 * Date: 07.01.2009
 */
public class ModuleHandler {

    public static final String INIT_FILE_NAME = "__init__";

    private static Map<String, ModuleNode> modules;
    private static ModuleLocator locator;
    private static SymbolTable builtinSymbolTable;
    private static Map<String, ModuleNode> builtinModules;

    public static void init() {
        builtinSymbolTable = new SymbolTable(false);
        locator = new ModuleLocator();
        modules = new HashMap<String, ModuleNode>();
        builtinModules = new HashMap<String, ModuleNode>();

        parseModulesConfig();
    }

    /**
     * @param srcModule     current module
     * @param targetModule  simple module name without dots. I.e. <code>compiler.ast</code> is not allowed,
     *                      only <code>compiler</code> or <code>ast</code>
     * @param globalAllowed true if we allow search for modules globally in application root
     * @return ModuleNode object representing the loaded module
     * @throws ModuleNotFoundException if no module was found
     */
    public static ModuleNode importModule(ModuleNode srcModule, String targetModule, boolean globalAllowed) throws ModuleNotFoundException {
        if (locator.countDots(targetModule) > 0) {
            throw new IllegalArgumentException("targetModule should not contain dots: " + targetModule);
        }

        ModuleNode module = null;

        if (srcModule.getFileName() != null) { // fileName == null means that module is loaded from builtin
            File importerFile = new File(srcModule.getFileName()).getAbsoluteFile();
            boolean xml = !"py".equalsIgnoreCase(StringUtils.substringAfterLast(importerFile.getAbsolutePath(), "."));
            // test if module is loaded already
            // try to find local module
            String fullModuleName = "";
            if (INIT_FILE_NAME.equals(StringUtils.substringBefore(importerFile.getName(), "."))) {
                fullModuleName = srcModule.getName() + "." + targetModule;
            } else {
                if (srcModule.getName().contains(".")) {
                    fullModuleName = StringUtils.substringBeforeLast(srcModule.getName(), ".") + ".";
                }
                fullModuleName += targetModule;
            }
            if (modules.containsKey(fullModuleName)) {
                module = modules.get(fullModuleName);
            }
            if (module == null) {
                File moduleFile = locator.locateImport(locator.getParentDir(importerFile) + "/" + targetModule, xml);
                if (moduleFile != null) {
                    module = moduleFile.isDirectory() ? new ModuleNode() : importModule(moduleFile);
                    module.setFileName(moduleFile.getAbsolutePath());
                    if (moduleFile.isDirectory()) {
                        module.setFileName(new File(module.getFileName() + "/" + INIT_FILE_NAME + ".py").getAbsolutePath());
                    }
                    module.setName(fullModuleName);

                    modules.put(fullModuleName, module);
                }
            }

            // if local module not found try to find global one
            if (globalAllowed && module == null) {
                if (modules.containsKey(targetModule)) {
                    module = modules.get(targetModule);
                }
                if (module == null) {
                    String dir = locator.getParentDir(importerFile);
                    int countDots = locator.countDots(srcModule.getName());
                    while (countDots-- > 0) {
                        dir = locator.getParentDir(new File(dir));
                    }

                    File moduleFile = locator.locateImport(dir + "/" + targetModule, xml);
                    if (moduleFile != null) {
                        module = importModule(moduleFile);
                        module.setFileName(moduleFile.getAbsolutePath());
                        module.setName(fullModuleName);

                        modules.put(targetModule, module);
                    }
                }
            }
        }

        // if not found yet look in builtin modules
        if (module == null) {
            // if current module is builtin then do only local import
            if (srcModule.getFileName() == null) {
                String fullModuleName = srcModule.getName() + "." + targetModule;
                if (builtinModules.containsKey(fullModuleName)) {
                    module = builtinModules.get(fullModuleName);
                }
            } else {// else do global builtin import
                if (builtinModules.containsKey(targetModule)) {
                    module = builtinModules.get(targetModule);
                }
            }

        }

        if (module == null) {
            throw new ModuleNotFoundException("Module " + targetModule + " imported from " + srcModule.getName() + " not found");
        }
        return module;
    }

    public static ModuleNode importModule(File moduleFile) throws ModuleNotFoundException {
        // load module
        try {
            PythonNode root = ProcessingUtils.parseFile(moduleFile.getAbsolutePath());
            return (ModuleNode) root;
        } catch (ProcessingException e) {
            throw new ModuleNotFoundException("Error while parsing file " + moduleFile, e);
        }
    }

    public static File findModuleFile(String importer, String srcModule, String targetModule) throws ModuleNotFoundException {
        return locator.locateImport(new File(importer), srcModule, targetModule);
    }

    public static void addModule(String name, ModuleNode module) {
        modules.put(name, module);
    }

    public static Collection<ModuleNode> getImportedModules() {
        return modules.values();
    }

    protected static void parseModulesConfig() {
        for (Object obj : Converter.modulesConfig.getRootElement().getChildren("Module")) {
            Element module = (Element) obj;

            processModule(module);
        }
    }

    protected static void processModule(Element module) {
        String moduleName = module.getAttributeValue("name");
        StringBuilder buf = new StringBuilder();
        ModuleNode current = null;

        // for each simple module name make ModuleNode in builtins
        for (String m : moduleName.split("\\.")) {
            if (StringUtils.isBlank(m)) {
                throw new IllegalArgumentException("Couldn't load builtin submodule with empty name: " + moduleName);
            }
            // get name of new module
            if (buf.length() > 0) buf.append('.');
            buf.append(m);

            String newModuleName = buf.toString();
            ModuleNode newModule;
            if (builtinModules.containsKey(newModuleName)) {
                newModule = builtinModules.get(newModuleName);
            } else {
                newModule = new ModuleNode(false);
                newModule.setName(newModuleName);
                newModule.setLineno(-1);

                builtinModules.put(newModule.getName(), newModule);
            }


            if (current != null) {
                Symbol moduleSymbol = new Symbol(m, Symbol.Type.MODULE, null, current.getName());
                moduleSymbol.setSource(newModule);
                current.addSymbol(m, moduleSymbol);
            }

            current = newModule;
        }

        if (current == null) {
            throw new IllegalArgumentException("Couldn't load builtin module with empty name");
        }

        for (Object obj : module.getChildren("Function")) {
            Element function = (Element) obj;
            processFunction(function, current);
        }

        for (Object obj : module.getChildren("Variable")) {
            Element var = (Element) obj;
            processVar(var, current);
        }
    }

     protected static void processVar(Element function, ModuleNode module) {
        String varName = function.getAttributeValue("name");

        AssNameNode varNode = new AssNameNode(varName);
        varNode.setName(varName);

        Symbol varSymbol = new Symbol(varName, Symbol.Type.VARIABLE, null, module.getName(), -1);
        varSymbol.setSource(varNode);

        if (SymbolTable.BUILTIN_MODULE.equals(module.getName())) {
            builtinSymbolTable.addSymbol(varName, varSymbol);
            varSymbol.setScope(module);
        } else {
            module.addSymbol(varName, varSymbol);
        }
    }

    protected static void processFunction(Element function, ModuleNode module) {
        String functionName = function.getAttributeValue("name");

        FunctionNode functionNode = new FunctionNode(false);
        functionNode.setName(functionName);

        Symbol functionSymbol = new Symbol(functionName, Symbol.Type.FUNCTION, null, module.getName(), -1);
        functionSymbol.setSource(functionNode);

        if (SymbolTable.BUILTIN_MODULE.equals(module.getName())) {
            builtinSymbolTable.addSymbol(functionName, functionSymbol);
            functionSymbol.setScope(module);
        } else {
            module.addSymbol(functionName, functionSymbol);
        }
    }

    public static void fillWithBuiltins(SymbolTable target) {
        for (Map.Entry<String, Symbol> entry : builtinSymbolTable.getAllSymbols().entrySet()) {
            try {
                target.addSymbol(entry.getKey(), entry.getValue().clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e); // it is not possible since Symbol is Cloneable
            }
        }
    }

    public static List<PythonNode> inlineModules(String currentModule) {
        List<PythonNode> nodes = new ArrayList<PythonNode>();
        for (ModuleNode module : modules.values()) {
            if (module != null && !module.getName().equals(currentModule) &&
                    module.getStmt() != null && !module.getStmt().getNodes().isEmpty()) {
                ProcessingUtils.simplifyPython(module, false);
                nodes.addAll(module.getStmt().getNodes());
            }
        }
        return nodes;
    }

}
