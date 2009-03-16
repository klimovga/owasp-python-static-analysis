package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:49:47
 */
public class BitandNode extends AndNode {

    public BitandNode(List<PythonNode> nodes) {
        super(nodes);
    }

    public BitandNode() {
        super();
    }

    public BitandNode(Element jdom) {
        super(jdom);
    }

    public BitandNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append('(');
        boolean first = true;
        for (PythonNode child : nodes) {
            if (!first) out.print(" & ");
            first = false;
            child.print(out);
        }
        out.append(')');
    }
}
