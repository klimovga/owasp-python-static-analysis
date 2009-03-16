package su.msu.cs.lvk.xml2pixy.transform;

import su.msu.cs.lvk.xml2pixy.Printable;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleHandler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created at: [30.10.2007] 14:51:33
 *
 * @author gklimov
 */
public class SymbolTable implements Printable {

    private List<String> imported;
    private int builtins;
    private boolean finished;

    private Map<String, Symbol> symbolTable;
    public static final String CONFIG_MODULES_XML = "/config/modules.xml";
    public static final String BUILTIN_MODULE = "__builtin__";

    /**
     * Creates the symbol tables and initializes it if necessary
     *
     * @param doInit if true, then init symbol table with builtin symbols taken from <code>CONFIG_MODULES_XML</code>
     */
    public SymbolTable(boolean doInit) {
        symbolTable = new HashMap<String, Symbol>();
        imported = new ArrayList<String>();

        if (doInit) {
            init();
        }
    }

    /**
     * Initializes the symbol table with builtin modules, functions etc taken from <code>CONFIG_MODULES_XML</code>
     */
    public SymbolTable() {
        this(true);
    }

    public void init() {
        ModuleHandler.fillWithBuiltins(this);
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

    public void print(PrintStream out) {
        for (Map.Entry<String, Symbol> entry : symbolTable.entrySet()) {
            entry.getValue().print(out);
            out.println();
        }
    }

    public String toString() {
        return Utils.toString(this);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
