package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:45:17
 */

/**
 * @see AssListVisitor
 */
public class AssTupleVisitor extends AssListVisitor {

    public AssTupleVisitor() {
        super();
    }

    public AssTupleVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

}
