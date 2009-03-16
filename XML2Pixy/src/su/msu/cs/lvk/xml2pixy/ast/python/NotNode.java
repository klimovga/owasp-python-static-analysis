package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 24.09.2008
 * Time: 23:26:23
 */
public class NotNode extends InvertNode {

    public NotNode(PythonNode expr) {
        super(expr);
    }

    public NotNode(Element jdom) {
        super(jdom);
    }

    public NotNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append("not ");
        if (expr != null) expr.print(out);
    }
}
