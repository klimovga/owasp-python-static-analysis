package su.msu.cs.lvk.xml2pixy.transform.function;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.classes.PyClass;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 03.12.2007
 * Time: 23:58:45
 */

/**
 * Wrapper for declared constructors as method "ClassName____init__"
 */
public class Constructor extends Method {

    protected PyClass clazz;

    public Constructor(PyClass clazz) {
        super("__init__");
        this.clazz = clazz;
        clazz.setConstructor(this);
    }

    public Constructor(Function function) {
        super(function);
    }

    private boolean generated;

    /**
     * @return true if constructor is explicitly declared
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * @param generated true if constructor is explicitly declared
     */
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public ParseNode render() {
        if (this.code == null) {
            int lineno = getLineno(source.getJdomElement());

            this.code = new ParseNode(
                    PhpSymbols.declaration_statement, "declaration_statement", this.currentFile);
            ParseNode unticked_declaration_statement = new ParseNode(
                    PhpSymbols.unticked_declaration_statement, "unticked_declaration_statement", this.currentFile);

            // return by value
            ParseNode is_reference = new ParseNode(
                    PhpSymbols.is_reference, "is_reference", currentFile);
            is_reference.addChild(makeEpsilon());
            ParseNode parameter_list = new ParseNode(
                    PhpSymbols.parameter_list, "parameter_list", currentFile);

            // make argument list
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
                         if (!param.startsWith("&")) {
                             param = '&' + param;
                         }
                     } else {
                        ParseNode tmp = new ParseNode(
                                PhpSymbols.non_empty_parameter_list, "non_empty_parameter_list", currentFile);
                        tmp.addChild(non_empty_parameter_list);
                        tmp.addChild(new ParseNode(
                                PhpSymbols.T_COMMA, "T_COMMA", currentFile, ",", lineno));
                        non_empty_parameter_list = tmp;
                    }
                    // if arg starts with &, make it reference
                    if (param.startsWith("&")) {
                        non_empty_parameter_list.addChild(new ParseNode(PhpSymbols.T_BITWISE_AND, "T_BITWISE_AND", currentFile, "&", lineno));
                        param = param.substring(1);
                    }
                    non_empty_parameter_list.addChild(new ParseNode(
                            PhpSymbols.T_VARIABLE, "T_VARIABLE", currentFile, "$" + param, lineno));
                }
                parameter_list.addChild(non_empty_parameter_list);
            }

            this.code.addChild(unticked_declaration_statement);

            // render "constructor" declaration
            int tmpLineno = Utils.getLinenoRight(parameter_list, lineno);
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_FUNCTION, "T_FUNCTION", this.currentFile, "function", lineno));
            unticked_declaration_statement.addChild(is_reference);
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_STRING, "T_STRING", this.currentFile, this.name, lineno));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_BRACES, "T_OPEN_BRACES", this.currentFile, "(", lineno));
            unticked_declaration_statement.addChild(parameter_list);
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_BRACES, "T_CLOSE_BRACES", this.currentFile, ")", tmpLineno));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_OPEN_CURLY_BRACES, "T_OPEN_CURLY_BRACES", this.currentFile, "{", tmpLineno));
            unticked_declaration_statement.addChild(top2innerStatement(this.body.getParseNode()));
            unticked_declaration_statement.addChild(new ParseNode(
                    PhpSymbols.T_CLOSE_CURLY_BRACES, "T_CLOSE_CURLY_BRACES", this.currentFile,
                    "}", Utils.getLinenoRight(this.body.getParseNode(), tmpLineno)));

        }

        return this.code;
    }

}
