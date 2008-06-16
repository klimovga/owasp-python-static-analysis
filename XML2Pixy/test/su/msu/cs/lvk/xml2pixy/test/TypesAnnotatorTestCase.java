package su.msu.cs.lvk.xml2pixy.test;

import junit.framework.TestCase;
import su.msu.cs.lvk.xml2pixy.types.TypesAnnotator;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 17.01.2008
 * Time: 1:37:15
 */
public class TypesAnnotatorTestCase extends TestCase {

    public void test01() {
        String annotation = "  ";
        assertNull(TypesAnnotator.parse(annotation));
    }

    public void test02() {
        String annotation = "a : int -> _return : int";
        assertEquals(annotation, TypesAnnotator.parse(annotation).toString());
    }

    public void test03() {
        String annotation = "a : int, b : str -> a : str, b : int";
        assertEquals(annotation, TypesAnnotator.parse(annotation).toString());
    }

    public void test04() {
        String annotation = "a : 'a, b : 'a -> _result : 'a";
        assertEquals(annotation, TypesAnnotator.parse(annotation).toString());
    }
}
