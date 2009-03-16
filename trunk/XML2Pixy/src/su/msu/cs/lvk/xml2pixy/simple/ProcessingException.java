package su.msu.cs.lvk.xml2pixy.simple;

/**
 * User: KlimovGA
 * Date: 07.12.2008
 */
public class ProcessingException extends Exception {

    public ProcessingException() {
        super();
    }

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingException(Throwable cause) {
        super(cause);
    }
    
}
