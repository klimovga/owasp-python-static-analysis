package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:23:09
 */

/**
 * @see AndVisitor
 */
public class BitandVisitor extends AndVisitor {
    public BitandVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public BitandVisitor() {
        super();
    }
}
