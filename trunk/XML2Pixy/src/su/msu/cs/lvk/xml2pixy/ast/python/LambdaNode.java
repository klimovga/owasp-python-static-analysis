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
 * Date: 01.10.2008
 * Time: 20:31:09
 */
public class LambdaNode extends PythonNode {

    protected String kwArg;
    protected String varArg;
    protected PythonNode code;
    protected List<String> argNames = new ArrayList<String>();
    protected Map<String, PythonNode> defaults = new LinkedHashMap<String, PythonNode>();

    protected String generatedName;

    public LambdaNode() {
        super();
    }

    public LambdaNode(Element jdom) {
        super(jdom);

        populateStatic(jdom);
        code = makeNode(getFirst(jdom, "code"));
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

    public LambdaNode(Node node) {
        super(node);

        populateStatic(node.getJdomElement());
        code = makeNode(getFirst(node, "code"));
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
        out.append("lambda ");
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

        out.append(": ");
        code.print(out);

    }

    private void populateStatic(Element jdom) {
        String args = Utils.trimToNull(jdom.getChildText("argnames"));
        if (args != null) {
            this.argNames = new ArrayList<String>(Arrays.asList(args.split("\n")));
            for (int i = 0; i < this.argNames.size(); i++) {
                this.argNames.set(i, this.argNames.get(i).trim());
            }
        }
        if (jdom.getChildText("kwargs") == null) {
            kwArg = argNames.remove(argNames.size() - 1);
        }
        if (jdom.getChildText("varargs") == null) {
            varArg = argNames.remove(argNames.size() - 1);
        }
    }

    public List<ASTNode> getChildren() {
        return new ListBuilder<ASTNode>().add(code).add(defaults.values()).toList();
    }

    public String getGeneratedName() {
        return generatedName;
    }

    public void setGeneratedName(String generatedName) {
        this.generatedName = generatedName;
    }

    public String getKwArg() {
        return kwArg;
    }

    public void setKwArg(String kwArg) {
        this.kwArg = kwArg;
    }

    public String getVarArg() {
        return varArg;
    }

    public void setVarArg(String varArg) {
        this.varArg = varArg;
    }

    public PythonNode getCode() {
        return code;
    }

    public void setCode(PythonNode code) {
        this.code = code;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public Map<String, PythonNode> getDefaults() {
        return defaults;
    }

    public void setDefaults(Map<String, PythonNode> defaults) {
        this.defaults = defaults;
    }
}
