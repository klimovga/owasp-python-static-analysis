package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:19:24
 */

/**
 * @see AddVisitor
 */
public class SubVisitor extends AddVisitor {
    public SubVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public SubVisitor() {
        super();
    }
}
