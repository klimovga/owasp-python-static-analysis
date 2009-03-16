package su.msu.cs.lvk.xml2pixy.ast;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import su.msu.cs.lvk.xml2pixy.Printable;
import su.msu.cs.lvk.xml2pixy.Utils;

import java.io.PrintStream;
import java.util.List;

/**
 * User: Panther
 * Date: 22.09.2008
 */
public abstract class ASTNode implements Printable {

    protected String nodeName;
    protected int lineno;
    protected String fileName;

    protected ParseNode phpNode;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void print(PrintStream out) {
        out.print(nodeName + ' ');
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public abstract List<ASTNode> getChildren();

    public String toString() {
        return Utils.toString(this);
    }

    public String getLocation() {
        return fileName + ":" + lineno;
    }

    public ParseNode getPhpNode() {
        return phpNode;
    }

    public void setPhpNode(ParseNode phpNode) {
        this.phpNode = phpNode;
    }
}
