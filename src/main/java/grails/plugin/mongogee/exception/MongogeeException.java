package grails.plugin.mongogee.exception;

/**
 * @author abelski
 */
public class MongogeeException extends Exception {
    public MongogeeException(String message) {
        super(message);
    }

    public MongogeeException(String message, Throwable cause) {
        super(message, cause);
    }
}
