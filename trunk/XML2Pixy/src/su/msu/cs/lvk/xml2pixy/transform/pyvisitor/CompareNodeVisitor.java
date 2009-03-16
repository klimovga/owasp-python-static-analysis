package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.CompareNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * User: klimov
 * Date: 19.01.2009
 */
public class CompareNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        CompareNode compare = (CompareNode) node;
        PythonNode left = compare.getExpr();
        PythonNode right = compare.getExprs().get(0);
        String comparator = compare.getOps().get(0);

        if (left.getPhpNode() != null && right.getPhpNode() != null) {
            ParseNode phpComparator = makeCompare(comparator, compare.getLineno());

            if (phpComparator != null) {
                compare.setPhpNode(helper.create(PhpSymbols.expr,
                        helper.create(PhpSymbols.expr_without_variable,
                                makeExprInBraces(left.getPhpNode()),
                                phpComparator,
                                makeExprInBraces(right.getPhpNode()))));
            } else if (comparator.equals("in")) {
                compare.setPhpNode(makeFunctionCall("in_array", new ParseNode[]{left.getPhpNode(), right.getPhpNode()}, null, compare.getLineno()));
            } else if (comparator.equals("not in")) {
                compare.setPhpNode(helper.create(PhpSymbols.expr,
                        helper.create(PhpSymbols.expr_without_variable,
                                helper.create(PhpSymbols.T_NOT, "!", compare.getLineno()),
                                makeFunctionCall("in_array", new ParseNode[]{
                                        left.getPhpNode(),
                                        right.getPhpNode()
                                }, null, compare.getLineno()))));
            }

        }
    }
}
