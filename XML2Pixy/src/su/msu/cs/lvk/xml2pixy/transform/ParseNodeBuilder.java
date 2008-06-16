package su.msu.cs.lvk.xml2pixy.transform;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.jdom.JdomVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Main visitor class. Visits every AST node and calls different visitor classes by node names.
 * If node name starts with lowercase letter, does not visit. If there's no visitor class for current
 * node, call base ASTVisitor.
 */
public class ParseNodeBuilder implements JdomVisitor {

    protected static Logger log = Logger.getLogger(ParseNodeBuilder.class.getName());

    private String currentFile;

    private SymbolTable symbolTable;

    private int currentLineno;

    private Map<String, ASTVisitor> astVisitors;

    private String module;

    public ParseNodeBuilder(String currentFile, SymbolTable symbolTable, String module) {
        this.currentFile = currentFile;
        this.currentLineno = 1;
        this.symbolTable = symbolTable;
        astVisitors = new HashMap<String, ASTVisitor>();
        this.module = module;
    }

    public void visit(Node node) {
        Element jdom = node.getJdomElement();

        int lineno = getLineno(jdom);
        if (lineno > 0) currentLineno = lineno;
        else lineno = currentLineno;
        String nodeName = jdom.getName();

        ASTVisitor visitor = getVisitor(nodeName);
        visitor.setCurrentFile(currentFile);
        try {
            visitor.visit(node, currentFile, lineno, module);
        } catch (VisitorException ve) {
            log.error("WARNING: " + ve);
        }

    }

    private ASTVisitor getVisitor(String nodeName) {
        if (astVisitors.containsKey(nodeName) && astVisitors.get(nodeName) != null) {
            return astVisitors.get(nodeName);
        } else {
            ASTVisitor visitor;
            try {
                visitor = (ASTVisitor) Class.forName("su.msu.cs.lvk.xml2pixy.transform.astvisitor." +
                        nodeName + "Visitor").getConstructor(SymbolTable.class).newInstance(this.symbolTable);
            } catch (Throwable e) {
                visitor = new ASTVisitor(this.symbolTable);
            }
            astVisitors.put(nodeName, visitor);
            return visitor;
        }
    }

    private int getLineno(Element elem) {
        Attribute lineno = elem.getAttribute("lineno");
        try {
            return lineno == null ? -1 : lineno.getIntValue();
        } catch (DataConversionException e) {
            return -1;
        }

    }

}
