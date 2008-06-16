package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class ArrayElement {
    private ParseNode index;
    private ParseNode expr;

    public ArrayElement(ParseNode index, ParseNode expr) {
        this.index = index;
        this.expr = expr;
    }


    public ParseNode getIndex() {
        return index;
    }

    public void setIndex(ParseNode index) {
        this.index = index;
    }

    public ParseNode getExpr() {
        return expr;
    }

    public void setExpr(ParseNode expr) {
        this.expr = expr;
    }
}

/**
 * Transform initializers like $x = array(1, 2) to several statements:
 * <pre>
 *   $t_arr_0 = array();
 *   $t_arr_0[0] = 1;
 *   $t_arr_0[1] = 2;
 *   $x = &$t_arr_0;
 * </pre>
 *
 * @author ikonv
 */
public class ArrayInitializerTransformer implements NodeTransformer {
    private TemporaryVars temporaryVars;

    public ArrayInitializerTransformer(TemporaryVars temporaryVars) {
        this.temporaryVars = temporaryVars;
    }

    public Object createArgIfApplicable(ParseNode node) {
        if (ParseNodeHelper.childrenMatch(node,
                PhpSymbols.T_ARRAY, PhpSymbols.T_OPEN_BRACES, PhpSymbols.array_pair_list, PhpSymbols.T_CLOSE_BRACES)) {
            ParseNode arrayPairList = node.getChild(2);
            if (arrayPairList.getNumChildren() != 1 || arrayPairList.getChild(0).getSymbol() != PhpSymbols.T_EPSILON ) {
                return node;
            }
        }

        return null;
    }

    /**
     * Extract non-empty initializing array.
     *
     * @param arg ParseNode, root of array initializer
     */
    public void apply(Object arg) {
        final ParseNode node = (ParseNode) arg;

        final String arrayName = temporaryVars.createArrayVar();

        ParseNode statement = TransformerHelper.findParentStatement(node);

        // extract array(...) to distinguished variable
        new ExpressionExtractor() {
            public Object createArgIfApplicable(ParseNode node) {
                return null;
            }

            protected String createVar() {
                return arrayName;
            }

            protected ParseNode createTargetReplacement(ParseNodeHelper helper, ParseNode target, ParseNode referenceVar) {
                ParseNode parent = node.getParent();
                boolean isW_cvar = parent.getSymbol() != PhpSymbols.expr;
                boolean isCvar = parent.getChild(0).getSymbol() == PhpSymbols.T_RETURN;
                ParseNode cvar = helper.createChain(new int[]{
                        PhpSymbols.cvar, PhpSymbols.cvar_without_objects},
                        referenceVar);
                if (isW_cvar && !isCvar) {
                    return helper.create(PhpSymbols.w_cvar, cvar);
                } else if (isCvar) {
                    return cvar;
                } else {
                    return helper.create(PhpSymbols.r_cvar, cvar);
                }
            }
        }.apply(new ExpressionExtractionPoint(node));

        // find nodes of elements
        ParseNodeHelper h = new ParseNodeHelper();
        h.setCurrentFile(node.getFileName());

        List<ArrayElement> elements = new ArrayList<ArrayElement>();
        ParseNode pairList = node.getChild(2).getChild(0); // non_empty_array_pair_list
        while (pairList != null) {
            // that's weird...
            if (pairList.getNumChildren() == 1) {
                // element is expr, no more elements
                elements.add(new ArrayElement(null, pairList.getChild(0)));
                pairList = null;
            } else if (pairList.getNumChildren() == 3) {
                if (pairList.getChild(1).getSymbol() == PhpSymbols.T_COMMA) {
                    // element is expr, more elements exist
                    elements.add(new ArrayElement(null, pairList.getChild(2)));
                    pairList = pairList.getChild(0);
                } else if (pairList.getChild(1).getSymbol() == PhpSymbols.T_DOUBLE_ARROW) {
                    // element is index => expr, no more elements
                    elements.add(new ArrayElement(pairList.getChild(0), pairList.getChild(2)));
                    pairList = null;
                } else {
                    throw new RuntimeException("Inconsistent parse tree");
                }
            } else if (pairList.getNumChildren() == 5) {
                // element is index => expr, more elements exist
                if (pairList.getChild(3).getSymbol() != PhpSymbols.T_DOUBLE_ARROW) {
                    throw new RuntimeException("Inconsistent parse tree");
                }

                elements.add(new ArrayElement(pairList.getChild(2), pairList.getChild(4)));
                pairList = pairList.getChild(0);
            } else {
                throw new RuntimeException("Inconsistent parse tree");
            }
        }
        Collections.reverse(elements);

        // clear children
        int lineno = -1;
        node.getChild(2).getChildren().clear();
        node.getChild(2).addChild(h.create(PhpSymbols.T_EPSILON, "epsilon", lineno));

        // create assignment for all elements
        int elemNo = 0;
        for (ArrayElement elem : elements) {
            // create $arr[i] = elem
            ParseNode arrName = h.create(PhpSymbols.reference_variable,
                    h.create(PhpSymbols.compound_variable,
                            h.create(PhpSymbols.T_VARIABLE,
                                arrayName, lineno)));
            ParseNode dimOffset = elem.getIndex() != null
                    ? h.create(PhpSymbols.dim_offset, elem.getIndex())
                    : h.create(PhpSymbols.dim_offset,
                        h.create(PhpSymbols.expr,
                                h.create(PhpSymbols.expr_without_variable,
                                    h.create(PhpSymbols.scalar,
                                        h.create(PhpSymbols.common_scalar,
                                                    h.create(PhpSymbols.T_LNUMBER, Integer.toString(elemNo), lineno))))));
            ParseNode cvar = h.create(PhpSymbols.cvar,
                        h.create(PhpSymbols.cvar_without_objects,
                            h.create(PhpSymbols.reference_variable,
                                arrName,
                                h.create(PhpSymbols.T_OPEN_RECT_BRACES, "[", lineno),
                                dimOffset,
                                h.create(PhpSymbols.T_CLOSE_RECT_BRACES, "]", lineno))));

            ParseNode expr = h.create(PhpSymbols.expr,
                    h.create(PhpSymbols.expr_without_variable,
                            cvar,
                            h.create(PhpSymbols.T_ASSIGN, "=", lineno),
                            elem.getExpr()));
            TransformerHelper.insertExprAsStatementBefore(statement, expr);
            elemNo++;
        }
    }
}

