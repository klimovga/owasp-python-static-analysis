package su.msu.cs.lvk.xml2pixy.transform.function;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 20.11.2007
 * Time: 16:44:29
 */

/**
 * Wrapper for declared functions
 */
public class Function extends ASTVisitor {
    protected String currentFile;
    protected String originalName;
    protected String name;
    protected String module;

    protected ParseNode code;
    protected Node source;

    protected String[] arguments;
    protected Map<String, Node> defaults;
    protected Node body;
    protected boolean inline;

    public Function() {
        arguments = new String[0];
        defaults = new HashMap<String, Node>();
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

                    if (defaults.containsKey(param)) {
                        ParseNode rval = makeStaticScalar(defaults.get(param));
                        if (rval != null) {
                            non_empty_parameter_list.addChild(new ParseNode(
                                    PhpSymbols.T_ASSIGN, "T_ASSIGN", currentFile, "=", lineno));
                            non_empty_parameter_list.addChild(makeStaticScalar(defaults.get(param)));
                        }
                    }
                }
                parameter_list.addChild(non_empty_parameter_list);
            }

            this.code.addChild(unticked_declaration_statement);

            // make function declaration
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

    public String[] getArguments() {
        return arguments;
    }

    public Node getBody() {
        return body;
    }

    public void setBody(Node body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public ParseNode getCode() {
        return code;
    }

    public void setCode(ParseNode code) {
        this.code = code;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setArguments(String[] arguments) {
        if (arguments.length == 1) {
            if ("".equals(arguments[0])) arguments = new String[0];
        }
        this.arguments = arguments;
    }


    public String getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public Map<String, Node> getDefaults() {
        return defaults;
    }

    public void setDefaults(Map<String, Node> defaults) {
        this.defaults = defaults;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }
}
