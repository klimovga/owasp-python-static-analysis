package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.Element;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 *
 */
public class ConstVisitor extends ASTVisitor {

    protected static Logger log = Logger.getLogger(ASTVisitor.class.getName());

    public ConstVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ConstVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException  {
        ParseNode expr = new ParseNode(PhpSymbols.expr, "expr", currentFile);
        ParseNode expr_without_variable = new ParseNode(
                PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
        expr.addChild(expr_without_variable);
        ParseNode scalar = new ParseNode(
                PhpSymbols.scalar, "scalar", currentFile);
        expr_without_variable.addChild(scalar);
        ParseNode common_scalar = new ParseNode(
                PhpSymbols.common_scalar, "common_scalar", currentFile);
        scalar.addChild(common_scalar);
        String value;

        Element element = node.getJdomElement();
        if (element.getAttribute("value") != null) {
            // exact value, no trimming
            value = element.getAttribute("value").getValue();
        } else if (element.getChild("value") != null) {
            // indented value, trim it
            value = element.getChild("value").getText().trim();
        } else {
            log.error("Constant without value found in " + module + " near line " + lineno);
            return;
        }

        Integer intval = getInteger(node.getJdomElement(), "value");
        Double dval = getDouble(node.getJdomElement(), "value");

        ParseNode constant;
        if (intval != null) {
            constant = new ParseNode(
                    PhpSymbols.T_LNUMBER, "T_LNUMBER", currentFile, value, lineno);
        } else if (dval != null) {
            constant = new ParseNode(
                    PhpSymbols.T_DNUMBER, "T_DNUMBER", currentFile, value, lineno);
        } else {
            constant = new ParseNode(
                    PhpSymbols.T_CONSTANT_ENCAPSED_STRING, "T_CONSTANT_ENCAPSED_STRING",
                    currentFile, '"' + value + '"', lineno);
        }

        common_scalar.addChild(constant);

        node.setParseNode(expr);
    }

}
