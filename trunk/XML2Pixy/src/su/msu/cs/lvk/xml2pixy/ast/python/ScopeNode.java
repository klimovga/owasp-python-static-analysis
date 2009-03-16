package su.msu.cs.lvk.xml2pixy.ast.python;

import org.apache.log4j.Logger;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * User: KlimovGA
 * Date: 07.12.2008
 */
public abstract class ScopeNode extends PythonNode {

    private static Logger logger = Logger.getLogger(ScopeNode.class);

    protected static SymbolTable globalSymbolTable = new SymbolTable(false);

    protected SymbolTable symbolTable;

    protected String name;

    protected ScopeNode() {
        this(true);
    }

    protected ScopeNode(boolean init) {
        super();

        symbolTable = new SymbolTable(init);
    }

    public ScopeNode(Element jdom) {
        super(jdom);

        symbolTable = new SymbolTable(false);
    }

    public ScopeNode(Node node) {
        super(node);

        symbolTable = new SymbolTable(false);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Symbol getSymbol(String name) {
        Symbol symbol = globalSymbolTable.getSymbol(name);

        if (symbol != null) return symbol;

        symbol = symbolTable.getSymbol(name);
        if (symbol != null) return symbol;

        // if have not this symbol, search in parent scopes
        return findSymbol(name);
    }

    public void addGlobal(String name, Symbol symbol) {
        symbol.setGlobal(true);
        symbol.setScope(this);
        symbolTable.addSymbol(name, symbol);
    }

    public void addSymbol(String name, Symbol symbol) {
        Symbol set = symbolTable.getSymbol(name);
        symbol.setScope(this);
        if (set != null) {
            if (SymbolTable.BUILTIN_MODULE.equals(set.getModule())) {
                logger.warn("Trying to override builtin symbol " + set.getName() + " at " + symbol.getSource().getLocation());
            }
            if (set.isGlobal()) {
                ScopeNode globalScope = getGlobalScope();
                Symbol globalSymbol = globalScope.getSymbol(name);
                if (globalSymbol != null && globalSymbol.getType() != symbol.getType())  {
                    logger.warn("Symbol " + name + ":" + symbol.getType().name() + " already exists in global scope as " +
                            globalSymbol.getType().name() +
                            "(" + symbol.getFile() + ":" + symbol.getLineno() +
                            ", defined at " + globalSymbol.getFile() + ":" + globalSymbol.getLineno() + ")");
                }
                if (globalScope != this) globalScope.addSymbol(name, symbol);
                symbol.setGlobal(true);
            } else if (set.getType() != symbol.getType()) {
                logger.warn("Symbol " + name + ":" + symbol.getType().name() + " already exists in this scope as " +
                        set.getType().name() +
                        "(" + symbol.getFile() + ":" + symbol.getLineno() +
                        ", defined at " + set.getFile() + ":" + set.getLineno() + ")");
            }
        }

        symbolTable.addSymbol(name, symbol);
    }

    protected Symbol findSymbol(String name) {
        ScopeNode current = getParentScope();

        while (current != null) {
            // we cannot access class members without name even if we are inside class only if 
            if (!(current instanceof ClassNode)) {
                return current.getSymbol(name);
            }
            current = current.getParentScope();
        }

        return null;
    }

    public ScopeNode getParentScope() {
        PythonNode parent = getParent();
        if (parent != null) {
            return parent.getScope();
        }
        return null;
    }

    public ModuleNode getGlobalScope() {
        ScopeNode scope = this;
        while (scope != null && scope.getClass() != ModuleNode.class) {
            scope = scope.getParentScope();
        }
        return (ModuleNode) scope;
    }

    public void finishSymbolTable() {
        symbolTable.setFinished(true);
    }

    public boolean isFinished() {
        return symbolTable.isFinished();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
