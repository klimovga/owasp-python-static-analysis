package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:13:45
 */
public class NameVisitor extends ASTVisitor {

    public NameVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public NameVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        String name = node.getJdomElement().getAttributeValue("name");

        if (symbolTable.isModule(name)) {
            node.setParseNode(new ParseNode(
                    PhpSymbols.T_STRING, "T_STRING", currentFile, name, lineno));
        } else if (name.equals("True") || name.equals("False") || name.equals("None")) {
            String value = name.equals("True") ? "true" : name.equals("False") ? "false" : "null";
            ParseNode expr = new ParseNode(
                    PhpSymbols.expr, "expr", currentFile);
            ParseNode expr_without_variable = new ParseNode(
                    PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
            expr.addChild(expr_without_variable);
            ParseNode scalar = new ParseNode(
                    PhpSymbols.scalar, "scalar", currentFile);
            expr_without_variable.addChild(scalar);
            scalar.addChild(new ParseNode(
                    PhpSymbols.T_STRING, "T_STRING", currentFile, value, lineno));
            node.setParseNode(expr);
        } else {
            String fullName = module.replaceAll("\\.", "__") + "__" + name;
            if (symbolTable.isVariable(fullName)) {
                name = fullName;
            }
            node.setParseNode(makeReferenceVariableByName(name, lineno));

/*
            ParseNode reference_variable = new ParseNode(
                    PhpSymbols.reference_variable, "reference_variable", currentFile);
            ParseNode compound_variable = new ParseNode(
                    PhpSymbols.compound_variable, "compound_variable", currentFile);
            reference_variable.addChild(compound_variable);
            ParseNode var = new ParseNode(
                    PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile,
                    "$" + Utils.trimToEmpty(name), lineno);
            compound_variable.addChild(var);
            node.setParseNode(reference_variable);
*/
        }
    }

}
