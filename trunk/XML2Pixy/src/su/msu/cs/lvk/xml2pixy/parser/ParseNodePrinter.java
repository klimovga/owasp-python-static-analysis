package su.msu.cs.lvk.xml2pixy.parser;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 05.10.2007
 * Time: 1:29:01
 */

/**
 * ParseNodeVisitor that just prints ParseNode tokens into given output.
 * If no line-bound comments given, formats the output (indentation, linefeeds etc)
 */
public class ParseNodePrinter implements ParseNodeVisitor {

    private static final int INDENT_STEP = 2;

    private int currentline;
    private int indent;

    private boolean needIndent = false;

    private PrintStream printer;

    Map<Integer, StringWriter> comments;

    public ParseNodePrinter(OutputStream os) {
        printer = new PrintStream(os);
        currentline = 0;
        indent = 0;
    }

    public ParseNodePrinter(OutputStream os, Map<Integer, StringWriter> comments) {
        this(os);
        this.comments = comments;
    }

    public void visit(ParseNode node) {
        if (node.isToken() && node.getSymbol() != PhpSymbols.T_EPSILON) {
            if (comments != null) {
                printComments(node);
            } else {
                printFormatted(node);
            }
        }
    }

    private void printComments(ParseNode node) {
        if (currentline < node.getLineno()) {
            printer.println();
            StringWriter w = comments.get(currentline);
            if (w != null) {
                printer.println("// " + comments.get(currentline).toString().replaceAll("\\r\\n", ", "));
            } else {
                printer.println("// don't have results for line " + currentline);
            }
            currentline = node.getLineno();
            printer.print(currentline + ": ");
        }
        printer.print(node.getLexeme() + " ");
    }

    private void printFormatted(ParseNode node) {
        switch (node.getSymbol()) {
            case PhpSymbols.T_OPEN_CURLY_BRACES:
                indent += INDENT_STEP;
                printer.print(node.getLexeme());
                printer.println();
                needIndent = true;
                break;
            case PhpSymbols.T_CLOSE_CURLY_BRACES:
                indent -= INDENT_STEP;
                doIndent();
                printer.print(node.getLexeme());
                printer.println();
                needIndent = true;
                break;
            case PhpSymbols.T_SEMICOLON:
                printer.print(node.getLexeme());
                printer.println();
                needIndent = true;
                break;
            default:
                if (needIndent) {
                    needIndent = false;
                    doIndent();
                }
                printer.print(node.getLexeme());
                printer.print(' ');
                break;
        }
    }

    private void doIndent() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }
        printer.print(buf.toString());
    }
}
