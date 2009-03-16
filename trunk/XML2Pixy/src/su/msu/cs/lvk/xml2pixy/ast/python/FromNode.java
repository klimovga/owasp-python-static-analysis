package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * User: KlimovGA
 * Date: 25.09.2008
 * Time: 23:44:17
 */
public class FromNode extends ImportNode {

    protected String module;

    public FromNode(Element jdom) {
        super(jdom);

        module = jdom.getAttributeValue("modname");
     }

    public FromNode(Node node) {
        this(node.getJdomElement());
    }

    public void print(PrintStream out) {
        out.append("from ").append(module).append(" import ");
        boolean first = true;
        for (String name : names) {
            if (!first) out.append(", ");
            first = false;
            out.append(name);
        }
    }

    public String getModule() {
        return module;
    }

}
