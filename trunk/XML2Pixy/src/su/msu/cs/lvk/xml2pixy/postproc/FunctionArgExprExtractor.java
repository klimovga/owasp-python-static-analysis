package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;

/**
 * @author ikonv
 */
public class FunctionArgExprExtractor extends ExpressionExtractor {
    private TemporaryVars temporaryVars;

    public FunctionArgExprExtractor(TemporaryVars temporaryVars) {
        super();
        this.temporaryVars = temporaryVars;
    }

    public Object createArgIfApplicable(ParseNode node) {
        ParseNode parent = node.getParent();
        if (parent != null && parent.getSymbol() == PhpSymbols.non_empty_function_call_parameter_list
                && node.getSymbol() == PhpSymbols.expr_without_variable) {
            return new ExpressionExtractionPoint(node);
        }

        return null;
    }


    protected String createVar() {
        return temporaryVars.createArgVar();
    }
}
