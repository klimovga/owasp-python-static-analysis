package su.msu.cs.lvk.xml2pixy.jdom;

import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 30.09.2007
 * Time: 1:03:30
 */

/**
 * JDOM visitor for printing xml tag names.
 */
public class PrintVisitor implements JdomVisitor {

    private static PrintStream out = System.out;

    /**
     * Prints xml tag name.
     * @param elem xml tag wrapper
     */
    public void visit(Node elem) {
        out.println(elem.getJdomElement().getName());
    }

}
