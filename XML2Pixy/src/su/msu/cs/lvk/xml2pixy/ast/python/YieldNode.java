package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * User: KlimovGA
 * Date: 29.09.2008
 * Time: 20:54:26
 */
public class YieldNode extends ReturnNode {

    public YieldNode() {
        super();
    }

    public YieldNode(PythonNode value) {
        super(value);
    }

    public YieldNode(Element jdom) {
        super(jdom);
    }

    public YieldNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append("yield ");
        if (value != null) value.print(out);
    }
}
