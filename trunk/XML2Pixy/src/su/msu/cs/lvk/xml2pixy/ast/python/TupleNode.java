package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:05:37
 */
public class TupleNode extends ListNode {

    public TupleNode(Element jdom) {
        super(jdom);
    }

    public TupleNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        boolean first = true;
        out.append('(');
        for (PythonNode child : nodes) {
            if (!first) out.append(", ");
            first = false;
            child.print(out);
        }
        out.append(')');
    }
}
