package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;

/**
 * Extract subscripted expressions to distinguished variables.
 */
public class ArrayTargetExprExtractor extends ExpressionExtractor {
    private TemporaryVars temporaryVars;

    public ArrayTargetExprExtractor(TemporaryVars temporaryVars) {
        super();
        this.temporaryVars = temporaryVars;
    }

    public Object createArgIfApplicable(ParseNode node) {
        ParseNode parent = node.getParent();
        if (node.getSymbol() == PhpSymbols.expr && parent.getSymbol() == PhpSymbols.reference_variable
                && parent.getNumChildren() == 4 && parent.getChild(2).getSymbol() == PhpSymbols.dim_offset) {
            return new ExpressionExtractionPoint(node);
        }
        return null;
    }

    protected String createVar() {
        return temporaryVars.createExprVar();
    }

    protected ParseNode createTargetReplacement(ParseNodeHelper helper, ParseNode target, ParseNode referenceVar) {
        return referenceVar;
    }
}
