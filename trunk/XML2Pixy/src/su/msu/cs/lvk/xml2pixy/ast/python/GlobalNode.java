package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 29.09.2008
 * Time: 20:11:33
 */
public class GlobalNode extends PythonNode {

    protected List<String> names = new ArrayList<String>();

    public GlobalNode(Element jdom) {
        super(jdom);

        String names = Utils.trimToNull(jdom.getChildText("names"));
        if (names != null) {
            this.names = Arrays.asList(names.split("\n"));
            for (int i = 0; i < this.names.size(); i++) {
                this.names.set(i, this.names.get(i).trim());
            }
        }

    }

    public GlobalNode(Node node) {
        this(node.getJdomElement());
    }

    public void print(PrintStream out) {
        out.append("global ");
        boolean first = true;
        for (String name : names) {
            if (!first) out.append(", ");
            first = false;
            out.append(name);
        }
    }

    public List<String> getNames() {
        return names;
    }
}
