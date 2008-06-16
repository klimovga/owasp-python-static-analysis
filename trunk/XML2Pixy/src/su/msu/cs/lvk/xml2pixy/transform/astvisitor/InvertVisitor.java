package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:11:15
 */

/**
 * @see NotVisitor
 */
public class InvertVisitor extends NotVisitor {
    public InvertVisitor() {
        super();
    }

    public InvertVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
