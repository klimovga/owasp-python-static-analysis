package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 22:56:31
 */
public class NameNode extends PythonNode {

    public static final String[] BUILTIN_NAMES = {"None", "True", "False"};

    protected String name;

    public NameNode(Element jdom) {
        super(jdom);
        name = jdom.getAttributeValue("name");
    }

    public NameNode(Node node) {
        this(node.getJdomElement());
    }

    public NameNode(String name) {
        this.name = name;
    }

    public void print(PrintStream out) {
        out.print(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
