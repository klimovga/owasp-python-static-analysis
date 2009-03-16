package su.msu.cs.lvk.xml2pixy.simple;

import su.msu.cs.lvk.xml2pixy.simple.visitor.AddNodeVisitor;
import su.msu.cs.lvk.xml2pixy.simple.visitor.AndNodeVisitor;
import su.msu.cs.lvk.xml2pixy.simple.visitor.IfNodeVisitor;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class SSATransformer extends AbstractTransformVisitor {

    public SSATransformer() {
        super();

        visitors.put("AddNode", new AddNodeVisitor());
        visitors.put("AndNode", new AndNodeVisitor());
        visitors.put("IfNode", new IfNodeVisitor());
    }
}
