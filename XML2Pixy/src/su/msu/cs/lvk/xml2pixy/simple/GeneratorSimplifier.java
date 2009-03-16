package su.msu.cs.lvk.xml2pixy.simple;

import su.msu.cs.lvk.xml2pixy.simple.visitor.GeneratorExpressionVisitor;
import su.msu.cs.lvk.xml2pixy.simple.visitor.ListComprehensionVisitor;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GeneratorSimplifier extends AbstractTransformVisitor {

    public GeneratorSimplifier() {
        super();

        visitors.put("GenExprNode", new GeneratorExpressionVisitor());
        visitors.put("ListCompNode", new ListComprehensionVisitor());
                             
    }

}
