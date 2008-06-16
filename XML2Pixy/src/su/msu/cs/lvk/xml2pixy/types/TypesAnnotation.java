package su.msu.cs.lvk.xml2pixy.types;

import su.msu.cs.lvk.xml2pixy.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 17.01.2008
 * Time: 0:39:54
 */
public class TypesAnnotation {

    /*
   - input
       - param
           - name : string
           - type : ?
   - output
       - param
           ----||----
           - may be special param "_return"
    */

    private Parameters inputParams;
    private Parameters outputParams;

    public TypesAnnotation() {
        inputParams = new Parameters();
        outputParams = new Parameters();
    }

    public TypesAnnotation(String annotation) {
        if (Utils.isBlank(annotation)) {
            throw new IllegalArgumentException();
        }
        String[] params = annotation.split("->");

        inputParams = new Parameters(Utils.trimToNull(params[0]));
        outputParams = new Parameters(Utils.trimToNull(params[1]));

    }

    public Parameters getInputParams() {
        return inputParams;
    }

    public Parameters getOutputParams() {
        return outputParams;
    }


    public String toString() {
        return inputParams.toString() + " -> " + outputParams.toString();
    }
}
