package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:16:17
 */

/**
 * @see PrintVisitor
 */
public class PrintnlVisitor extends PrintVisitor {

    public PrintnlVisitor() {
        super();
    }

    public PrintnlVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }
}
