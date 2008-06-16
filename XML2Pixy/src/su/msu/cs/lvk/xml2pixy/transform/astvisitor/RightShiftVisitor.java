package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:20:44
 */

/**
 * @see AddVisitor
 */
public class RightShiftVisitor extends AddVisitor {
    public RightShiftVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public RightShiftVisitor() {
        super();
    }
}
