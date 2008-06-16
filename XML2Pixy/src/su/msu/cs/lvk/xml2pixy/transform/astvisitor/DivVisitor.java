package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:20:21
 */

/**
 * @see AddVisitor
 */
public class DivVisitor extends AddVisitor {
    public DivVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public DivVisitor() {
        super();
    }
}
