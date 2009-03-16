package su.msu.cs.lvk.xml2pixy.ast.python;

import org.jdom.Element;
import su.msu.cs.lvk.xml2pixy.Utils;
import su.msu.cs.lvk.xml2pixy.ast.ASTNode;
import su.msu.cs.lvk.xml2pixy.ast.ListBuilder;
import su.msu.cs.lvk.xml2pixy.transform.Node;

import java.io.PrintStream;
import java.util.*;

/**
 * User: KlimovGA
 * Date: 25.09.2008
 * Time: 23:54:03
 */
public class FunctionNode extends ScopeNode {

    public static final String EVAL = "eval";

    protected String kwArg;
    protected String varArg;
    protected StmtNode code;
    protected List<String> argNames = new ArrayList<String>();
    protected Map<String, PythonNode> defaults = new LinkedHashMap<String, PythonNode>();

    public FunctionNode(LambdaNode lambda) {
        kwArg = lambda.getKwArg();
        varArg = lambda.getVarArg();
        code = new StmtNode();
        code.addNode(new ReturnNode(lambda.getCode()));
        argNames = lambda.getArgNames();
        defaults = lambda.getDefaults();
        name = lambda.getGeneratedName();
    }

    public FunctionNode() {
        super();
    }

    public FunctionNode(boolean init) {
        super(init);
    }

    public FunctionNode(Element jdom) {
        super(jdom);

        populateStatic(jdom);
        code = (StmtNode) makeNode(getFirst(jdom, "code"));
        if (code != null) code.setParent(this);

        List defaults = jdom.getChild("defaults").getChildren();
        if (defaults != null && !defaults.isEmpty()) {
            int indexToRemove = argNames.size() - defaults.size();
            for (Object def : defaults) {
                String arg = argNames.remove(indexToRemove);
                this.defaults.put(arg, makeNode((Element) def));
            }
        }
        
    }

    public FunctionNode(Node node) {
        super(node);

        populateStatic(node.getJdomElement());
        code = (StmtNode) makeNode(getFirst(node, "code"));
        if (code != null) code.setParent(this);

        List defaults = node.getChildren("defaults").get(0).getChildren();
        if (defaults != null && !defaults.isEmpty()) {
            int indexToRemove = argNames.size() - defaults.size();
            for (Object def : defaults) {
                String arg = argNames.remove(indexToRemove);
                this.defaults.put(arg, makeNode(((Node) def)));
            }
        }
    }

    public void print(PrintStream out) {
        out.append("def ").append(name).append("(");
        boolean first = true;
        for (String arg : argNames) {
            if (!first) out.append(", ");
            first = false;
            out.append(arg);
        }

        for (Map.Entry<String, PythonNode> entry : defaults.entrySet()) {
            if (!first) out.append(", ");
            first = false;
            out.append(entry.getKey()).append('=');
            entry.getValue().print(out);
        }

        if (varArg != null) {
            if (!first) out.append(", ");
            first = false;
            out.append('*').append(varArg);
        }

        if (kwArg != null) {
            if (!first) out.append(", ");
            out.append("**").append(kwArg);
        }

        out.println("):");
        indent += indentStep;
        code.print(out);
        indent -= indentStep;
    }

    private void populateStatic(Element jdom) {
        name = jdom.getAttributeValue("name");
        String args = Utils.trimToNull(jdom.getChildText("argnames"));
        if (args != null) {
            this.argNames.addAll(Arrays.asList(args.split(",")));
        }
        if (Utils.trimToNull(jdom.getChildText("kwargs")) != null) {
            kwArg = argNames.remove(argNames.size() - 1);
        }
        if (Utils.trimToNull(jdom.getChildText("varargs")) != null) {
            varArg = argNames.remove(argNames.size() - 1);
        }
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(code).add(defaults.values()).toList();
    }

    public String getKwArg() {
        return kwArg;
    }

    public String getVarArg() {
        return varArg;
    }

    public StmtNode getCode() {
        return code;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public Map<String, PythonNode> getDefaults() {
        return defaults;
    }

    public void setKwArg(String kwArg) {
        this.kwArg = kwArg;
    }

    public void setVarArg(String varArg) {
        this.varArg = varArg;
    }

    public void setCode(StmtNode code) {
        this.code = code;
        setAsParent(code);
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public void setDefaults(Map<String, PythonNode> defaults) {
        this.defaults = defaults;
    }

}
