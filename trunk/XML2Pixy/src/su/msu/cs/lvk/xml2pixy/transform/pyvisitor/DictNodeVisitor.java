package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.DictNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.List;

/**
 * @author gklimov
 * @created 15.03.2009 12:33:59
 */
public class DictNodeVisitor extends PythonNodeVisitor {
    public void visit(PythonNode node) throws VisitorException {
        DictNode dict = (DictNode) node;

        ParseNode arrayPairList = helper.create(PhpSymbols.array_pair_list);

        ParseNode neArrayPairList = null;

        List<PythonNode> keys = dict.getKeys();
        List<PythonNode> values = dict.getValues();

        for (int i = 0; i < keys.size(); i++) {
            PythonNode key = keys.get(i);
            PythonNode value = i < values.size() ? values.get(i) : null;
            ParseNode phpKey = key.getPhpNode();
            ParseNode phpValue = value == null ? null : value.getPhpNode();
            if (phpKey != null) {
                // if have not phpValue, set "null" instead of value
                if (phpValue == null) {
                    phpValue = helper.createNull(key.getLineno());
                }

                ParseNode tmp = helper.create(PhpSymbols.non_empty_array_pair_list);
                if (neArrayPairList != null) {
                    helper.addChild(tmp, neArrayPairList).
                            addChild(helper.create(PhpSymbols.T_COMMA, ",", key.getLineno()));
                }
                helper.addChild(tmp, makeExpr(phpKey)).
                        addChild(helper.create(PhpSymbols.T_DOUBLE_ARROW, "=>", key.getLineno())).
                        addChild(makeExpr(phpValue));

                neArrayPairList = tmp;
            }
        }

        if (neArrayPairList != null) {
            arrayPairList.addChild(neArrayPairList);
            arrayPairList.addChild(helper.create(PhpSymbols.possible_comma, makeEpsilon()));
        } else {
            arrayPairList.addChild(makeEpsilon());
        }

        dict.setPhpNode(helper.create(PhpSymbols.expr,
                helper.create(PhpSymbols.expr_without_variable,
                        helper.create(PhpSymbols.T_ARRAY, "array", node.getLineno()),
                        helper.create(PhpSymbols.T_OPEN_BRACES, "(", node.getLineno()),
                        arrayPairList,
                        helper.create(PhpSymbols.T_CLOSE_BRACES, ")", node.getLineno())
                )
        ));

    }
}
