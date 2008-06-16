package su.msu.cs.lvk.xml2pixy.transform.function;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.Converter;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.classes.ClassManager;
import su.msu.cs.lvk.xml2pixy.transform.classes.PyClass;

import java.util.*;

/**
 * Wrapper for declared methods.
 */
public class Method extends Function {

    protected static Logger log = Logger.getLogger(Method.class.getName());

    Map<String, Node> registeredClasses;

    public Method(String name) {
        this.registeredClasses = new LinkedHashMap<String, Node>();
        this.originalName = name;
    }

    public Method(Function function) {
        this.registeredClasses = new LinkedHashMap<String, Node>();
        this.originalName = function.originalName;
        this.arguments = function.arguments;
        this.name = FunctionManager.METHOD_PREFIX + this.originalName + arguments.length;
    }

    public void addClass(String name, Node node) {
        this.registeredClasses.put(name, node);
    }

    /**
     * Renders big parse node with a function <code>__method__&lt;name&gt;&lt;argsnum&gt;()</code>.
     * It actually uses arguments, name,
     *
     * @return rendered ParseNode
     */
    public ParseNode render() {
        if (this.code == null) {
            String currentFile = Converter.mainFile;
            int lineno = getLineno(getSource().getJdomElement());

            ParseNode declaration_statement = new ParseNode(
                    PhpSymbols.declaration_statement, "declaration_statement", currentFile);
            ParseNode unticked_declaration_statement = new ParseNode(
                    PhpSymbols.unticked_declaration_statement, "unticked_declaration_statement", currentFile);
            declaration_statement.addChild(unticked_declaration_statement);

            ParseNode is_reference = new ParseNode(
                    PhpSymbols.is_reference, "is_reference", currentFile);
            is_reference.addChild(makeEpsilon());

            ParseNode parameter_list = new ParseNode(
                    PhpSymbols.parameter_list, "parameter_list", currentFile);

            if (arguments.length == 0) {
                parameter_list.addChild(new ParseNode(
                        PhpSymbols.T_EPSILON, "T_EPSILON", currentFile, "epsilon", -2));
            } else {
                ParseNode non_empty_parameter_list = null;
                for (String param : arguments) {
                    if (non_empty_parameter_list == null) {
                        non_empty_parameter_list = new ParseNode(
                                PhpSymbols.non_empty_parameter_list, "non_empty_parameter_list", currentFile);
                        // make first arg reference
/*
                        if (!param.startsWith("&")) {
                            param = '&' + param;
                        }
*/
                    } else {
                        ParseNode tmp = new ParseNode(
                                PhpSymbols.non_empty_parameter_list, "non_empty_parameter_list", currentFile);
                        tmp.addChild(non_empty_parameter_list);
                        tmp.addChild(new ParseNode(
                                PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        non_empty_parameter_list = tmp;
                    }
                    // if arg starts with &, make it reference
                    // always make params by references 
                    non_empty_parameter_list.addChild(new ParseNode(PhpSymbols.T_BITWISE_AND, "T_BITWISE_AND", currentFile, "&", lineno));
                    if (param.startsWith("&")) {
                        param = param.substring(1);
                    }
                    non_empty_parameter_list.addChild(new ParseNode(
                            PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile, "$" + param, lineno));
                }
                parameter_list.addChild(non_empty_parameter_list);
            }

            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_FUNCTION, "T_FUNCTION", currentFile, "function", lineno));
            unticked_declaration_statement.addChild(is_reference);
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_STRING, "T_STRING", currentFile, this.name, lineno));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            unticked_declaration_statement.addChild(parameter_list);
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", lineno));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_CURLY_BRACES, "T_OPEN_CURLY_BRACES", currentFile, "{", lineno));
            unticked_declaration_statement.addChild(renderGlobalIf(currentFile, lineno, arguments[0]));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_CURLY_BRACES, "T_CLOSE_CURLY_BRACES", currentFile, "}", lineno));


            this.code = declaration_statement;
        }

        return this.code;

    }

    private ParseNode renderGlobalIf(String currentFile, int lineno, String arg1) {
        /*
        if (self["__class_name"] == "class1") {
            return class1__meth();
        } else if (self["__class_name"] == "class2") {
            return class2__meth();
        }
         */
        ParseNode inner_statement_list = new ParseNode(
                PhpSymbols.inner_statement_list, "inner_statement_list", currentFile);
        ParseNode tmp = new ParseNode(
                PhpSymbols.inner_statement_list, "inner_statement_list", currentFile);
        ParseNode inner_statement = new ParseNode(
                PhpSymbols.inner_statement, "inner_statement", currentFile);
        tmp.addChild(makeEpsilon());
        inner_statement_list.addChild(tmp);
        inner_statement_list.addChild(inner_statement);
        ParseNode statement = new ParseNode(
                PhpSymbols.statement, "statement", currentFile);
        ParseNode unticked_statement = new ParseNode(
                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
        statement.addChild(unticked_statement);
        inner_statement.addChild(statement);

        Iterator<Map.Entry<String, Node>> iter = registeredClasses.entrySet().iterator();
        Map.Entry<String, Node> first = iter.next();
        ParseNode firstExpr = renderIfExpr(currentFile, lineno, arg1, first.getKey());
        ParseNode firstStmt = makeTopStatementListFromExpr(returnFunctionCall(first.getKey(), currentFile, lineno),
                currentFile, lineno);
        ParseNode elseif_list = new ParseNode(
                PhpSymbols.elseif_list, "elseif_list", currentFile);
        ParseNode else_single = new ParseNode(
                PhpSymbols.else_single, "else_single", currentFile);
        elseif_list.addChild(makeEpsilon());
        else_single.addChild(makeEpsilon()); // there's no single else

        while (iter.hasNext()) {
            Map.Entry<String, Node> elem = iter.next();
            PyClass clazz = ClassManager.getInstance().getPyClass(elem.getKey());
            List<PyClass> classes = new ArrayList<PyClass>();
            classes.add(clazz);
            for (int i = 0; i < classes.size(); i++) {
                int curSize = classes.size();
                try {
                    for (PyClass baseClass : classes.get(i).getBaseClasses()) {
                        classes.add(curSize, baseClass);
                    }
                } catch (NullPointerException e) {
                    log.error("NPE while building method " + this.name, e);
//                    throw e; TODO empty base class
                }
            }
            ParseNode expr = renderIfExpr(currentFile, lineno, arg1, elem.getKey());
            PyClass methodMaster = null;
            for (PyClass pyClass : classes) {
                if (pyClass == null) continue; // TODO empty base class
                if (pyClass.hasOwnMethod(name)) {
                    methodMaster = pyClass;
                    break;
                }
            }
            if (methodMaster == null) continue;
            ParseNode stmt = makeTopStatementListFromExpr(returnFunctionCall(
                    methodMaster.getName(), currentFile, lineno
            ), currentFile, lineno);

            tmp = new ParseNode(
                    PhpSymbols.elseif_list, "elseif_list", currentFile);
            tmp.addChild(elseif_list);
            elseif_list = tmp;

            elseif_list.addChild(new ParseNode(
                    PhpSymbols.T_ELSEIF, "T_ELSEIF", currentFile, "elseif", lineno));
            elseif_list.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
            elseif_list.addChild(expr);
            elseif_list.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", lineno));
            elseif_list.addChild(makeInnerStatementListInBraces(
                    top2innerStatement(stmt), lineno));

        }

        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_IF, "T_IF", currentFile, "if", lineno));
        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
        unticked_statement.addChild(firstExpr); //expr
        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")",
                Utils.getLinenoRight(firstExpr, lineno)));
        unticked_statement.addChild(makeInnerStatementListInBraces(
                top2innerStatement(firstStmt), lineno)); //stmt
        unticked_statement.addChild(elseif_list); //elseif
        unticked_statement.addChild(else_single); //else

        return inner_statement_list;
    }

    private ParseNode returnFunctionCall(String function, String currentFile, int lineno) {
        /*
        return ClassName__method();        
         */
        ParseNode unticked_statement = new ParseNode(
                PhpSymbols.unticked_statement, "unticked_statement", currentFile);
        unticked_statement.addChild(new ParseNode(
                PhpSymbols.T_RETURN, "T_RETURN", currentFile, "return", lineno));
        ParseNode functionCall = renderFunctionCall(function + "__" + originalName + arguments.length,
                arguments, currentFile, lineno);

        if (functionCall.getSymbol() == PhpSymbols.expr) {
            functionCall = functionCall.getChild(0);
        }

        if (functionCall.getSymbol() == PhpSymbols.expr_without_variable) {
            unticked_statement.addChild(functionCall);
            unticked_statement.addChild(new ParseNode(
                    PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));
            return unticked_statement;
        } else {
            return functionCall;
        }

    }

    private ParseNode renderIfExpr(String currentFile, int lineno, String arg1, String className) {
        /*
        self["__class_name__"] == "class1"
         */
        ParseNode expr = new ParseNode(
                PhpSymbols.expr, "expr", currentFile);
        ParseNode expr_without_variable = new ParseNode(
                PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
        expr.addChild(expr_without_variable);

        ParseNode object = new ParseNode(
                PhpSymbols.expr, "expr", currentFile);
        ParseNode objCvar = new ParseNode(
                PhpSymbols.r_cvar, "r_cvar", currentFile);
        object.addChild(objCvar);
        ParseNode cvar = new ParseNode(PhpSymbols.cvar, "cvar", currentFile);
        ParseNode cvar_without_objects = new ParseNode(
                PhpSymbols.cvar_without_objects, "cvar_without_objects", currentFile);
        ParseNode reference_variable = new ParseNode(
                PhpSymbols.reference_variable, "reference_variable", currentFile);
        ParseNode var = new ParseNode(
                PhpSymbols.reference_variable, "reference_variable", currentFile);
        ParseNode compound_variable = new ParseNode(
                PhpSymbols.compound_variable, "compound_variable", currentFile);
        ParseNode dim_offset = new ParseNode(
                PhpSymbols.dim_offset, "dim_offset", currentFile);

        objCvar.addChild(cvar);
        cvar.addChild(cvar_without_objects);
        cvar_without_objects.addChild(reference_variable);
        reference_variable.addChild(var);
        reference_variable.addChild(new ParseNode(
                PhpSymbols.T_OPEN_RECT_BRACES, "T_OPEN_RECT_BRACES", currentFile, "[", lineno));
        reference_variable.addChild(dim_offset);
        reference_variable.addChild(new ParseNode(
                PhpSymbols.T_CLOSE_RECT_BRACES, "T_CLOSE_RECT_BRACES", currentFile, "]", lineno));
        var.addChild(compound_variable);
        compound_variable.addChild(new ParseNode(
                PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile, "$" + arg1, lineno));
        dim_offset.addChild(renderStringConstant(lineno, FunctionManager.TYPE_KEY));

        expr_without_variable.addChild(object);
        expr_without_variable.addChild(new ParseNode(
                PhpSymbols.T_IS_EQUAL, "T_IS_EQUAL", currentFile, "==", lineno));
        expr_without_variable.addChild(renderStringConstant(lineno, className));

        return expr;
    }

    Node findMethodBody(Node clazz, String methodOriginalName) {
        Node body = findMethod(clazz, methodOriginalName);
        if (body != null) {
            return getFirstChild(getFirstChild(body, "code"), null);
        } else {
            return null;
        }
    }

    Node findMethod(Node clazz, String methodOriginalName) {
        Node stmt = getFirstChild(getFirstChild(getFirstChild(clazz, "code"), null), "nodes");
        for (Node child : stmt.getChildren()) {
            if (child.getJdomElement().getName().equals("Function")) {
                if (child.getJdomElement().getAttributeValue("name").equals(methodOriginalName)) {
                    return child;
                }
            }
        }
        return null;
    }

    // TODO replace with ASTVisitor.makeFunctionCall()
    ParseNode renderFunctionCall(String function, String[] argNames, String currentFile, int lineno) {
        ParseNode expr = new ParseNode(
                PhpSymbols.expr, "expr", currentFile);
        ParseNode expr_without_variable = new ParseNode(
                PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
        expr.addChild(expr_without_variable);
        ParseNode function_call = new ParseNode(PhpSymbols.function_call, "function_call", currentFile);
        expr_without_variable.addChild(function_call);

        ParseNode function_call_parameter_list = new ParseNode(
                PhpSymbols.function_call_parameter_list, "function_call_parameter_list", currentFile);
        if (argNames.length == 0) {
            function_call_parameter_list.addChild(makeEpsilon());
        } else {
            ParseNode non_empty_function_call_parameter_list = null;
            for (String argName : argNames) {
                ParseNode tmp = new ParseNode(
                        PhpSymbols.non_empty_function_call_parameter_list,
                        "non_empty_function_call_parameter_list", currentFile);

                if (non_empty_function_call_parameter_list != null) {
                    tmp.addChild(non_empty_function_call_parameter_list);
                    tmp.addChild(new ParseNode(PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                }

                tmp.addChild(
                        makeCvar(makeReferenceVariable(
                                new ParseNode(
                                        PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile,
                                        "$" + argName, lineno
                                )
                        ))
                );
                non_empty_function_call_parameter_list = tmp;
            }
            function_call_parameter_list.addChild(non_empty_function_call_parameter_list);
        }

        function_call.addChild(new ParseNode(
                PhpSymbols.T_STRING, "T_STRING", currentFile, function, lineno));
        function_call.addChild(new ParseNode(
                PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
        function_call.addChild(function_call_parameter_list);
        function_call.addChild(new ParseNode(
                PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", lineno));

        return expr;
    }

    ParseNode makeTopStatementListFromExpr(ParseNode expr, String currentFile, int lineno) {
        /*
        expr -> unticked_statement(expr)
        unticked statement -> statement(unticked_statement)
        statement -> top_statement(statement)
        top_statement -> top_statement_list(top_statement)
         */

        expr = makeExpr(expr);

        if (expr.getSymbol() == PhpSymbols.expr) {
            ParseNode unticked_statement = new ParseNode(PhpSymbols.unticked_statement, "unticked_statement", currentFile);
            unticked_statement.addChild(expr);
            unticked_statement.addChild(new ParseNode(PhpSymbols.T_SEMICOLON, "T_SEMICOLON", currentFile, ";", lineno));
            expr = unticked_statement;
        }
        if (expr.getSymbol() == PhpSymbols.unticked_statement) {
            ParseNode statement = new ParseNode(PhpSymbols.statement, "statement", currentFile);
            statement.addChild(expr);
            expr = statement;
        }

        if (expr.getSymbol() == PhpSymbols.statement) {
            ParseNode epsilon = new ParseNode(PhpSymbols.top_statement_list, "top_statement_list", currentFile);
            epsilon.addChild(makeEpsilon());

            ParseNode top_statement_list = new ParseNode(PhpSymbols.top_statement_list, "top_statement_list", currentFile);
            ParseNode top_statement = new ParseNode(PhpSymbols.top_statement, "top_statement", currentFile);

            top_statement_list.addChild(epsilon);
            top_statement_list.addChild(top_statement);

            top_statement.addChild(expr);
            return top_statement_list;
        } else {
            return expr;
        }
    }
}
