package su.msu.cs.lvk.xml2pixy.jdom;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created at: [30.10.2007] 15:00:24
 *
 * @author gklimov
 */

/**
 * JDOM visitor which builds symbol table of modules, classes, functions, global variables. 
 */
public class SymbolTableBuilder extends ASTVisitor implements JdomVisitor {
    private ModuleLocator moduleLocator = new ModuleLocator();
    private SymbolTable symbolTable;
    private String fileName;
    private String module;

    public SymbolTableBuilder() {
        this(null, null, null);
    }

    public SymbolTableBuilder(SymbolTable symbolTable, String fileName, String module) {
        this.symbolTable = symbolTable == null ? new SymbolTable() : symbolTable;
        this.fileName = fileName;
        this.module = module;
    }

    public void visit(Node node) throws VisitorException {

        Element jdom = node.getJdomElement();

        String nodeName = jdom.getName();

        try {
            Method method = SymbolTableBuilder.class.getMethod("visit" + nodeName, Node.class);
            method.invoke(this, node);
        } catch (NoSuchMethodException ne) {
            /* do nothing */
        } catch (Exception e) {
//            e.printStackTrace();

            throw new VisitorException(e.getCause().getMessage(), e.getCause());
        }

    }

    public void visitAssName(Node node) {
        int lineno = getLineno(node.getJdomElement());
        String name = Utils.trimToEmpty(node.getJdomElement().getAttributeValue("name"));

        Element parent = node.getJdomElement(). /* Module node Stmt Assign nodes AssName */
                getParentElement(). /* Module node Stmt nodes Assign nodes */
                getParentElement(); /* Module node Stmt nodes Assign */

        Element ancestor = node.getJdomElement(). /* Module node Stmt Assign nodes AssName */
                getParentElement(). /* Module node Stmt nodes Assign nodes */
                getParentElement(). /* Module node Stmt nodes Assign */
                getParentElement(). /* Module node Stmt nodes */
                getParentElement(). /* Module node Stmt */
                getParentElement(). /* Module node */
                getParentElement(); /* Module */

        if (parent.getName().equals("Assign")) {
            if (ancestor.getName().equals("Module")) {
                symbolTable.addSymbol(module.replaceAll("\\.", "__") + "__" + name,
                        new Symbol(name, Symbol.Type.VARIABLE, fileName, module, lineno));
            } else if (ancestor.getName().equals("Class")) {
                symbolTable.addSymbol(module.replaceAll("\\.", "__") + "__" +
                        ancestor.getAttributeValue("name") + "__" + name,
                        new Symbol(name, Symbol.Type.VARIABLE, fileName, module, lineno));
            }
        }
    }

    public void visitClass(Node node) throws VisitorException {
        int lineno = getLineno(node.getJdomElement());
        String name = Utils.trimToEmpty(node.getJdomElement().getAttributeValue("name"));
        symbolTable.addSymbol(module.replaceAll("\\.", "__") + "__" + name,
                new Symbol(name, Symbol.Type.CLASS, fileName, module, lineno)
        );
    }

    public void visitFunction(Node node) throws VisitorException {
        int lineno = getLineno(node.getJdomElement());
        String name = Utils.trimToEmpty(node.getJdomElement().getAttributeValue("name"));

        Symbol funSym = new Symbol(name, Symbol.Type.FUNCTION, fileName, module, lineno);

        // get default argument values
        funSym.setArgs(makeArgs(node));

        symbolTable.addSymbol(module.replaceAll("\\.", "__") + "__" + name, funSym);
    }

    private Map<String, ParseNode> makeArgs(Node node) {
        Map<String, ParseNode> args = new LinkedHashMap<String, ParseNode>();
        String[] argNames = getFirstChild(node, "argnames").getJdomElement().getTextTrim().split(",");

        // get argnames
        for (String arg : argNames) {
            // check for empty args
            if (!Utils.isBlank(arg)) {
                args.put(arg, null);
            }
        }

        // get default values (for last N arguments)
        List<Node> defaults = getFirstChild(node, "defaults").getChildren();

        int argI = argNames.length - defaults.size(); // get index of first arg with default value
        // iterate over default values
        for (Node defaultValue : defaults) {
            args.put(argNames[argI], makeStaticScalar(defaultValue)); // update default value for arg
            argI++;
        }

        return args;
    }

    public void visitImport(Node node) throws VisitorException {
        File file = new File(fileName);

        String names = node.getChildren("names").get(0).getJdomElement().getTextTrim();
        String[] toImport = new String[0];
        if (!Utils.isBlank(names)) {
            toImport = names.split(",");
        }

        String currentModule = file.getName().substring(0, file.getName().indexOf('.'));
        if (symbolTable.nothingImported()) {
            symbolTable.getImported().add(currentModule);
            symbolTable.addSymbol(currentModule, Symbol.Type.MODULE);
        }
        for (String module : toImport) {
            if (!symbolTable.contains(module)) {
                StringBuffer buf = new StringBuffer();
                for (String m : module.split("\\.")) {
                    if (buf.length() > 0) {
                        buf.append("__");
                    }
                    buf.append(m);
                    symbolTable.addSymbol(buf.toString(), Symbol.Type.MODULE);
                }
            }

            if (symbolTable.getImported().contains(module)) {
                continue;
            }

            symbolTable.getImported().add(module);

            // put imported module in namespace of current module
            // Ex.: moo: import foo.bar.baz -> symbol moo.baz is a module
            String[] dottedNames = module.split("\\.");
            String innerName = dottedNames[dottedNames.length - 1];

            String mangledName = module.replaceAll("\\.", "__");
            Symbol localModuleSymbol = new Symbol(currentModule + "__" + innerName, Symbol.Type.MODULE, null, mangledName);
            symbolTable.addSymbol(currentModule + "__" + innerName, localModuleSymbol);

            // build symbol table for imported module
            try {
                File fileToImport;
                fileToImport = moduleLocator.locateImport(file, this.module, module);
                Utils.buildSymbolTable(fileToImport.getAbsolutePath(), symbolTable, module);
            } catch (ModuleNotFoundException e) {
                throw new VisitorException("ERROR: Can't import module: " + module + " (from "
                        + fileName + ':' + node.getJdomElement().getAttributeValue("lineno") + ")", e);
            }
        }

    }

    public void visitFrom(Node node) throws VisitorException {

        String names = node.getChildren("names").get(0).getJdomElement().getTextTrim();
        String[] toImport = new String[0];
        if (!Utils.isBlank(names)) {
            toImport = names.split(",");
        }

        // fix up symbol table... fill it with function and classes without module
        if (toImport.length > 0) {
            String moduleName = node.getJdomElement().getAttributeValue("modname");
            boolean delete = !symbolTable.contains(moduleName.trim());
            fromImport(node);
            if (delete) symbolTable.remove(moduleName); // unbind moduleName

            if (toImport[0].equals("*")) {
                Map<String, Symbol> local = new HashMap<String, Symbol>();
                for (Map.Entry<String, Symbol> entry : symbolTable.getAllSymbols().entrySet()) {
                    Symbol current = entry.getValue();
                    if (moduleName.trim().equals(current.getModule())) {
                        String newName = entry.getKey().substring((moduleName + "__").length()); // new name - without 'module__'
                        local.put(newName, new Symbol(newName, current.getType(), current.getFile(),
                                current.getModule(), current.getLineno()));
                    }
                }
                symbolTable.getAllSymbols().putAll(local);
            } else {
                for (String item : toImport) {
                    Symbol old = symbolTable.getSymbol(moduleName + "__" + item);
                    if (old != null) {
                        symbolTable.addSymbol(item, new Symbol(item, old.getType(), old.getFile(),
                                old.getModule(), old.getLineno()));
                    } else {
                        symbolTable.addSymbol(item, new Symbol(item, Symbol.Type.UNKNOWN, "unknown",
                                moduleName, -2));
                    }
                }
            }
        }

    }

    public void fromImport(Node node) throws VisitorException {
        File file = new File(fileName);

        String module = node.getJdomElement().getAttributeValue("modname");

        String currentModule = file.getName().substring(0, file.getName().indexOf('.'));
        if (symbolTable.nothingImported()) {
            symbolTable.getImported().add(currentModule);
            symbolTable.addSymbol(currentModule, Symbol.Type.MODULE);
        }

        if (!Utils.isBlank(module)) {
            if (!symbolTable.contains(module)) {
                StringBuffer buf = new StringBuffer();
                for (String m : module.split("\\.")) {
                    if (buf.length() > 0) {
                        buf.append("__");
                    }
                    buf.append(m);
                    symbolTable.addSymbol(buf.toString(), Symbol.Type.MODULE);
                }
            }

            if (symbolTable.getImported().contains(module)) return;
            symbolTable.getImported().add(module);


            try {
                File fileToImport;
                fileToImport = moduleLocator.locateImport(file, this.module, module);
                Utils.buildSymbolTable(fileToImport.getAbsolutePath(), symbolTable, module);
            } catch (ModuleNotFoundException e) {
                throw new VisitorException("ERROR: Can't import module: " + module + " (from "
                        + fileName + ':' + node.getJdomElement().getAttributeValue("lineno") + ")", e);
            }
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
