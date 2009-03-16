package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:20:11
 */

/**
 * @see ListVisitor
 */
public class TupleVisitor extends ListVisitor {

    public TupleVisitor() {
        super();
    }

    public TupleVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

}
