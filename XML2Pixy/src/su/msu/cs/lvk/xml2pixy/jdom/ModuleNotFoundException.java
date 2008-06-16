package su.msu.cs.lvk.xml2pixy.jdom;

/**
 * This exception when module could not be located.
 *
 * @author konnov
 */
public class ModuleNotFoundException extends Exception {
    public ModuleNotFoundException(String string) {
        super(string);
    }

    public ModuleNotFoundException(String string, Throwable throwable) {
        super(string, throwable);
    }
}
