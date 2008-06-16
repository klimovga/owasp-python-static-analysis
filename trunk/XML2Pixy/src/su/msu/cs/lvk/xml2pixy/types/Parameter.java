package su.msu.cs.lvk.xml2pixy.types;

import su.msu.cs.lvk.xml2pixy.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 17.01.2008
 * Time: 0:42:32
 */
public class Parameter {

    private String name;
    private Type type;

    public Parameter(String param) {
        String[] pData = param.split(":");
        if (pData.length != 2) {
            throw new IllegalArgumentException();
        }

        setName(Utils.trimToNull(pData[0]));
        setType(new Type(Utils.trimToNull(pData[1])));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String toString() {
        return name + " : " + type.toString();
    }
}
