package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:03:05
 */
public class AssAttrVisitor extends ASTVisitor {

    public AssAttrVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public AssAttrVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        Node callee = getFirstChild(getFirstChild(node, "expr"), null);
        String attrname = node.getJdomElement().getAttributeValue("attrname");

        if (callee.getParseNode() != null) {

            if (callee.getParseNode().getSymbol() == PhpSymbols.T_STRING) {
                String fullName = callee.getParseNode().getLexeme() + "__" + attrname;
                Symbol symbol = symbolTable.getSymbol(fullName);
                if (symbol != null && symbol.isVariable()) {
                    node.setParseNode(makeReferenceVariableByName(fullName, lineno));
                } else {
                    node.setParseNode(new ParseNode(
                            PhpSymbols.T_STRING, "T_STRING", currentFile,
                            callee.getParseNode().getLexeme() + "__" + attrname, lineno));
                }
            } else {
                if (callee.getJdomElement().getName().equals("Name")) {
                    Symbol symbol = symbolTable.getSymbol(callee.getJdomElement().getAttributeValue("name"));
                    if (symbol == null) {
                        symbol = symbolTable.getSymbol(module.replaceAll("\\.", "__") + "__" +
                                callee.getJdomElement().getAttributeValue("name"));
                    }

                    if (symbol != null && symbol.isClass()) {
                        String varName = symbol.getModule().replaceAll("\\.", "__") + "__" +
                                symbol.getName() + "__" + attrname;
                        if (symbolTable.isVariable(varName)) {
                            node.setParseNode(makeReferenceVariableByName(varName, lineno));
                            return;
                        }
                    }
                }

                ParseNode reference_variable = new ParseNode(PhpSymbols.reference_variable, "reference_variable", currentFile);

                ParseNode dim_offset = new ParseNode(PhpSymbols.dim_offset, "dim_offset", currentFile);

                dim_offset.addChild(renderStringConstant(lineno, attrname));

                reference_variable.addChild(makeReferenceVariable(callee.getParseNode()));
                reference_variable.addChild(new ParseNode(PhpSymbols.T_OPEN_RECT_BRACES, "T_OPEN_RECT_BRACES", currentFile, "[", lineno));
                reference_variable.addChild(dim_offset);
                reference_variable.addChild(new ParseNode(PhpSymbols.T_CLOSE_RECT_BRACES, "T_CLOSE_RECT_BRACES", currentFile, "]", Utils.getLinenoRight(dim_offset, lineno)));

                node.setParseNode(reference_variable);

            }

        }
    }

}
