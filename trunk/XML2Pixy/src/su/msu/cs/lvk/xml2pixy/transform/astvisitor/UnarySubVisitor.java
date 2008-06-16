package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:12:04
 */

/**
 * @see NotVisitor
 */
public class UnarySubVisitor extends NotVisitor {
    public UnarySubVisitor() {
        super();
    }

    public UnarySubVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
