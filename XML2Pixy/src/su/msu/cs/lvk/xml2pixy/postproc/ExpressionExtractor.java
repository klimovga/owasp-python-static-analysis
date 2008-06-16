package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;

/**
 * @author ikonv
 */
public abstract class ExpressionExtractor implements NodeTransformer {
    protected ExpressionExtractor() {
    }

    public void apply(Object arg) {
        ExpressionExtractionPoint point = (ExpressionExtractionPoint) arg;
        ParseNode statementNode = TransformerHelper.findParentStatement(point.getParent());
        String varName = createVar();

        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(statementNode.getFileName());

        ParseNode targetReplacement = createTargetReplacement(helper,
                point.getTargetNode(), helper.createChain(new int[]{
                PhpSymbols.reference_variable, PhpSymbols.compound_variable
        }, helper.create(PhpSymbols.T_VARIABLE, varName, -1)));
        // place to extraction point
        point.getParent().getChildren().set(point.getChildIndex(), targetReplacement);
        targetReplacement.setParent(point.getParent());

        ParseNode cvarExpr = helper.createChain(new int[]{
                PhpSymbols.cvar, PhpSymbols.cvar_without_objects,
                PhpSymbols.reference_variable, PhpSymbols.compound_variable},
                helper.create(PhpSymbols.T_VARIABLE, varName, -1));

        ParseNode rhsExpr = (point.getTargetNode().getSymbol() == PhpSymbols.expr_without_variable)
                ? helper.create(PhpSymbols.expr, point.getTargetNode())
                : point.getTargetNode();

        ParseNode expr = helper.create(PhpSymbols.expr,
                helper.create(PhpSymbols.expr_without_variable,
                        cvarExpr, helper.create(PhpSymbols.T_ASSIGN, "=", -1), rhsExpr));

        // insert new statement just before function call
        TransformerHelper.insertExprAsStatementBefore(statementNode, expr);
    }

    protected abstract String createVar();

    protected ParseNode createTargetReplacement(ParseNodeHelper helper, ParseNode target, ParseNode referenceVar) {
        return helper.createChain(new int[]{
                PhpSymbols.cvar, PhpSymbols.cvar_without_objects},
                referenceVar);
    }
}
