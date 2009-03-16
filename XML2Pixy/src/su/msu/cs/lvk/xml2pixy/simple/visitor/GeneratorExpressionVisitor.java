package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: klimov
 * Date: 10.03.2009
 */
public class GeneratorExpressionVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        GenExprNode genExpr = (GenExprNode) node;
        GenExprInnerNode inner = genExpr.getCode();
        FunctionNode genFunc = new FunctionNode();
        genFunc.setName(ProcessingUtils.getNextTempFunction("__generator_"));

        // make func arguments from collections used in this genExpr
        List<String> argNames = new ArrayList<String>();
        for (int i = 0; i < inner.getQuals().size(); i++) {
            argNames.add("arg" + i);
        }
        genFunc.setArgNames(argNames);

        // render FOR statement
        StmtNode code = new StmtNode(makeFor(inner.getQuals(), inner.getExpr()));

        genFunc.setCode(code);

        addToNearestStmt(node, genFunc);
        node.getParent().replace(node, new CallFuncNode(
                new NameNode(genFunc.getName()).copyLocation(node),
                makeGeneratorArgs(inner.getQuals()),
                null,
                null
        ).copyLocation(node));

    }

    protected ForNode makeFor(List<GenExprForNode> fors, PythonNode expr) {
        ForNode current = null;

        List<GenExprForNode> reversed = new ArrayList<GenExprForNode>(fors);
        Collections.reverse(reversed);
        int argIndex = reversed.size();
        for (GenExprForNode genFor : reversed) {
            ForNode tmp = (ForNode) new ForNode().copyLocation(genFor);
            tmp.setAssign(genFor.getAssign());
            tmp.setList(new NameNode("arg" + --argIndex).copyLocation(genFor.getIter()));
            PythonNode forBody = current == null ? new YieldNode(expr).copyLocation(expr) : current;
            IfNode genIf = makeIf(genFor.getIfs(), forBody);
            tmp.setBody(new StmtNode(genIf != null ? genIf : forBody));
            current = tmp;
        }

        return current;
    }

    protected IfNode makeIf(List<GenExprIfNode> ifs, PythonNode code) {
        IfNode ifNode = null;

        List<GenExprIfNode> reversed = new ArrayList<GenExprIfNode>(ifs);
        Collections.reverse(reversed);
        for (GenExprIfNode genIf : reversed) {
            PythonNode ifBody = ifNode == null ? code : ifNode;
            ifNode = (IfNode) new IfNode(
                    Arrays.asList((PythonNode) genIf.getTest()),
                    Arrays.asList(new StmtNode(ifBody)),
                    null).copyLocation(genIf);
        }

        return ifNode;
    }

    protected List<PythonNode> makeGeneratorArgs(List<GenExprForNode> quals) {
        List<PythonNode> args = new ArrayList<PythonNode>();
        for (GenExprForNode qual : quals) {
            args.add(qual.getIter());
        }
        return args;
    }
}
