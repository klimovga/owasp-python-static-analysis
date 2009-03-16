package su.msu.cs.lvk.xml2pixy.simple.visitor;

import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.Visitor;
import su.msu.cs.lvk.xml2pixy.ast.Walker;
import su.msu.cs.lvk.xml2pixy.ast.python.*;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: klimov
 * Date: 13.01.2009
 */
public class FunctionNodeVisitor extends PythonNodeVisitor {

    /**
     * Rename function using fully specified function name, i.e. module__func and move its declaration to module.
     * If function is generator function, split into two functions - constructor and method next.
     *
     * @param node node
     */
    public void visit(PythonNode node) {
        FunctionNode function = (FunctionNode) node;
        Symbol functionSymbol = function.getScope().getSymbol(function.getName());
        if (functionSymbol == null || functionSymbol.isFunction()) {
            processFunction(function);
        } else if (functionSymbol.isGeneratorFunction()) {
            processGeneratorFunction(function);
        }


    }

    private void processFunction(FunctionNode function) {
        ModuleNode module = function.getGlobalScope();

        updateNames(function);
        StmtNode parentStmt = (StmtNode) function.getParent();
        parentStmt.getNodes().remove(function);

        module.getStmt().addNode(0, function);
    }


    /**
     * if we have generator, then function itself is replaced with constructor:
     * <pre><code>
     * def f(a, b):
     *     yield a
     *     yield b
     * </code></pre>
     * is replaced with
     * <pre><code>
     * def f(a, b):
     *     self = {'a': a, 'b': b}
     *     return self
     * </code></pre>
     * <p/>
     * and next() method is rendered into
     * <pre><code>
     * def f__next(self):
     *     #import random
     *     a = self['a']
     *     b = local['b']
     *     if random.randint(0, 1):
     *         return self['a']
     *     if random.randint(0, 1):
     *         return self['b']
     * </code></pre>
     *
     * @param function function node corresponding to generator function
     */
    private void processGeneratorFunction(FunctionNode function) {

        ModuleNode module = function.getGlobalScope();

        updateNames(function);

        List<String> argNames = function.getArgNames();

        StmtNode parentStmt = (StmtNode) function.getParent();
        parentStmt.getNodes().remove(function);

        StmtNode nextCode = function.getCode();

        // generate generator function code
        StmtNode genCode = new StmtNode();

        List<PythonNode> keys = new ArrayList<PythonNode>();
        List<PythonNode> values = new ArrayList<PythonNode>();

        for (String arg : argNames) {
            keys.add(new ConstNode(arg));
            values.add(new NameNode(arg));
        }
        if (function.getVarArg() != null) {
            keys.add(new ConstNode(function.getVarArg()));
            values.add(new NameNode(function.getVarArg()));
        }
        if (function.getKwArg() != null) {
            keys.add(new ConstNode(function.getKwArg()));
            values.add(new NameNode(function.getKwArg()));
        }

        genCode.addNode(new AssignNode(new AssNameNode("self"), new DictNode(keys, values)));
        genCode.addNode(new ReturnNode(new NameNode("self")));
        function.setCode(genCode);

        // generate next method
        FunctionNode next = (FunctionNode) new FunctionNode().copyLocation(function);
        next.setName(ProcessingUtils.concatMangled(function.getName(), ProcessingUtils.GENERATOR_NEXT));
        next.setArgNames(Arrays.asList("self"));
        next.setCode(nextCode);

        int i = 0;
        for (String arg : argNames) {
            nextCode.addNode(i++, new AssignNode(new AssNameNode(arg), new SubscriptNode(new NameNode("self"), new ConstNode(arg))));
        }
        if (function.getVarArg() != null) {
            nextCode.addNode(i++, new AssignNode(new AssNameNode(function.getVarArg()),
                    new SubscriptNode(new NameNode("self"), new ConstNode(function.getVarArg()))));
        }
        if (function.getKwArg() != null) {
            nextCode.addNode(i++, new AssignNode(new AssNameNode(function.getKwArg()),
                    new SubscriptNode(new NameNode("self"), new ConstNode(function.getKwArg()))));
        }

        replaceYields(next);

        // add function definitions to code
        module.getStmt().addNode(0, next);
        module.getStmt().addNode(0, function);
    }

    private void updateNames(FunctionNode function) {
        List<String> argNames = function.getArgNames();
        function.setName(ProcessingUtils.getMangledName(function));

        for (int i = 0; i < argNames.size(); i++) {
            argNames.set(i, ProcessingUtils.concatMangled(function.getName(), argNames.get(i)));
        }
        if (function.getVarArg() != null) {
            function.setVarArg(ProcessingUtils.concatMangled(function.getName(), function.getVarArg()));
        }
        if (function.getKwArg() != null) {
            function.setKwArg(ProcessingUtils.concatMangled(function.getName(), function.getKwArg()));
        }

    }

    private void replaceYields(final FunctionNode function) {
        Visitor visitor = new Visitor() {
            public boolean visit(ASTNode node) throws VisitorException {
                if (node instanceof YieldNode) {
                    YieldNode yield = (YieldNode) node;

                    if (yield.getScope() == function) {
                        PythonNode callRandom = callRandom(yield);
                        PythonNode returnNode = new ReturnNode(yield.getValue()).copyLocation(yield);
                        StmtNode stmtNode = (StmtNode) new StmtNode(returnNode).copyLocation(yield);
                        PythonNode ifNode = new IfNode(
                                Arrays.asList(callRandom),
                                Arrays.asList(stmtNode),
                                null
                        ).copyLocation(yield);
                        yield.getParent().replace(yield, ifNode);
                    }
                }
                return true;
            }
        };

        Walker walker = new Walker(visitor);
        walker.walkDeep(function.getCode());
    }

}
