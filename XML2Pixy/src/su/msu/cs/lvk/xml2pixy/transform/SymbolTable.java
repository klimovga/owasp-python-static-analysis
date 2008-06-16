package su.msu.cs.lvk.xml2pixy.transform;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created at: [30.10.2007] 14:51:33
 *
 * @author gklimov
 */
public class SymbolTable {

    private List<String> imported;
    private int builtins;

    private Map<String, Symbol> symbolTable;
    public static final String CONFIG_MODULES_XML = "/config/modules.xml";
    public static final String BUILTIN_MODULE = "__builtin__";

    /**
     * Initializes the symbol table with builtin modules, functions etc taken from <code>CONFIG_MODULES_XML</code>
     */
    public SymbolTable() {
        symbolTable = new HashMap<String, Symbol>();
        imported = new ArrayList<String>();

        // Walk through builtin modules
        for (Object obj : Converter.modulesConfig.getRootElement().getChildren("Module")) {
            Element module = (Element) obj;
            String moduleName = module.getAttributeValue("name");
            StringBuffer buf = new StringBuffer();
            for (String m : moduleName.split("\\.")) {
                if (buf.length() > 0) buf.append('.');
                buf.append(m);
                if (!imported.contains(buf.toString())) {
                    imported.add(buf.toString());
                }
            }

            addSymbol(moduleName, Symbol.Type.MODULE);

            // Walk through builtin functions
            for (Object func : module.getChildren("Function")) {
                Element function = (Element) func;
                String functionName = function.getAttributeValue("name");
                String fullFunctionName = functionName;
                if (!moduleName.equals(BUILTIN_MODULE)) {
                    fullFunctionName = moduleName.replaceAll("\\.", "__") + "__" + fullFunctionName;
                }
                Symbol funSym = new Symbol(
                        functionName, Symbol.Type.FUNCTION, CONFIG_MODULES_XML, moduleName);

/*
                Map<String, ParseNode> args = new LinkedHashMap<String, ParseNode>();

                for (Object arg : function.getChildren("Argument")) {
                    Element argument = (Element) arg;
                    String argName = argument.getAttributeValue("name");
                    if (!Utils.isBlank(argName)) {
                        args.put(argName.trim(), null);
                    }
                }
                if (!args.isEmpty()) {
                    funSym.setArgs(args);
                }
*/

                addSymbol(fullFunctionName, funSym);

                // todo: the same, but __builtin____functionName for correct responding on builtin classes ?
/*
                addSymbol(moduleName + "__" + fullFunctionName, new Symbol(
                        functionName, Symbol.Type.FUNCTION, "/config/modules.xml", moduleName, true));
*/
            }


        }

        builtins = imported.size();
    }

    public Symbol getSymbol(String name) {
        return symbolTable.get(name);
    }

    public boolean isClass(String name) {
        Symbol symbol = getSymbol(name);
        return symbol != null && symbol.isClass();
    }

    public boolean isFunction(String name) {
        Symbol symbol = getSymbol(name);
        return symbol != null && symbol.isFunction();
    }

    public boolean isModule(String name) {
        Symbol symbol = getSymbol(name);
        return symbol != null && symbol.isModule();
    }

    public boolean isVariable(String name) {
        Symbol symbol = getSymbol(name);
        return symbol != null && symbol.isVariable();
    }

    public Symbol addSymbol(String name, Symbol.Type type) {
        Symbol symbol = new Symbol(name, type, null, null);
        symbolTable.put(name, symbol);
        return symbol;
    }

    public void addSymbol(String name, Symbol symbol) {
        symbolTable.put(name, symbol);
    }

    public boolean contains(String name) {
        return symbolTable.containsKey(name);
    }

    public List<String> getImported() {
        return imported;
    }

    public boolean nothingImported() {
        return imported.size() <= builtins;
    }

    /**
     * Removes symbol from table. Needed by FromVisitor for not to bind moduleName.
     *
     * @param symName symbol to be deleted
     */
    public void remove(String symName) {
        symbolTable.remove(symName);
    }

    public Map<String, Symbol> getAllSymbols() {
        return symbolTable;
    }
}
