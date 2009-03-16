package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 24.09.2008
 * Time: 23:27:24
 */
public class OrNode extends AndNode {

    public OrNode(Element jdom) {
        super(jdom);
    }

    public OrNode(Node node) {
        super(node);
    }

    public OrNode(List<PythonNode> nodes) {
        super(nodes);
    }

    public void print(PrintStream out) {
        out.append('(');
        boolean first = true;
        for (PythonNode child : nodes) {
            if (!first) out.print(" or ");
            first = false;
            child.print(out);
        }
        out.append(')');  
    }
}
