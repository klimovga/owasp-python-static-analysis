package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 24.09.2008
 * Time: 23:28:31
 */
public class PowerNode extends AddNode {

    public PowerNode(Element jdom) {
        super(jdom);
    }

    public PowerNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append('(');
        if (left != null) left.print(out);
        out.append(" ^ ");
        if (right != null) right.print(out);
        out.append(')');
    }
}
