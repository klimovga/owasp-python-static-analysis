package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:04:16
 */

/**
 * @see AssAttrVisitor
 */
public class GetattrVisitor extends AssAttrVisitor {
    public GetattrVisitor() {
        super();
    }

    public GetattrVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
