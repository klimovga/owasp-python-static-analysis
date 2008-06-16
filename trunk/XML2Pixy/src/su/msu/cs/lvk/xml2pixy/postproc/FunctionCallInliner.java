package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.transform.function.FunctionManager;
import su.msu.cs.lvk.xml2pixy.transform.function.Function;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;

import java.util.*;

class FunctionParameter {
    private String name;
    private boolean isReference;

    public FunctionParameter(String name, boolean reference) {
        this.name = name;
        isReference = reference;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setReference(boolean reference) {
        isReference = reference;
    }
}

/**
 * Inline functions that should be inlined.
 *
 * @author ikonv
 */
public class FunctionCallInliner implements NodeTransformer {
    private TemporaryVars temporaryVars;

    public FunctionCallInliner(TemporaryVars temporaryVars) {
        this.temporaryVars = temporaryVars;
    }

    public Object createArgIfApplicable(ParseNode node) {
        if (node.getSymbol() == PhpSymbols.function_call) {
            String name = node.getChild(0).getLexeme();
            Function func = FunctionManager.getInstance().getFunction(name);
            if (name.equals("fun10__inline_me") || func != null && func.isInline()) {
                return node;
            }
        }
        
        return null;
    }

    public void apply(Object arg) {
        ParseNode callNode = (ParseNode) arg;
        // replace function manager by function body
        Function func = FunctionManager.getInstance().getFunction(callNode.getChild(0).getLexeme());

        ParseNode statement = TransformerHelper.findParentStatement(callNode);

        ParseNode declaration = func.getCode();
        List<FunctionParameter> formal = extractFormalParameters(declaration);
        List<ParseNode> actual = extractActualParameters(callNode);

        if (actual.size() != formal.size()) {
            throw new RuntimeException("Lists of actual and formal parameters have different lengths");
        }

        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(callNode.getFileName());

        ParseNode body = declaration.getChild(0).getChild(7);
        // create assignments formal_param = actual param
        Map<String, String> renamings = createParamAssignments(formal, actual, statement, helper, body);
        createVarRenamings(body, renamings);

        ParseNode bodyCopy = helper.deepCopy(body, renamings);
        String resultVar = insertBodyBefore(helper, statement, bodyCopy);

        ParseNode parent = callNode.getParent();
        ParseNode replacement = helper.createChain(new int[] {
                    PhpSymbols.cvar, PhpSymbols.cvar_without_objects,
                    PhpSymbols.reference_variable, PhpSymbols.compound_variable },
                    helper.create(PhpSymbols.T_VARIABLE, resultVar, -1));
        replaceCallByVar(parent, callNode, replacement, helper);
    }

    private void replaceCallByVar(ParseNode parent, ParseNode callNode, ParseNode replacement, ParseNodeHelper helper) {
        if (parent.getParent().getSymbol() == PhpSymbols.expr) {
            replacement = helper.create(PhpSymbols.r_cvar, replacement);
            callNode = parent;
            parent = parent.getParent();
        }

        if (parent.getParent().getSymbol() == PhpSymbols.unticked_statement
                && parent.getParent().getChild(0).getSymbol() == PhpSymbols.T_RETURN) {
            callNode = parent;
            parent = parent.getParent();
        }

        for (int i = 0; i < parent.getNumChildren(); i++) {
            if (parent.getChild(i) == callNode) {
                parent.getChildren().set(i, replacement);
                replacement.setParent(parent);
            }
        }
    }

    private boolean isVariableAffected(ParseNodeHelper helper, ParseNode node, String var) {
        if (node.getSymbol() == PhpSymbols.expr_without_variable
                && node.getNumChildren() == 3 && node.getChild(1).getSymbol() == PhpSymbols.T_ASSIGN) {
            ParseNode cvar = node.getChild(0);
            ParseNode tVar = helper.traverseFirst(cvar, "cvar_without_objects",
                    "reference_variable", "compound_variable", "T_VARIABLE");
            if (tVar.equals(var)) {
                return true;
            } else {
                return false;
            }
        } else {
            for (Object c : node.getChildren()) {
                ParseNode child = (ParseNode) c;
                if (isVariableAffected(helper, child, var)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String insertBodyBefore(ParseNodeHelper helper, ParseNode statement, ParseNode body) {
        String resultVar = temporaryVars.createFunctionRes();
        List<ParseNode> statements = helper.unpackStatementsFromList(body);
        for (int i = 0; i < statements.size(); ++i) {
            ParseNode s = statements.get(i);
            ParseNode unticked = s.getChild(0);

            boolean skip = false;

            if (s.getNumChildren() > 0 && unticked.getChild(0).getSymbol() == PhpSymbols.T_RETURN) {
                if (i != statements.size() - 1) {
                    throw new RuntimeException("Functions with the last 'return' statement are supported only :(");
                }

                if (unticked.getNumChildren() == 1) {
                    skip = true;
                }

                // replace return statement by assignment to the result
                ParseNode cvar = helper.createChain(new int[] { PhpSymbols.cvar,
                        PhpSymbols.cvar_without_objects, PhpSymbols.reference_variable, PhpSymbols.compound_variable },
                        helper.create(PhpSymbols.T_VARIABLE, resultVar, -1));
                ParseNode rhs = unticked.getChild(1);
                if (rhs.getSymbol() == PhpSymbols.cvar) {
                    rhs = helper.create(PhpSymbols.r_cvar, rhs);
                }

                ParseNode expr = helper.create(PhpSymbols.expr,
                        helper.create(PhpSymbols.expr_without_variable,
                                cvar,
                                helper.create(PhpSymbols.T_ASSIGN, "=", -1),
                                helper.create(PhpSymbols.expr, rhs)));
                s = helper.create(PhpSymbols.statement, helper.create(PhpSymbols.unticked_statement,
                        expr, helper.create(PhpSymbols.T_SEMICOLON, ";", -1)));
            }

            if (!skip) {
                TransformerHelper.insertStatementBefore(statement, s);
            }
        }

        return resultVar;
    }

    private Map<String, String> createParamAssignments(List<FunctionParameter> formal, List<ParseNode> actual,
                                                       ParseNode statement, ParseNodeHelper helper, ParseNode funcBody) {
        Map<String, String> renaming = new HashMap<String, String>();

        for (int i = 0; i < formal.size(); i++) {
            ParseNode actualParam = actual.get(i);
            FunctionParameter formalParam = formal.get(i);

            String replacementVar;
            boolean useActualParam = false;
            if (actualParam.getSymbol() == PhpSymbols.cvar && !formalParam.isReference()) {
                ParseNode tVar = helper.traverseFirst(actualParam, "cvar_without_objects",
                        "reference_variable", "compound_variable", "T_VARIABLE");
                // safety check (no assignments to formal param in the body)
                if (!isVariableAffected(helper, funcBody, tVar.getLexeme())) {
                    useActualParam = true;
                    replacementVar = tVar.getLexeme();
                } else {
                    replacementVar = temporaryVars.createFunctionArg();
                }
            } else {
                replacementVar = temporaryVars.createFunctionArg();
            }

            renaming.put(formalParam.getName(), replacementVar);

            ParseNode lhs = helper.createChain(new int[] { PhpSymbols.cvar,
                    PhpSymbols.cvar_without_objects, PhpSymbols.reference_variable, PhpSymbols.compound_variable },
                    helper.create(PhpSymbols.T_VARIABLE, replacementVar, -1));

            ParseNode expr_wo_var = null;
            if (actualParam.getSymbol() == PhpSymbols.cvar) {
                if (formalParam.isReference()) {
                    ParseNode w_cvar = helper.create(PhpSymbols.w_cvar, actualParam);
                    expr_wo_var = helper.create(PhpSymbols.expr_without_variable,
                            lhs, helper.create(PhpSymbols.T_ASSIGN, "=", -1),
                            helper.create(PhpSymbols.T_BITWISE_AND, "&", -1),
                            w_cvar);
                } else if (!useActualParam) {
                    ParseNode r_cvar = helper.create(PhpSymbols.r_cvar, actualParam);
                    expr_wo_var = helper.create(PhpSymbols.expr_without_variable,
                            lhs, helper.create(PhpSymbols.T_ASSIGN, "=", -1),
                            helper.create(PhpSymbols.expr, r_cvar));
                }
            } else {
                expr_wo_var = helper.create(PhpSymbols.expr_without_variable,
                        lhs, helper.create(PhpSymbols.T_ASSIGN, "=", -1),
                        helper.create(PhpSymbols.expr, actualParam));
            }

            if (expr_wo_var != null) {
                TransformerHelper.insertExprAsStatementBefore(statement, helper.create(PhpSymbols.expr, expr_wo_var));
            }
        }

        return renaming;
    }

    private void createVarRenamings(ParseNode node, Map<String, String> renamings) {
        if (node.getSymbol() == PhpSymbols.T_VARIABLE) {
            String name = node.getLexeme();
            if (!renamings.containsKey(name)) {
                renamings.put(name, temporaryVars.createFunctionVar());
            }
        } else {
            for (int i = 0; i < node.getNumChildren(); i++) {
                createVarRenamings(node.getChild(i), renamings);
            }
        }
    }

    private List<ParseNode> extractActualParameters(ParseNode call) {
        List<ParseNode> params = new ArrayList<ParseNode>();
        ParseNode parameterList = call.getChild(2).getChild(0);
        while (parameterList != null && parameterList.getNumChildren() > 0
                && parameterList.getChild(0).getSymbol() != PhpSymbols.T_EPSILON) {
            ParseNode newList;
            int index = 0;
            if (parameterList.getChild(0).getSymbol() == PhpSymbols.non_empty_function_call_parameter_list) {
                newList = parameterList.getChild(0);
                index = 2; // one for list and one for comma
            } else {
                newList = null;
            }

            if (parameterList.getChild(index).getSymbol() == PhpSymbols.T_BITWISE_AND) {
                throw new IllegalArgumentException("Call time pass by reference is not supported");
            }

            params.add(parameterList.getChild(index));
            parameterList = newList;
        }

        Collections.reverse(params);
        return params;
    }

    private List<FunctionParameter> extractFormalParameters(ParseNode declaration) {
        List<FunctionParameter> params = new ArrayList<FunctionParameter>();

        ParseNode parameterList = declaration.getChild(0).getChild(4).getChild(0);
        while (parameterList != null && parameterList.getNumChildren() > 0
                && parameterList.getChild(0).getSymbol() != PhpSymbols.T_EPSILON) {
            ParseNode newList;
            int index = 0;
            if (parameterList.getChild(0).getSymbol() == PhpSymbols.non_empty_parameter_list) {
                newList = parameterList.getChild(0);
                index = 2; // one for list and for comma
            } else {
                newList = null;
            }

            boolean isReference = false;
            if (parameterList.getChild(index).getSymbol() == PhpSymbols.T_BITWISE_AND) {
                isReference = true;
                index++;
            }

            if (parameterList.getChild(index).getSymbol() == PhpSymbols.T_CONST) {
                throw new IllegalArgumentException("Constant prefix is not supported");
            }

            if (parameterList.getChild(index).getSymbol() != PhpSymbols.T_VARIABLE) {
                throw new RuntimeException("T_VARIABLE expected");
            }

            String variable = parameterList.getChild(index).getLexeme();
            params.add(new FunctionParameter(variable, isReference));

            parameterList = newList;
        }

        Collections.reverse(params);
        return params;
    }
}
