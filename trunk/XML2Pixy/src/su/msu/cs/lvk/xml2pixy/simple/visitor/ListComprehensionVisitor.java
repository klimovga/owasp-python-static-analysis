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
public class ListComprehensionVisitor extends PythonNodeVisitor {

    public static final String LIST_COMP_RESULT = "__listcomp_result__";

    public void visit(PythonNode node) {
        ListCompNode listComp = (ListCompNode) node;
        FunctionNode genFunc = new FunctionNode();
        genFunc.setName(ProcessingUtils.getNextTempFunction("__listcomp_"));

        // make func arguments from collections used in this listComp
        List<String> argNames = new ArrayList<String>();
        for (int i = 0; i < listComp.getQuals().size(); i++) {
            argNames.add("arg" + i);
        }
        genFunc.setArgNames(argNames);

        // render FOR statement
        StmtNode code = new StmtNode();

        PythonNode body = makeFor(listComp.getQuals(), listComp.getExpr());

        code.addNode(new AssignNode(
                new AssNameNode(LIST_COMP_RESULT).copyLocation(node),
                new ListNode().copyLocation(node)
        ).copyLocation(node));
        code.addNode(body);
        code.addNode(new ReturnNode(new NameNode(LIST_COMP_RESULT).copyLocation(node)).copyLocation(node));

        genFunc.setCode(code);

        addToNearestStmt(node, genFunc);
        node.getParent().replace(node, new CallFuncNode(
                new NameNode(genFunc.getName()).copyLocation(node),
                makeGeneratorArgs(listComp.getQuals()),
                null,
                null
        ).copyLocation(node));

    }

    protected ForNode makeFor(List<ListCompForNode> fors, PythonNode expr) {
        ForNode current = null;

        List<ListCompForNode> reversed = new ArrayList<ListCompForNode>(fors);
        Collections.reverse(reversed);
        int argIndex = reversed.size();
        for (ListCompForNode genFor : reversed) {
            ForNode tmp = (ForNode) new ForNode().copyLocation(genFor);
            tmp.setAssign(genFor.getAssign());
            tmp.setList(new NameNode("arg" + --argIndex).copyLocation(genFor.getList()));
            PythonNode forBody = current == null ? buildAppend(expr) : current;
            IfNode genIf = makeIf(genFor.getIfs(), forBody);
            tmp.setBody(new StmtNode(genIf != null ? genIf : forBody));
            current = tmp;
        }

        return current;
    }

    protected PythonNode buildAppend(PythonNode expr) {
        PythonNode callee = new NameNode(LIST_COMP_RESULT).copyLocation(expr);
        callee = new GetattrNode(callee, ListNode.APPEND_METHOD);

        return new CallFuncNode(callee, Arrays.asList(expr), null, null).copyLocation(expr);
    }

    protected IfNode makeIf(List<ListCompIfNode> ifs, PythonNode code) {
        IfNode ifNode = null;

        List<ListCompIfNode> reversed = new ArrayList<ListCompIfNode>(ifs);
        Collections.reverse(reversed);
        for (ListCompIfNode genIf : reversed) {
            PythonNode ifBody = ifNode == null ? code : ifNode;
            ifNode = (IfNode) new IfNode(
                    Arrays.asList((PythonNode) genIf.getTest()),
                    Arrays.asList(new StmtNode(ifBody)),
                    null).copyLocation(genIf);
        }

        return ifNode;
    }

    protected List<PythonNode> makeGeneratorArgs(List<ListCompForNode> quals) {
        List<PythonNode> args = new ArrayList<PythonNode>();
        for (ListCompForNode qual : quals) {
            args.add(qual.getList());
        }
        return args;
    }
}