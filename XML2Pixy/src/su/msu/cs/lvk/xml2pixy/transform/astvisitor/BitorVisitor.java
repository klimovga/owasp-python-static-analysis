package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:23:26
 */

/**
 * @see AndVisitor
 */
public class BitorVisitor extends AndVisitor {
    public BitorVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public BitorVisitor() {
        super();
    }
}
