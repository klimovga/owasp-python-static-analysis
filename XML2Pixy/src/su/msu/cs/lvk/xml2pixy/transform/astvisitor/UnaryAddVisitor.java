package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:11:41
 */

/**
 * @see NotVisitor
 */
public class UnaryAddVisitor extends NotVisitor {
    public UnaryAddVisitor() {
        super();
    }

    public UnaryAddVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
