package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.Symbol;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 *
 */
public class CallFuncVisitor extends ASTVisitor {

    protected static Logger log = Logger.getLogger(CallFuncVisitor.class.getName());

    public CallFuncVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public CallFuncVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        Node callee = getFirstChild(getFirstChild(node, "node"), null);
        Node args = getFirstChild(node, "args");

        boolean ready = true;
        if (callee == null) ready = false;
        if (ready) {
            for (Node child : args.getChildren()) {
                if (child.getParseNode() == null) {
                    ready = false;
                    break;
                }
            }
        }

        if (ready) {
            ParseNode calleeNode = callee.getParseNode();

            if (callee.getJdomElement().getName().equals("Name") ||
                    calleeNode != null && calleeNode.getSymbol() == PhpSymbols.T_STRING) {
                String fullName = getCalleeFullName(callee, module);
                ParseNode firstArg = null;
                if (symbolTable.isClass(fullName)) {
                    fullName = "__init__" + fullName + (args.getChildren().size() + 1);
                    firstArg = makeEmptyArray(currentFile, lineno);
                }
                node.setParseNode(makeFunctionCall(fullName, getArgs(fullName, args, lineno),
                        firstArg, lineno));
            } else {
                if (!callee.getJdomElement().getName().equals("Getattr")) {
                    log.warn("WARNING: Epsilon added in module " + module
                            + " near line " + lineno + " for node " + callee.getJdomElement().getName());
                    node.setParseNode(makeEpsilon());
                    return;
                }

                String fullName = callee.getJdomElement().getAttributeValue("attrname");
                callee = getFirstChild(getFirstChild(callee, "expr"), null);

                ParseNode firstArg = null;

                if (calleeNode == null) {
                    log.warn("WARNING: Unresolved reference to function " + fullName
                            + " found in module " + module + " near line " + lineno);
                    return;
                }

                if ((callee.getJdomElement().getName().equals("Name") ||
                        calleeNode.getSymbol() == PhpSymbols.T_STRING)
                        && symbolTable.isClass(getCalleeFullName(callee, module))) {
                    fullName = getCalleeFullName(callee, module) + "__" + fullName + args.getChildren().size();
                } else if (callee.getJdomElement().getName().equals("Name")) {
                    // $foo["bar"], where foo is a module, bar is a function
                    ParseNodeHelper helper = new ParseNodeHelper();
                    ParseNode moduleNode = helper.traverseFirst(calleeNode,
                            "reference_variable", "compound_variable", "T_VARIABLE");
                    ParseNode funcNode = helper.traverse(calleeNode,
                            2, "dim_offset", 0, "expr", 0, "expr_without_variable",
                            0, "scalar", 0, "common_scalar", 0, "T_CONSTANT_ENCAPSED_STRING");

                    if (moduleNode != null && funcNode != null
                            && symbolTable.isModule(module + "__" + moduleNode.getLexeme().substring(1))) {
                        String localName = module + "__" + moduleNode.getLexeme().substring(1);
                        String qualifiedName = symbolTable.getSymbol(localName).getModule();
                        fullName = qualifiedName // without $
                                + "__" + funcNode.getLexeme().substring(1, funcNode.getLexeme().length() - 1); // without ""
                    } else {
                        fullName = "__method__" + fullName + (args.getChildren().size() + 1);
                        firstArg = makeCvar(callee.getParseNode());
                    }
                } else {
                    fullName = "__method__" + fullName + (args.getChildren().size() + 1);
                    firstArg = makeCvar(callee.getParseNode());
                }
                node.setParseNode(makeFunctionCall(fullName, getArgs(fullName, args, lineno),
                        firstArg, lineno));
            }
        }

    }


    protected ParseNode makeEmptyArray(String currentFile, int lineno) {
        ParseNode expr_without_variable = new ParseNode(
                PhpSymbols.expr_without_variable, "expr_without_variable", currentFile);
        ParseNode array_pair_list = new ParseNode(
                PhpSymbols.array_pair_list, "array_pair_list", currentFile);
        array_pair_list.addChild(makeEpsilon());

        expr_without_variable.addChild(new ParseNode(
                PhpSymbols.T_ARRAY, "T_ARRAY", currentFile, "array", lineno));
        expr_without_variable.addChild(new ParseNode(
                PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", currentFile, "(", lineno));
        expr_without_variable.addChild(array_pair_list);
        expr_without_variable.addChild(new ParseNode(
                PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", currentFile, ")", lineno));


        return expr_without_variable;
    }

    // TODO: this function should be moved to Function or FunctionManager
    protected String getCalleeFullName(Node callee, String module) {
        String fullName;
        if (callee.getParseNode().getSymbol() == PhpSymbols.T_STRING) {
            fullName = callee.getParseNode().getLexeme();
        } else {
            String calleeName = callee.getJdomElement().getAttributeValue("name");
            Symbol symbol = symbolTable.getSymbol(calleeName);
            if (symbol != null) {
                if (symbol.getModule().equals(SymbolTable.BUILTIN_MODULE)) {
                    fullName = "__builtin____" + calleeName;
                } else {
                    fullName = symbol.getModule() + "__" + calleeName;
                }
            } else {
                fullName = module.replaceAll("\\.", "__")
                        + "__" + calleeName;
            }
        }

        return fullName;
    }

}
