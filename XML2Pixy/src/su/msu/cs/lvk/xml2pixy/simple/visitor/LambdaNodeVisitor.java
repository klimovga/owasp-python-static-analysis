package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.util.List;

/**
 * User: klimov
 * Date: 13.01.2009
 */
public class LambdaNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) {
        LambdaNode lambda = (LambdaNode) node;

        ScopeNode scope = lambda.getScope();
        lambda.getParent().replace(lambda, new NameNode(lambda.getGeneratedName()).copyLocation(lambda));
        ModuleNode module = scope.getGlobalScope();

        List<String> argNames = lambda.getArgNames();
        for (int i = 0 ; i < argNames.size(); i++) {
            argNames.set(i, ProcessingUtils.getMangledName(scope, argNames.get(i)));
        }

        module.getStmt().addNode(0, new FunctionNode(lambda));


    }

}
