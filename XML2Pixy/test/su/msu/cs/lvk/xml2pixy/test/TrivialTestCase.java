package su.msu.cs.lvk.xml2pixy.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created at: [09.10.2007] 11:51:56
 *
 * @author gklimov
 */
public class TrivialTestCase extends BaseTestCase {

    private static final String[] TEST_FILES = {
            "arith1.py",
            "arith2.py",
            "arith3.py",
            "arith4.py",
            "arith5.py",
            "arith6.py",
            "arith7.py",
            "arith8.py",
            "arith9.py",
            "arith10.py",
            "arith11.py",
            "arith12.py",
            "arith13.py",
            "arith14.py",
            "arith15.py",
            "arith16.py",
            "assign1.py",
            "assign2.py",
            "bool1.py",
            "bool2.py",
            "bool3.py",
            "bool4.py",
            "bool5.py",
            "bool6.py",
            "bool7.py",
            "bool8.py",
            "class1.py",
            "class2.py",
            "class3.py",
            "class4.py",
            "class5.py",
            "class6.py",
            "class7.py",
            "class8.py",
            "class9.py",
            "class10.py",
            "class11.py",
            "class12.py",
            "class13.py",
            "class14.py",
            "class15.py",
            "class16.py",
            "empty.py",
            "formatted_str1.py",
            "formatted_str2.py",
            "fun1.py",
            "fun2.py",
            "fun3.py",
            "fun4.py",
            "fun5.py",
            "fun6.py",
            "fun7.py",
            "fun8.py", //TODO
            "fun9.py",
            "fun10.py", //TODO
            "fun11.py",
            "if1.py",
            "if2.py",
            "if3.py",
            "if4.py",
            "loop1.py",
            "loop2.py",
            "loop3.py",
            "loop4.py",
            "loop5.py",
            "loop6.py",
            "loop7.py",
            "pass1.py",
            "pass2.py",
            "slice1.py",
            "slice2.py",
            "slice3.py",
            "slice4.py",
            "str1.py",
            "str2.py",
            "str3.py",
            "str4.py",
            "str5.py",
            "str6.py",
            "str7.py",
            "str8.py",
            "str9.py",
            "try_except1.py",
            "try_finally1.py",
            "tuple1.py",
            "tuple2.py",
            "tuple3.py",
            "tuple4.py",
            "tuple5.py",
            "tuple6.py",
            "tuple7.py",
            "tuple8.py",
            "tuple9.py",
            "tuple10.py",
            "tuple11.py",
            "tuple12.py"
    };

    public static Test suite() {
        TestSuite suite = new TestSuite();
        for (String file : TEST_FILES) {
            suite.addTest(new TrivialTestCase(file));
        }
        return suite;
    }

    protected TrivialTestCase(String fileName) {
        super(fileName);
    }

    protected void setUp() {
        this.path = "tests/func/trivial/";
    }

}
