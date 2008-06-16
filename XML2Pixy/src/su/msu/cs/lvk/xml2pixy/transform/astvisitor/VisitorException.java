package su.msu.cs.lvk.xml2pixy.transform.astvisitor;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 25.12.2007
 * Time: 1:23:19
 */
public class VisitorException extends Exception {

    public VisitorException() {
        super();
    }

    public VisitorException(String message) {
        super(message);
    }

    public VisitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public VisitorException(Throwable cause) {
        super(cause);
    }
}
