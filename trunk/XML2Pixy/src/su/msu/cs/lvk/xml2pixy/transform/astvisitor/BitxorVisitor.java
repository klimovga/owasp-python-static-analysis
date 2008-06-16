package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:23:40
 */

/**
 * @see AndVisitor
 */
public class BitxorVisitor extends AndVisitor {
    public BitxorVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public BitxorVisitor() {
        super();
    }
}
