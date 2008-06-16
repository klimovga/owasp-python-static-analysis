package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleLocator;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleNotFoundException;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 30.01.2008
 * Time: 0:44:06
 */

/**
 * Just inline the whole module as usual. The rest will be done by symtable.
 *
 * @see ImportVisitor
 */
public class FromVisitor extends ImportVisitor {

    private ModuleLocator moduleLocator = new ModuleLocator();

    public FromVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public FromVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String currentModule) throws VisitorException {
        String module = node.getJdomElement().getAttributeValue("modname");

        if (imported.size() <= builtins) {
            imported.add(currentModule);
        }

        ParseNode result = null;
        if (!Utils.isBlank(module)) {
            module = module.trim();
            if (imported.contains(module)) {
                return;
            }

            imported.add(module);

            try {
                File fileToImport = moduleLocator.locateImport(new File(currentFile), currentModule, module);
                result = Utils.buildParseTree(fileToImport.getAbsolutePath(), symbolTable, module);
            } catch (ModuleNotFoundException e) {
                throw new VisitorException("ERROR: Can't import module: " + module + " (from "
                        + currentFile + ':' + node.getJdomElement().getAttributeValue("lineno") + ")", e);
            }

            result = result.getChild(0).getChild(0);
        }

        node.setParseNode(result == null
                ? new ParseNode(PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2)
                : result);
    }
}
