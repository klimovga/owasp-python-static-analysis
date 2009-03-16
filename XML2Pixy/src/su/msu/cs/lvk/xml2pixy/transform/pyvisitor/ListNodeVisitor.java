package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.ListNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author Panther
 * @created 12.03.2009 1:20:58
 */
public class ListNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        ListNode list = (ListNode) node;
        ParseNode arrayPairList = helper.create(PhpSymbols.array_pair_list);
        ParseNode arrayPair = null;
        for (PythonNode child : list.getNodes()) {
            if (child.getPhpNode() != null) {
                ParseNode tmp = helper.create(PhpSymbols.non_empty_array_pair_list);
                if (arrayPair != null) {
                    tmp.addChild(arrayPair);
                    tmp.addChild(helper.create(PhpSymbols.T_COMMA, ",", node.getLineno()));
                }
                tmp.addChild(makeExpr(child.getPhpNode()));
                arrayPair = tmp;
            }
        }

        if (arrayPair == null) {
            arrayPairList.addChild(makeEpsilon());
        } else {
            arrayPairList.addChild(arrayPair);
            arrayPairList.addChild(helper.create(PhpSymbols.possible_comma, makeEpsilon()));
        }

        ParseNode expr = helper.create(PhpSymbols.expr,
                helper.create(PhpSymbols.expr_without_variable,
                        helper.create(PhpSymbols.T_ARRAY, "array", node.getLineno()),
                        helper.create(PhpSymbols.T_OPEN_BRACES, "(", node.getLineno()),
                        arrayPairList,
                        helper.create(PhpSymbols.T_CLOSE_BRACES, ")", node.getLineno())
                )
        );

        list.setPhpNode(expr);
    }

}