package su.msu.cs.lvk.xml2pixy.parser;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 05.10.2007
 * Time: 1:28:29
 */
public interface ParseNodeVisitor {

    void visit(ParseNode node);

}
