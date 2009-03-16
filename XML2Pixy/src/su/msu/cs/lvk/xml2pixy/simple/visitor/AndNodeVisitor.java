package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.util.Arrays;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 04.12.2008
 */
public class AndNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        AndNode and = (AndNode) node;

        PythonNode parent = node.getParent();
        List<PythonNode> operands = and.getNodes();
        PythonNode tmp = null;
        for (PythonNode operand : operands) {
            if (tmp == null) {
                tmp = operand;
                continue;
            }
            String tmpVar = ProcessingUtils.getNextTempVar();

            // if first operand - just assign it to new variable (for lazy calculations)
            PythonNode expr = createOperator(and, Arrays.asList(tmp, operand));
            AssignNode assignTmp = (AssignNode) new AssignNode(new AssNameNode(tmpVar).copyLocation(and), expr).copyLocation(and);

            // TODO lazy calculation of "and" and "or" expressions
/*
            if (node.getClass() == AndNode.class || node.getClass() == OrNode.class) {
                
            }
*/

            addToNearestStmt(parent, assignTmp);
            tmp = new NameNode(tmpVar).copyLocation(and);
        }

        parent.replace(and, tmp);

    }

    protected AndNode createOperator(AndNode source, List<PythonNode> nodes) {
        AndNode result;

        try {
            result = (AndNode) source.getClass().getConstructor(List.class).newInstance(nodes).copyLocation(source);
        } catch (Exception e) {
            result = null;
        }

        return result;
    }

}
