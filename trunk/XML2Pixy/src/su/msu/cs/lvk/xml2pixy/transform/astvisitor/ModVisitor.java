package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import org.jdom.Attribute;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 15:08:11
 */

/**
 * @see AddVisitor
 */
public class ModVisitor extends AddVisitor {
    public ModVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ModVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        // check, whether the first argument is a string constant,
        // in that case we should deal with formatted output
        // XXX: we can't detect formatted output in all the cases, as the left arg may be a variable
        if (isLeftFormatString(node)) {
            createFormattedStr(node, currentFile, lineno, module);
        } else {
            super.visit(node, currentFile, lineno, module);
        }
    }

    private void createFormattedStr(Node node, String currentFile, int lineno, String module) {
        ParseNode leftNode = getFirstChild(node, "left").getChildren().get(0).getParseNode();
        Node rightNode = getFirstChild(node, "right");
        if (rightNode.getChildren().size() == 1) {
            // one argument in formatted output
            Node arg = rightNode.getChildren().get(0);
            ParseNode argNode = arg.getParseNode();
            String printfName = arg.getJdomElement().getName().equals("Tuple") ? "analysis_vsprintf" : "analysis_sprintf";
            ParseNode funcNode = makeFunctionCall(printfName, new ParseNode[] {argNode}, leftNode, lineno);
            node.setParseNode(funcNode);
        }
    }

    private boolean isLeftFormatString(Node node) {
        Node leftNode = getFirstChild(node, "left");
        if (leftNode.getChildren().size() == 1) {
            Node child = leftNode.getChildren().get(0);
            if ("Const".equals(child.getJdomElement().getName())) {
                Attribute valueAttr = child.getJdomElement().getAttribute("value");
                if (valueAttr != null && valueAttr.getValue().indexOf("%") != -1) {
                    return true;
                }
            }
        }

        return false;
    }
}
