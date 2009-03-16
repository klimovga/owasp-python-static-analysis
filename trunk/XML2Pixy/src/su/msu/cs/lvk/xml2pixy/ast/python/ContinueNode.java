package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 24.09.2008
 * Time: 22:44:14
 */
public class ContinueNode extends PythonNode {

    public ContinueNode(Element jdom) {
        super(jdom);
    }

    public ContinueNode(Node node) {
        super(node);
    }

    public void print(PrintStream out) {
        out.print("continue");
    }

}
