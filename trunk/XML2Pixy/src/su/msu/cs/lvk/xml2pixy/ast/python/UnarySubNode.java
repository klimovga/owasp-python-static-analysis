package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 24.09.2008
 * Time: 23:30:47
 */
public class UnarySubNode extends InvertNode {
    public UnarySubNode(Element jdom) {
        super(jdom);
    }

    public UnarySubNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.append('-');
        if (expr != null) expr.print(out);
    }
}
