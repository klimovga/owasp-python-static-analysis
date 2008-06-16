package su.msu.cs.lvk.xml2pixy.transform;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created at: [30.10.2007] 14:49:47
 *
 * @author gklimov
 */

/**
 * Single symbol in target symbol table.
 */
public class Symbol {

    protected static Logger log = Logger.getLogger(Symbol.class);

    /**
     * Enum for symbol types.
     */
    public enum Type {
        CLASS,
        FUNCTION,
        MODULE,
        VARIABLE,
        UNKNOWN;

        /**
         * Returns type using python AST node name.
         * @param s node name
         * @return type object
         */
        public static Type fromString(String s) {
            if ("Class".equals(s)) {
                return CLASS;
            } else if ("Function".equals(s)) {
                return FUNCTION;
            } else if ("Module".equals(s)) {
                return MODULE;
            } else if ("AssName".equals(s)) {
                return VARIABLE;
            } else {
                return UNKNOWN;
            }
        }

    }

    private String name;
    private Type type;
    private int lineno;
    private String file;
    private String module;

    private Map<String, ParseNode> args;

    public Symbol(String name, Type type, String file, String module, int lineno) {
        this.name = name;
        this.type = type;
        this.lineno = lineno;
        this.file = file;
        this.module = module;
    }

    public Symbol(String name, Type type, String file, String module) {
        this(name, type, file, module, -2);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getLineno() {
        return lineno;
    }

    public String getFile() {
        return file;
    }

    public boolean isClass() {
        return type == Type.CLASS;
    }

    public boolean isFunction() {
        return type == Type.FUNCTION;
    }

    public boolean isModule() {
        return type == Type.MODULE;
    }

    public boolean isVariable() {
        return type == Type.VARIABLE;
    }

    public String getModule() {
        return module;
    }

    /**
     * Set default function argument values.
     * @param args map <code>argument name</code> -> parse node representing default value or null if there's no default value
     */
    public void setArgs(Map<String, ParseNode> args) {
        if (isFunction()) {
            log.warn("Symbol " + getModule() + "." + getName() + " is not a function");
        }
        this.args = args;
    }

    /**
     * Get default function argument values.
     * @return map <code>argument name</code> -> static parse node representing default value or null if there's no default value
     */
    public Map<String, ParseNode> getArgs() {
        if (isFunction()) {
            log.warn("Symbol " + getModule() + "." + getName() + " is not a function");
        }
        return this.args;
    }

    /**
     * @param arg argument name
     * @return 0-based index of argument in function argument list
     */
    public int getArgIndex(String arg) {
        if (isFunction()) {
            log.warn("Symbol " + getModule() + "." + getName() + " is not a function");
        }
        if (args == null) { // if don't know about arguments (not specified built-in function)
            return -1;
        }
        int i = 0;
        for (String key : args.keySet()) {
            if (key.equals(arg)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @param i 0-based index of argument in function argument list
     * @return static ParseNode with default value or null if there's no default value
     */
    public ParseNode getArg(int i) {
        if (isFunction()) {
            log.warn("Symbol " + getModule() + "." + getName() + " is not a function");
        }

        if (args == null) { // if don't know about arguments (not specified built-in function)
            return null;
        }
        int j = 0;
        for (ParseNode value : args.values()) {
            if (j++ == i) return value;
        }

        return null;
    }

}
