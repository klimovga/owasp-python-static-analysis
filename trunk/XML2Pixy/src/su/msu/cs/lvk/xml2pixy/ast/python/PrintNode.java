package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 22:56:57
 */
public class PrintNode extends PrintnlNode {

    public PrintNode(Element jdom) {
        super(jdom);
    }

    public PrintNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        super.print(out);
        out.print(',');
    }

}
