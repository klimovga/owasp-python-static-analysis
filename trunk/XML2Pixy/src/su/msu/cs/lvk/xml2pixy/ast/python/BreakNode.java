package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:55:18
 */
public class BreakNode extends PythonNode {

    public BreakNode(Element jdom) {
        super(jdom);
    }

    public BreakNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.print("break");
    }
}
