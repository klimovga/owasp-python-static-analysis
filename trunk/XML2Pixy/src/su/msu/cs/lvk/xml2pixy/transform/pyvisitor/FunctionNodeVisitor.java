package su.msu.cs.lvk.xml2pixy.transform.pyvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.ast.python.FunctionNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

/**
 * @author Panther
 * @created 12.03.2009 1:20:58
 */
public class FunctionNodeVisitor extends PythonNodeVisitor {

    public void visit(PythonNode node) throws VisitorException {
        FunctionNode function = (FunctionNode) node;

        if (function.getCode().getPhpNode() != null) {

            ParseNode decStmt = helper.create(PhpSymbols.unticked_declaration_statement,
                    helper.create(PhpSymbols.T_FUNCTION, "function", node.getLineno()),
                    helper.create(PhpSymbols.is_reference, makeEpsilon()),
                    helper.create(PhpSymbols.T_STRING, function.getName(), node.getLineno()),
                    helper.create(PhpSymbols.T_OPEN_BRACES, "(", node.getLineno()),
                    makeFunctionParameterList(function),
                    helper.create(PhpSymbols.T_CLOSE_BRACES, ")", node.getLineno()),
                    helper.create(PhpSymbols.T_OPEN_CURLY_BRACES, "{", node.getLineno()),
                    top2innerStatement(function.getCode().getPhpNode()),
                    helper.create(PhpSymbols.T_CLOSE_CURLY_BRACES, "}", node.getLineno()));


            function.setPhpNode(helper.create(PhpSymbols.declaration_statement, decStmt));
        }
    }

    protected ParseNode makeFunctionParameterList(FunctionNode function) {
        ParseNode paramList = null;

        for (String name : function.getArgNames()) {
            ParseNode tmp = helper.create(PhpSymbols.non_empty_parameter_list);
            if (paramList != null) {
                tmp.addChild(paramList);
                tmp.addChild(helper.create(PhpSymbols.T_COMMA, ",", function.getLineno()));
            }
            tmp.addChild(helper.create(PhpSymbols.T_BITWISE_AND, "&", function.getLineno()));
            tmp.addChild(helper.create(PhpSymbols.T_VARIABLE, "$" + name, function.getLineno()));

            paramList = tmp;
        }

        return helper.create(PhpSymbols.parameter_list, paramList == null ? makeEpsilon() : paramList);
    }


}
