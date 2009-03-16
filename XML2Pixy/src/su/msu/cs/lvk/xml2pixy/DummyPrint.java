package su.msu.cs.lvk.xml2pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpLexer;
import at.ac.tuwien.infosys.www.phpparser.PhpParser;
import su.msu.cs.lvk.xml2pixy.ast.python.ModuleNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.jdom.JdomVisitor;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;
import su.msu.cs.lvk.xml2pixy.transform.Node;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.ASTVisitor;
import su.msu.cs.lvk.xml2pixy.transform.astvisitor.VisitorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * User: Panther
 * Date: 22.09.2008
 */
public class DummyPrint {

    public static void main(String[] args) throws Exception {
        ASTVisitor.reset();
        ModuleNode node = ProcessingUtils.parseFile(args[0]);

        System.out.println("---------- begin source ");
        node.print(System.out);
        System.out.println();
        System.out.println("---------- end source ");

        ModuleNode woGens = ProcessingUtils.simplifyGenerators(node);

        System.out.println("---------- begin simplified generators");
        woGens.print(System.out);
        System.out.println();
        System.out.println("---------- end simplified generators");

        ProcessingUtils.processScopes(woGens);

        ModuleNode after = ProcessingUtils.simplifyPython(woGens, true);

        System.out.println("---------- begin transformed");
        after.print(System.out);
        System.out.println();
        System.out.println("---------- end transformed");

/*
        after = ProcessingUtils.ssaTransform(after);
        System.out.println("---------- begin SSA");
        after.print(System.out);
        System.out.println();
        System.out.println("---------- end SSA");
*/

        ParseNode php = Utils.buildParseTree(after);

        System.out.println("---------- begin php");
        Utils.printTree(php);
        System.out.println();
        System.out.println("---------- end php");


        // test for syntax correctness
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos).append("<?php");
        pw.println();
        pw.flush();
        Utils.printTree(php, null, baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        PhpLexer phpLexer = new PhpLexer(bais);
        phpLexer.setFileName(args[0]);
        PhpParser phpParser = new PhpParser(phpLexer);
        ParseNode expected = (ParseNode) phpParser.parse().value;


        String compare = ProcessingUtils.compare(expected, php);
        if (compare != null) {
            System.out.println(compare);
            Utils.printTree(expected);
        }

    }

    public static class ASTBuilder implements JdomVisitor {
        public void visit(Node node) throws VisitorException {
            node.setAstNode(PythonNode.makeNode(node));
        }
    }

}
