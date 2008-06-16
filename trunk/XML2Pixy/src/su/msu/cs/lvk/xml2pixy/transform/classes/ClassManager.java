package su.msu.cs.lvk.xml2pixy.transform.classes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 21.11.2007
 * Time: 1:57:32
 */
public class ClassManager {

    private static ClassManager instance = null;

    public static ClassManager getInstance() {
        return instance == null ? (instance = new ClassManager()) : instance;
    }

    public void reset() {
        classes = new LinkedHashMap<String, PyClass>();
    }

    private Map<String, PyClass> classes;

    private ClassManager() {
        this.reset();
    }

    public PyClass getPyClass(String name) {
        return classes.get(name);
    }

    public void addPyClass(String name, PyClass clazz) {
        classes.put(name, clazz);
    }

    public Map<String, PyClass> getClasses() {
        return classes;
    }

}
