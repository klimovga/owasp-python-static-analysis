package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import su.msu.cs.lvk.xml2pixy.parser.ParseNodeHelper;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.SymbolTable;
import su.msu.cs.lvk.xml2pixy.transform.classes.ClassManager;
import su.msu.cs.lvk.xml2pixy.transform.classes.PyClass;
import su.msu.cs.lvk.xml2pixy.transform.function.Constructor;
import su.msu.cs.lvk.xml2pixy.transform.function.FunctionManager;

/**
 * Created by IntelliJ IDEA.
 * User: gaklimov
 * Date: 13.11.2007
 * Time: 14:42:14
 */
public class ClassVisitor extends ASTVisitor {

    public ClassVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public ClassVisitor() {
        super();
    }

    public void visit(Node node, String currentFile, int lineno, String module) throws VisitorException {
        module = module.replaceAll("\\.", "__");
        String className = node.getJdomElement().getAttributeValue("name");
        className = module + "__" + className;

        PyClass clazz = new PyClass(className, node);
        ClassManager.getInstance().addPyClass(className, clazz);

        Node bases = getFirstChild(node, "bases");

        for (Node baseClass : bases.getChildren()) {
            if (baseClass.getJdomElement().getName().equals("Name")) {
                String baseClassName = module + "__" + baseClass.getJdomElement().getAttributeValue("name");
                clazz.addBaseClass(baseClassName);
            }
        }

        visitClassStatements(node, className, module, currentFile);

    }

    protected void visitClassStatements(Node node, String className, String module, String currentFile) {
        Node code = getFirstChild(getFirstChild(node, "code"), null);
        ParseNodeHelper helper = new ParseNodeHelper();
        helper.setCurrentFile(currentFile);
        ParseNode staticFields = null;

        for (Node statement : getFirstChild(code, "nodes").getChildren()) {
            if (statement.getJdomElement().getName().equals("Function")) {
                String name = statement.getJdomElement().getAttributeValue("name");
                String[] args = getFirstChild(statement, "argnames").getJdomElement().getTextTrim().split(",");
                if (args.length == 1 && "".equals(args[0])) args = new String[0];
                String methodName;
                if (name.equals("__init__")) {
                    methodName = className + "__" + name + args.length;
                    Constructor constructor = new Constructor(ClassManager.getInstance().getPyClass(className));
                    constructor.setGenerated(false);

                    Node constructorBody = getFirstChild(getFirstChild(statement, "code"), null);

                    // pass all arguments by reference!
                    for (int i = 0; i < args.length; ++i) {
                        args[i] = "&" + args[i];
                    }

                    constructor.setName(methodName);
                    constructor.setSource(code);
                    constructor.setOriginalName("__init__");
                    constructor.setArguments(args);
                    constructor.setBody(constructorBody);
                    constructor.setModule(module);
                    constructor.setCurrentFile(currentFile);

                    FunctionManager.getInstance().addFunction(methodName, constructor);
                } else {
                    methodName = FunctionManager.METHOD_PREFIX + name + args.length;
                    FunctionManager.getInstance().registerMethod(methodName, name, node, className, currentFile, module);
                }

            } else if (statement.getJdomElement().getName().equals("Assign")) {

                if (staticFields == null) {
                    staticFields = helper.create(PhpSymbols.top_statement_list, makeEpsilon());
                }

                staticFields = addLastTopStatement(staticFields, makeStatementFromExpr(statement.getParseNode(),
                        getLineno(statement.getJdomElement())));
            }
        }

        node.setParseNode(staticFields);

    }

}
