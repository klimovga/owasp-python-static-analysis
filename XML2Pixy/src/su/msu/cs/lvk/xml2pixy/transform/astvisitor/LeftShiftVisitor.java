package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:21:05
 */

/**
 * @see AddVisitor
 */
public class LeftShiftVisitor extends AddVisitor {
    public LeftShiftVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public LeftShiftVisitor() {
        super();
    }
}
