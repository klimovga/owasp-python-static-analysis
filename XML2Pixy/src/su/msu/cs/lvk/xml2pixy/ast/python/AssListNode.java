package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.transform.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 23.09.2008
 * Time: 22:05:53
 */
public class AssListNode extends ListNode {

    public AssListNode(Element jdom) {
        super(jdom);
    }

    public AssListNode(Node node) {
        super(node);
    }
    
}
