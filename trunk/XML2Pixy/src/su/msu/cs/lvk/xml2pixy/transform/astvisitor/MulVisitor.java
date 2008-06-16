package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:19:52
 */

/**
 * @see AddVisitor
 */
public class MulVisitor extends AddVisitor {
    public MulVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public MulVisitor() {
        super();
    }
}
