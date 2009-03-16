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
 * Time: 20:46:25
 */
public class ImportNode extends PythonNode {

    protected List<String> names = new ArrayList<String>();

    public ImportNode(Element jdom) {
        super(jdom);

        String names = Utils.trimToNull(jdom.getChildText("names"));
         if (names != null) {
             this.names = Arrays.asList(names.split(","));
         }
    }

    public ImportNode(Node node) {
        this(node.getJdomElement());
    }

    public void print(PrintStream out) {
        out.append("import ");
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
