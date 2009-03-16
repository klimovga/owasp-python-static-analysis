package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 22.09.2008
 * Time: 22:56:09
 */
public class ConstNode extends PythonNode {

    protected String stringValue;
    protected Integer intValue;
    protected Double doubleValue;

    public ConstNode(String value) {
        stringValue = value;
    }

    public ConstNode(int value) {
        intValue = value;
    }

    public ConstNode(double value) {
        doubleValue = value;
    }

    public ConstNode(Element jdom) {
        super(jdom);

        if (jdom.getAttribute("value") != null) {
            // exact value, no trimming
            stringValue = jdom.getAttribute("value").getValue();
        } else if (jdom.getChild("value") != null) {
            // indented value, trim it
            stringValue = jdom.getChild("value").getText().trim();
        }
        stringValue = Utils.trimToNull(stringValue);

        if (stringValue != null) {
            intValue = getInteger(stringValue);
            doubleValue = getDouble(stringValue);
        }
    }

    public ConstNode(Node node) {
        this(node.getJdomElement());
    }

    public void print(PrintStream out) {
        Object value = getValue();
        if (value != null) out.print(value);
    }

    public Object getValue() {
        return intValue != null
                ? intValue
                : doubleValue != null
                ? doubleValue
                : stringValue != null
                ? '"' + stringValue + '"'
                : null;
    }

}
