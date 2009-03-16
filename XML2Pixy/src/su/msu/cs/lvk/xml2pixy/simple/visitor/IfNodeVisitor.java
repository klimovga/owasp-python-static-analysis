package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 02.12.2008
 */
public class IfNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        IfNode ifNode = (IfNode) node;

        StmtNode parent = (StmtNode) ifNode.getParent();
        List<PythonNode> tests = ifNode.getTests();
        List<StmtNode> stmts = ifNode.getStmts();
        StmtNode elze = ifNode.getElse();
        int index = parent.getNodes().indexOf(ifNode);
        parent.getNodes().remove(ifNode);

        List<String> prevVars = new ArrayList<String>(); // used for constructing if-else conditions
        // iterate over conditions
        for (int i = 0; i < tests.size(); i++) {
            PythonNode test = tests.get(i);
            StmtNode stmt = stmts.get(i);

            // Process condition. Result is:
            // tmp = condition
            // if tmp:
            String tmpConditionVarName = ProcessingUtils.getNextTempVar();
            IfNode newIf = new IfNode(Arrays.asList(new NameNode(tmpConditionVarName).copyLocation(test)),
                    Arrays.asList(stmt),
                    null);
            // Build complex if-else condition as: not test1 and test2
            AndNode newTest = null;
            if (i != 0) {
                newTest = (AndNode) new AndNode(buildInvertedNameNodes(prevVars)).copyLocation(test);
                newTest.addNode(test);
            }
            // add statements to parent statement
            parent.addNode(index++, new AssignNode(
                    new AssNameNode(tmpConditionVarName).copyLocation(test),
                    newTest == null ? test : newTest
            ).copyLocation(ifNode));
            parent.addNode(index++, newIf);

            prevVars.add(tmpConditionVarName);
        }

        // append else
        if (elze != null) {
            String tmpConditionVarName = ProcessingUtils.getNextTempVar();
            IfNode newIf = (IfNode) new IfNode(Arrays.asList(new NameNode(tmpConditionVarName).copyLocation(elze)),
                    Arrays.asList(elze),
                    null).copyLocation(elze);
            // Build condition: not test1 and not test2
            AndNode newTest = (AndNode) new AndNode(buildInvertedNameNodes(prevVars)).copyLocation(newIf);
            parent.addNode(index++, new AssignNode(
                    new AssNameNode(tmpConditionVarName).copyLocation(newTest), newTest
            ).copyLocation(newTest));
            parent.addNode(index, newIf);

        }

    }

    /**
     * Builds list of (not varN) nodes.
     *
     * @param names list of variable names
     * @return list of (not varN) nodes
     */
    protected List<PythonNode> buildInvertedNameNodes(List<String> names) {
        List<PythonNode> list = new ArrayList<PythonNode>();
        for (String name : names) {
            list.add(new NotNode(new NameNode(name)));
        }
        return list;
    }

}
