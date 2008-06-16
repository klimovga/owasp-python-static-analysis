package su.msu.cs.lvk.xml2pixy.parser;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Converter;
import su.msu.cs.lvk.xml2pixy.Utils;

import java.util.*;

/**
 * Builder class to get rid of those giant hand-made trees.
 *
 * @author ikonv
 */
public class ParseNodeHelper {
    private static Map<Integer, String> symbolMap = new HashMap<Integer, String>();

    static {
        symbolMap.put(PhpSymbols.T_FILE, "T_FILE");
        symbolMap.put(PhpSymbols.T_IS_GREATER, "T_IS_GREATER");
        symbolMap.put(PhpSymbols.T_SEMICOLON, "T_SEMICOLON");
        symbolMap.put(PhpSymbols.T_CASE, "T_CASE");
        symbolMap.put(PhpSymbols.T_GLOBAL, "T_GLOBAL");
        symbolMap.put(PhpSymbols.T_DNUMBER, "T_DNUMBER");
        symbolMap.put(PhpSymbols.T_ARRAY, "T_ARRAY");
        symbolMap.put(PhpSymbols.T_SINGLE_QUOTE, "T_SINGLE_QUOTE");
        symbolMap.put(PhpSymbols.T_CLASS_C, "T_CLASS_C");
        symbolMap.put(PhpSymbols.T_PAAMAYIM_NEKUDOTAYIM, "T_PAAMAYIM_NEKUDOTAYIM");
        symbolMap.put(PhpSymbols.T_EXTENDS, "T_EXTENDS");
        symbolMap.put(PhpSymbols.T_BACKTICK, "T_BACKTICK");
        symbolMap.put(PhpSymbols.T_USE, "T_USE");
        symbolMap.put(PhpSymbols.T_MINUS_EQUAL, "T_MINUS_EQUAL");
        symbolMap.put(PhpSymbols.T_INT_CAST, "T_INT_CAST");
        symbolMap.put(PhpSymbols.T_INCLUDE, "T_INCLUDE");
        symbolMap.put(PhpSymbols.T_BOOLEAN_OR, "T_BOOLEAN_OR");
        symbolMap.put(PhpSymbols.T_EMPTY, "T_EMPTY");
        symbolMap.put(PhpSymbols.T_XOR_EQUAL, "T_XOR_EQUAL");
        symbolMap.put(PhpSymbols.T_END_HEREDOC, "T_END_HEREDOC");
        symbolMap.put(PhpSymbols.T_CLASS, "T_CLASS");
        symbolMap.put(PhpSymbols.T_FOR, "T_FOR");
        symbolMap.put(PhpSymbols.T_STRING, "T_STRING");
        symbolMap.put(PhpSymbols.T_DOUBLE_QUOTE, "T_DOUBLE_QUOTE");
        symbolMap.put(PhpSymbols.T_START_HEREDOC, "T_START_HEREDOC");
        symbolMap.put(PhpSymbols.T_DIV, "T_DIV");
        symbolMap.put(PhpSymbols.T_AT, "T_AT");
        symbolMap.put(PhpSymbols.T_AS, "T_AS");
        symbolMap.put(PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES");
        symbolMap.put(PhpSymbols.T_STRING_CAST, "T_STRING_CAST");
        symbolMap.put(PhpSymbols.T_STATIC, "T_STATIC");
        symbolMap.put(PhpSymbols.T_WHILE, "T_WHILE");
        symbolMap.put(PhpSymbols.T_SR, "T_SR");
        symbolMap.put(PhpSymbols.T_ENDFOREACH, "T_ENDFOREACH");
        symbolMap.put(PhpSymbols.T_FUNC_C, "T_FUNC_C");
        symbolMap.put(PhpSymbols.T_EVAL, "T_EVAL");
        symbolMap.put(PhpSymbols.T_CLOSE_CURLY_BRACES, "T_CLOSE_CURLY_BRACES");
        symbolMap.put(PhpSymbols.T_SL, "T_SL");
        symbolMap.put(PhpSymbols.T_INC, "T_INC");
        symbolMap.put(PhpSymbols.T_ENDWHILE, "T_ENDWHILE");
        symbolMap.put(PhpSymbols.T_QUESTION, "T_QUESTION");
        symbolMap.put(PhpSymbols.T_BOOLEAN_AND, "T_BOOLEAN_AND");
        symbolMap.put(PhpSymbols.T_STRING_VARNAME, "T_STRING_VARNAME");
        symbolMap.put(PhpSymbols.T_DIV_EQUAL, "T_DIV_EQUAL");
        symbolMap.put(PhpSymbols.T_BREAK, "T_BREAK");
        symbolMap.put(PhpSymbols.T_POINT, "T_POINT");
        symbolMap.put(PhpSymbols.T_IS_SMALLER, "T_IS_SMALLER");
        symbolMap.put(PhpSymbols.T_AND_EQUAL, "T_AND_EQUAL");
        symbolMap.put(PhpSymbols.T_DEFAULT, "T_DEFAULT");
        symbolMap.put(PhpSymbols.T_VARIABLE, "T_VARIABLE");
        symbolMap.put(PhpSymbols.T_SR_EQUAL, "T_SR_EQUAL");
        symbolMap.put(PhpSymbols.T_SL_EQUAL, "T_SL_EQUAL");
        symbolMap.put(PhpSymbols.T_PRINT, "T_PRINT");
        symbolMap.put(PhpSymbols.T_CURLY_OPEN, "T_CURLY_OPEN");
        symbolMap.put(PhpSymbols.T_ENDIF, "T_ENDIF");
        symbolMap.put(PhpSymbols.T_BITWISE_AND, "T_BITWISE_AND");
        symbolMap.put(PhpSymbols.T_EPSILON, "T_EPSILON");
        symbolMap.put(PhpSymbols.T_ELSEIF, "T_ELSEIF");
        symbolMap.put(PhpSymbols.T_MINUS, "T_MINUS");
        symbolMap.put(PhpSymbols.T_IS_EQUAL, "T_IS_EQUAL");
        symbolMap.put(PhpSymbols.T_INCLUDE_ONCE, "T_INCLUDE_ONCE");
        symbolMap.put(PhpSymbols.T_UNSET_CAST, "T_UNSET_CAST");
        symbolMap.put(PhpSymbols.T_OLD_FUNCTION, "T_OLD_FUNCTION");
        symbolMap.put(PhpSymbols.T_BITWISE_XOR, "T_BITWISE_XOR");
        symbolMap.put(PhpSymbols.T_BAD_CHARACTER, "T_BAD_CHARACTER");
        symbolMap.put(PhpSymbols.T_OBJECT_CAST, "T_OBJECT_CAST");
        symbolMap.put(PhpSymbols.T_OR_EQUAL, "T_OR_EQUAL");
        symbolMap.put(PhpSymbols.T_INLINE_HTML, "T_INLINE_HTML");
        symbolMap.put(PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES");
        symbolMap.put(PhpSymbols.T_NEW, "T_NEW");
        symbolMap.put(PhpSymbols.T_UNSET, "T_UNSET");
        symbolMap.put(PhpSymbols.T_MOD_EQUAL, "T_MOD_EQUAL");
        symbolMap.put(PhpSymbols.T_DOLLAR, "T_DOLLAR");
        symbolMap.put(PhpSymbols.T_ENDSWITCH, "T_ENDSWITCH");
        symbolMap.put(PhpSymbols.T_FOREACH, "T_FOREACH");
        symbolMap.put(PhpSymbols.T_COLON, "T_COLON");
        symbolMap.put(PhpSymbols.EOF, "EOF");
        symbolMap.put(PhpSymbols.T_ENDFOR, "T_ENDFOR");
        symbolMap.put(PhpSymbols.T_NUM_STRING, "T_NUM_STRING");
        symbolMap.put(PhpSymbols.T_PLUS, "T_PLUS");
        symbolMap.put(PhpSymbols.T_REQUIRE_ONCE, "T_REQUIRE_ONCE");
        symbolMap.put(PhpSymbols.T_IS_SMALLER_OR_EQUAL, "T_IS_SMALLER_OR_EQUAL");
        symbolMap.put(PhpSymbols.T_FUNCTION, "T_FUNCTION");
        symbolMap.put(PhpSymbols.T_LNUMBER, "T_LNUMBER");
        symbolMap.put(PhpSymbols.T_BITWISE_OR, "T_BITWISE_OR");
        symbolMap.put(PhpSymbols.T_ASSIGN, "T_ASSIGN");
        symbolMap.put(PhpSymbols.T_IS_NOT_EQUAL, "T_IS_NOT_EQUAL");
        symbolMap.put(PhpSymbols.T_ENDDECLARE, "T_ENDDECLARE");
        symbolMap.put(PhpSymbols.T_MULT, "T_MULT");
        symbolMap.put(PhpSymbols.T_MODULO, "T_MODULO");
        symbolMap.put(PhpSymbols.T_PLUS_EQUAL, "T_PLUS_EQUAL");
        symbolMap.put(PhpSymbols.error, "error");
        symbolMap.put(PhpSymbols.T_ELSE, "T_ELSE");
        symbolMap.put(PhpSymbols.T_DO, "T_DO");
        symbolMap.put(PhpSymbols.T_CONTINUE, "T_CONTINUE");
        symbolMap.put(PhpSymbols.T_DOUBLE_ARROW, "T_DOUBLE_ARROW");
        symbolMap.put(PhpSymbols.T_ECHO, "T_ECHO");
        symbolMap.put(PhpSymbols.T_OPEN_RECT_BRACES, "T_OPEN_RECT_BRACES");
        symbolMap.put(PhpSymbols.T_IS_IDENTICAL, "T_IS_IDENTICAL");
        symbolMap.put(PhpSymbols.T_CHARACTER, "T_CHARACTER");
        symbolMap.put(PhpSymbols.T_BITWISE_NOT, "T_BITWISE_NOT");
        symbolMap.put(PhpSymbols.T_REQUIRE, "T_REQUIRE");
        symbolMap.put(PhpSymbols.T_ARRAY_CAST, "T_ARRAY_CAST");
        symbolMap.put(PhpSymbols.T_CONSTANT_ENCAPSED_STRING, "T_CONSTANT_ENCAPSED_STRING");
        symbolMap.put(PhpSymbols.T_SWITCH, "T_SWITCH");
        symbolMap.put(PhpSymbols.T_ENCAPSED_AND_WHITESPACE, "T_ENCAPSED_AND_WHITESPACE");
        symbolMap.put(PhpSymbols.T_LINE, "T_LINE");
        symbolMap.put(PhpSymbols.T_DOUBLE_CAST, "T_DOUBLE_CAST");
        symbolMap.put(PhpSymbols.T_BOOL_CAST, "T_BOOL_CAST");
        symbolMap.put(PhpSymbols.T_CONST, "T_CONST");
        symbolMap.put(PhpSymbols.T_RETURN, "T_RETURN");
        symbolMap.put(PhpSymbols.T_IS_NOT_IDENTICAL, "T_IS_NOT_IDENTICAL");
        symbolMap.put(PhpSymbols.T_OPEN_CURLY_BRACES, "T_OPEN_CURLY_BRACES");
        symbolMap.put(PhpSymbols.T_LOGICAL_AND, "T_LOGICAL_AND");
        symbolMap.put(PhpSymbols.T_IS_GREATER_OR_EQUAL, "T_IS_GREATER_OR_EQUAL");
        symbolMap.put(PhpSymbols.T_DOLLAR_OPEN_CURLY_BRACES, "T_DOLLAR_OPEN_CURLY_BRACES");
        symbolMap.put(PhpSymbols.T_EXIT, "T_EXIT");
        symbolMap.put(PhpSymbols.T_CLOSE_RECT_BRACES, "T_CLOSE_RECT_BRACES");
        symbolMap.put(PhpSymbols.T_LOGICAL_OR, "T_LOGICAL_OR");
        symbolMap.put(PhpSymbols.T_NOT, "T_NOT");
        symbolMap.put(PhpSymbols.T_ISSET, "T_ISSET");
        symbolMap.put(PhpSymbols.T_LOGICAL_XOR, "T_LOGICAL_XOR");
        symbolMap.put(PhpSymbols.T_CONCAT_EQUAL, "T_CONCAT_EQUAL");
        symbolMap.put(PhpSymbols.T_LIST, "T_LIST");
        symbolMap.put(PhpSymbols.T_COMMA, "T_COMMA");
        symbolMap.put(PhpSymbols.T_DEC, "T_DEC");
        symbolMap.put(PhpSymbols.T_VAR, "T_VAR");
        symbolMap.put(PhpSymbols.T_MUL_EQUAL, "T_MUL_EQUAL");
        symbolMap.put(PhpSymbols.T_OBJECT_OPERATOR, "T_OBJECT_OPERATOR");
        symbolMap.put(PhpSymbols.T_DECLARE, "T_DECLARE");
        symbolMap.put(PhpSymbols.T_IF, "T_IF");
        symbolMap.put(PhpSymbols.function_call_parameter_list, "function_call_parameter_list");
        symbolMap.put(PhpSymbols.for_statement, "for_statement");
        symbolMap.put(PhpSymbols.is_reference, "is_reference");
        symbolMap.put(PhpSymbols.encaps_list, "encaps_list");
        symbolMap.put(PhpSymbols.object_property, "object_property");
        symbolMap.put(PhpSymbols.new_else_single, "new_else_single");
        symbolMap.put(PhpSymbols.encaps_var, "encaps_var");
        symbolMap.put(PhpSymbols.parameter_list, "parameter_list");
        symbolMap.put(PhpSymbols.inner_statement, "inner_statement");
        symbolMap.put(PhpSymbols.scalar, "scalar");
        symbolMap.put(PhpSymbols.expr, "expr");
        symbolMap.put(PhpSymbols.class_variable_decleration, "class_variable_decleration");
        symbolMap.put(PhpSymbols.non_empty_parameter_list, "non_empty_parameter_list");
        symbolMap.put(PhpSymbols.declaration_statement, "declaration_statement");
        symbolMap.put(PhpSymbols.dim_offset, "dim_offset");
        symbolMap.put(PhpSymbols.unset_variable, "unset_variable");
        symbolMap.put(PhpSymbols.w_cvar, "w_cvar");
        symbolMap.put(PhpSymbols.case_list, "case_list");
        symbolMap.put(PhpSymbols.switch_case_list, "switch_case_list");
        symbolMap.put(PhpSymbols.case_separator, "case_separator");
        symbolMap.put(PhpSymbols.function_call, "function_call");
        symbolMap.put(PhpSymbols.inner_statement_list, "inner_statement_list");
        symbolMap.put(PhpSymbols.top_statement_list, "top_statement_list");
        symbolMap.put(PhpSymbols.static_array_pair_list, "static_array_pair_list");
        symbolMap.put(PhpSymbols.possible_comma, "possible_comma");
        symbolMap.put(PhpSymbols.static_var_list, "static_var_list");
        symbolMap.put(PhpSymbols.reference_variable, "reference_variable");
        symbolMap.put(PhpSymbols.non_empty_static_array_pair_list, "non_empty_static_array_pair_list");
        symbolMap.put(PhpSymbols.static_or_variable_string, "static_or_variable_string");
        symbolMap.put(PhpSymbols.r_cvar, "r_cvar");
        symbolMap.put(PhpSymbols.class_statement_list, "class_statement_list");
        symbolMap.put(PhpSymbols.ref_list, "ref_list");
        symbolMap.put(PhpSymbols.internal_functions_in_yacc, "internal_functions_in_yacc");
        symbolMap.put(PhpSymbols.static_scalar, "static_scalar");
        symbolMap.put(PhpSymbols.isset_variables, "isset_variables");
        symbolMap.put(PhpSymbols.simple_indirect_reference, "simple_indirect_reference");
        symbolMap.put(PhpSymbols.echo_expr_list, "echo_expr_list");
        symbolMap.put(PhpSymbols.declare_statement, "declare_statement");
        symbolMap.put(PhpSymbols.object_dim_list, "object_dim_list");
        symbolMap.put(PhpSymbols.cvar, "cvar");
        symbolMap.put(PhpSymbols.elseif_list, "elseif_list");
        symbolMap.put(PhpSymbols.S, "S");
        symbolMap.put(PhpSymbols.unset_variables, "unset_variables");
        symbolMap.put(PhpSymbols.use_filename, "use_filename");
        symbolMap.put(PhpSymbols.global_var_list, "global_var_list");
        symbolMap.put(PhpSymbols.global_var, "global_var");
        symbolMap.put(PhpSymbols.common_scalar, "common_scalar");
        symbolMap.put(PhpSymbols.while_statement, "while_statement");
        symbolMap.put(PhpSymbols.unticked_statement, "unticked_statement");
        symbolMap.put(PhpSymbols.assignment_list_element, "assignment_list_element");
        symbolMap.put(PhpSymbols.for_expr, "for_expr");
        symbolMap.put(PhpSymbols.non_empty_function_call_parameter_list, "non_empty_function_call_parameter_list");
        symbolMap.put(PhpSymbols.class_statement, "class_statement");
        symbolMap.put(PhpSymbols.assignment_list, "assignment_list");
        symbolMap.put(PhpSymbols.compound_variable, "compound_variable");
        symbolMap.put(PhpSymbols.non_empty_array_pair_list, "non_empty_array_pair_list");
        symbolMap.put(PhpSymbols.non_empty_for_expr, "non_empty_for_expr");
        symbolMap.put(PhpSymbols.new_elseif_list, "new_elseif_list");
        symbolMap.put(PhpSymbols.cvar_without_objects, "cvar_without_objects");
        symbolMap.put(PhpSymbols.else_single, "else_single");
        symbolMap.put(PhpSymbols.declare_list, "declare_list");
        symbolMap.put(PhpSymbols.encaps_var_offset, "encaps_var_offset");
        symbolMap.put(PhpSymbols.exit_expr, "exit_expr");
        symbolMap.put(PhpSymbols.rw_cvar, "rw_cvar");
        symbolMap.put(PhpSymbols.expr_without_variable, "expr_without_variable");
        symbolMap.put(PhpSymbols.array_pair_list, "array_pair_list");
        symbolMap.put(PhpSymbols.statement, "statement");
        symbolMap.put(PhpSymbols.variable_name, "variable_name");
        symbolMap.put(PhpSymbols.foreach_optional_arg, "foreach_optional_arg");
        symbolMap.put(PhpSymbols.top_statement, "top_statement");
        symbolMap.put(PhpSymbols.ctor_arguments, "ctor_arguments");
        symbolMap.put(PhpSymbols.foreach_statement, "foreach_statement");
        symbolMap.put(PhpSymbols.unticked_declaration_statement, "unticked_declaration_statement");
    }

    private String currentFile;

    /**
     * Creates ParseNode by symbol code using current set filepath.
     * @param symbol php symbol code to create
     * @return created node
     */
    public ParseNode create(int symbol) {
        return new ParseNode(symbol, symbolMap.get(symbol), getCurrentFile());
    }

    /**
     * Creates ParseNode by symbol code using current set filepath and adds one child to it.
     * @param symbol php symbol code to create
     * @param child child node
     * @return created node
     */
    public ParseNode create(int symbol, ParseNode child) {
        ParseNode node = new ParseNode(symbol, symbolMap.get(symbol), getCurrentFile());
        if (child != null) {
            node.addChild(child);
        }

        return node;
    }

    /**
     * Creates ParseNode by symbol code using current set filepath and adds some children to it.
     * @param symbol php symbol code to create
     * @param children children nodes
     * @return created node
     */
    public ParseNode create(int symbol, ParseNode... children) {
        ParseNode node = new ParseNode(symbol, symbolMap.get(symbol), getCurrentFile());
        for (ParseNode c : children) {
            if (c != null) node.addChild(c);
        }

        return node;
    }

    /**
     * Creates token node by symbol code, lexeme and line number using current filepath.
     * @param symbol symbol code
     * @param value lexeme
     * @param lineno line number
     * @return created token node
     */
    public ParseNode create(int symbol, String value, int lineno) {
        return new ParseNode(symbol, symbolMap.get(symbol), getCurrentFile(), value, lineno);
    }

    public NodeBinding addChild(ParseNode parent, ParseNode child) {
        parent.addChild(child);

        return new NodeBinding(parent);
    }

    /**
     * Makes linked list of ParseNodes corresponding to given array of symbol codes. The deepest
     * element is <code>leaf</code>
     * I.e. <code>createChain(new int[]{PhpSymbols.scalar, PhpSymbols.common_scalar}, create(PhpSymbols.T_STRING, "a", 1))</code
     * @param symbols array of symbol codes
     * @param leaf the deepest element
     * @return root of created list
     */
    public ParseNode createChain(int[] symbols, ParseNode leaf) {
        ParseNode top = leaf;
        for (int i = symbols.length - 1; i >= 0; --i) {
            ParseNode parent = create(symbols[i]);
            if (top != null) {
                parent.addChild(top);
            }

            top = parent;
        }

        return top;
    }

    /**
     * Checks if all children node symbold are correct using given php symbol codes
     * @param node root node
     * @param types PhpSymbol codes
     * @return true if all children are correct, false otherwise
     */
    public static boolean childrenMatch(ParseNode node, int... types) {
        if (node.getNumChildren() == types.length) {
            for (int i = 0; i < types.length; ++i) {
                if (node.getChild(i).getSymbol() != types[i]) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Traverse by first children checking node types.
     *
     * @param root root parse node
     * @param names string values of php grammar symbols
     * @return node, corresponding the last <code>names</code> argument
     */
    public ParseNode traverseFirst(ParseNode root, String... names) {
        ParseNode node = root;
        for (String name : names) {
            if (node.getNumChildren() == 0) {
                return null;
            }

            ParseNode first = node.getChild(0);
            if (name.equals(first.getName())) {
                node = first;
            } else {
                return null;
            }
        }

        return node;
    }

    /**
     * Traverse by given children, checking node types.
     * @param root root parse node
     * @param args sequence of index1, name1, index2, name2, ...,
     *   where index is an integer index in tree and name is a name of node
     * @return parse node if reachable
     */
    public ParseNode traverse(ParseNode root, Object... args) {
        List<Integer> indices = new ArrayList<Integer>();
        List<String> names = new ArrayList<String>();

        int num = 1;
        for (Object o : args) {
            if (num % 2 != 0) {
                if (!(o instanceof Integer)) {
                    throw new IllegalArgumentException("Odd arguments must be integer indices");
                }
                Integer index = (Integer) o;
                indices.add(index);
            } else {
                if (!(o instanceof String)) {
                    throw new IllegalArgumentException("Even arguments must be string names");
                }
                String name = (String) o;
                names.add(name);
            }

            ++num;
        }

        if (indices.size() != names.size()) {
            throw new IllegalArgumentException("Different number of indices and names: "
                    + indices.size() + " and " + names.size());
        }

        ParseNode node = root;
        for (int i = 0; i < names.size(); ++i) {
            String name = names.get(i);
            int index = indices.get(i);

            if (node.getNumChildren() < index) {
                return null;
            }

            ParseNode child = node.getChild(index);
            if (name.equals(child.getName())) {
                node = child;
            } else {
                return null;
            }
        }

        return node;
    }

    public ParseNode deepCopy(ParseNode root, Map<String, String> renamings) {
        ParseNode copy;
        if (root.isToken()) {
            if (root.getSymbol() == PhpSymbols.T_VARIABLE && renamings.containsKey(root.getLexeme())) {
                copy = new ParseNode(root.getSymbol(), symbolMap.get(root.getSymbol()),
                        root.getFileName(), renamings.get(root.getLexeme()), root.getLineno());
            } else {
                copy = new ParseNode(root.getSymbol(), symbolMap.get(root.getSymbol()),
                        root.getFileName(), root.getLexeme(), root.getLineno());
            }
        } else {
            copy = new ParseNode(root.getSymbol(), symbolMap.get(root.getSymbol()),
                    root.getFileName());

            for (int i = 0; i < root.getNumChildren(); i++) {
                copy.addChild(deepCopy(root.getChild(i), renamings));
            }
        }

        return copy;
    }

    public List<ParseNode> unpackStatementsFromList(ParseNode list) {
        List<ParseNode> statements = new ArrayList<ParseNode>();
        while (list.getSymbol() != PhpSymbols.T_EPSILON) {
            if (list.getNumChildren() > 1) {
                statements.add(list.getChild(1).getChild(0));
            }
            list = list.getChild(0);
        }

        Collections.reverse(statements);

        return statements;
    }

    public String getCurrentFile() {
        return Utils.isBlank(currentFile) ? Converter.mainFile : currentFile.trim();
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }
}
