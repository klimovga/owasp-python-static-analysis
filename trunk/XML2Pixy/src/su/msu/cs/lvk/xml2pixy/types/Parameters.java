package su.msu.cs.lvk.xml2pixy.types;

import su.msu.cs.lvk.xml2pixy.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 17.01.2008
 * Time: 0:42:22
 */
public class Parameters {

    private Map<String, Parameter> params;

    public Parameters() {
        params = new HashMap<String, Parameter>();
    }

    public Parameters(String params) {
        this();

        if (Utils.isBlank(params)) {
            throw new IllegalArgumentException();
        }

        for (String paramStr : params.split(",")) {
            Parameter param = new Parameter(Utils.trimToNull(paramStr));
            addParameter(param);
        }
    }

    public Parameter getParam(String param) {
        return params.get(param);
    }

    public void addParameter(Parameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Null parameter");
        }

        params.put(parameter.getName(), parameter);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Parameter param : params.values()) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(param.toString());
        }
        return buf.toString();
    }
}
