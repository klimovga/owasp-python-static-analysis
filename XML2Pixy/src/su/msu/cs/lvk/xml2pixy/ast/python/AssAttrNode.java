package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 21:50:38
 */
public class AssAttrNode extends GetattrNode {

    public AssAttrNode(Element jdom) {
        super(jdom);
    }

    public AssAttrNode(Node node) {
        super(node);
    }
}
