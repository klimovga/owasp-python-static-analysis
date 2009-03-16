package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 22:55:32
 */
public class AssNameNode extends NameNode {

    protected boolean delete;

    public AssNameNode(String name) {
        super(name);
    }

    public AssNameNode(Element jdom) {
        super(jdom);

        delete = ProcessingUtils.DELETE_FLAG.equalsIgnoreCase(jdom.getAttributeValue("flags"));

    }

    public AssNameNode(Node node) {
        this(node.getJdomElement());
    }

    public void print(PrintStream out) {
        if (delete) out.append("del ");
        super.print(out);
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
