package su.msu.cs.lvk.xml2pixy.types;

import su.msu.cs.lvk.xml2pixy.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 16.01.2008
 * Time: 23:49:01
 */
public class TypesAnnotator {

    public static TypesAnnotation parse(String annotation) {
        // trim annotation
        annotation = Utils.trimToNull(annotation);
        // return null if annotation is not specified
        if (annotation == null) {
            return null;
        }

        return new TypesAnnotation(annotation);
    }

}
