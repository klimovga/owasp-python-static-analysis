package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleLocator;
import su.msu.cs.lvk.xml2pixy.jdom.ModuleNotFoundException;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:54:30
 */

public class ImportVisitor extends ASTVisitor {

    static List<String> imported = new ArrayList<String>();
    static int builtins = 0;

    private ModuleLocator moduleLocator = new ModuleLocator();

    public ImportVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ImportVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String currentModule) throws VisitorException {
        String names = getFirstChild(node, "names").getJdomElement().getTextTrim();
        String[] toImport = new String[0];
        if (!Utils.isBlank(names)) {
            toImport = names.split(",");
        }

        if (imported.size() <= builtins) {
            imported.add(currentModule);
        }

        ParseNode result = null;
        for (String module : toImport) {
            if (imported.contains(module)) {
                continue;
            }

            imported.add(module);

            ParseNode parseNode;

            try {
                File fileToImport = moduleLocator.locateImport(new File(currentFile), currentModule, module);
                parseNode = Utils.buildParseTree(fileToImport.getAbsolutePath(), symbolTable, module);
            } catch (ModuleNotFoundException e) {
                throw new VisitorException("ERROR: Can't import module: " + module + " (from "
                        + currentFile + ':' + node.getJdomElement().getAttributeValue("lineno") + ")", e);
            }

            parseNode = parseNode.getChild(0).getChild(0);

            if (result == null) {
                result = parseNode;
            } else {
                ParseNode last = parseNode;
                while (last.getSymbol() == PhpSymbols.top_statement_list) {
                    last = last.getChild(0);
                }
                last = last.getParent();
                last.getChildren().clear();
                last.addChild(result.getChild(0));
                last.addChild(result.getChild(1));
            }

        }

        node.setParseNode(result == null
                ? new ParseNode(PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2)
                : result);
    }

}
