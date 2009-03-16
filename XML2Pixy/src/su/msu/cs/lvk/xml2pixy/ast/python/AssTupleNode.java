package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:06:25
 */
public class AssTupleNode extends AssListNode {

    public AssTupleNode(Element jdom) {
        super(jdom);
    }

    public AssTupleNode(Node node) {
        super(node);
    }
}
