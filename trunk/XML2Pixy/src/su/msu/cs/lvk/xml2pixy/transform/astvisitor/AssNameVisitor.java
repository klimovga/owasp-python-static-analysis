package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:24:10
 */
public class AssNameVisitor extends ASTVisitor {

    public AssNameVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public AssNameVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        Element ancestor = node.getJdomElement(). /* Class code Stmt nodes Assign nodes AssName */
                getParentElement(). /* Class code Stmt nodes Assign nodes */
                getParentElement(). /* Class code Stmt nodes Assign   */
                getParentElement(). /* Class code Stmt nodes */
                getParentElement(). /* Class code Stmt */
                getParentElement(). /* Class code */
                getParentElement(); /* Class */

        String name = Utils.trimToEmpty(node.getJdomElement().getAttributeValue("name"));
        String fullName;
        if (ancestor.getName().equals("Class")) {
            name = ancestor.getAttributeValue("name") + "__" + name;
        }
        fullName = module.replaceAll("\\.", "__") + "__" + name;

        if (symbolTable.isVariable(fullName)) {
            name = fullName;
        }

        ParseNode cvar = new ParseNode(PhpSymbols.cvar, "cvar", currentFile);
        ParseNode cvar_withour_objects = new ParseNode(PhpSymbols.cvar_without_objects,
                "cvar_without_objects", currentFile);
        cvar.addChild(cvar_withour_objects);
        cvar_withour_objects.addChild(makeReferenceVariableByName(name, lineno));

        node.setParseNode(cvar);
    }

}
