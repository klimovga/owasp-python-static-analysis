package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:22:46
 */

/**
 * @see AndVisitor
 */
public class OrVisitor extends AndVisitor {
    public OrVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public OrVisitor() {
        super();
    }
}
