package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 23:23:37
 */
public class PassNode extends PythonNode {

    public PassNode(Element jdom) {
        super(jdom);
    }

    public PassNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.print("pass");
    }
}
