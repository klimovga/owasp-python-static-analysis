package su.msu.cs.lvk.xml2pixy.ast;

import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: KlimovGA
 * Date: 15.10.2008
 * Time: 22:44:46
 */
public interface Visitor {

    boolean visit(ASTNode node) throws VisitorException;

}
