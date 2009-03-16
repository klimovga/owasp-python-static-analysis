package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * User: KlimovGA
 * Date: 01.10.2008
 * Time: 20:26:27
 */
public class KeywordNode extends InvertNode {

    protected String name;
    
    protected KeywordNode() {
        super();
    }

    public KeywordNode(Element jdom) {
        super(jdom);

        name = jdom.getAttributeValue("name");
    }

    public KeywordNode(Node node) {
        super(node);

        name = node.getJdomElement().getAttributeValue("name");
    }

    public void print(PrintStream out) {
        out.append(name).append('=');
        expr.print(out);
    }

    public String getName() {
        return name;
    }
}
