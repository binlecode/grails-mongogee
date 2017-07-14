package grails.plugin.mongogee.exception;

/**
 * @author abelski
 */
public class MongoSeaException extends Exception {
    public MongoSeaException(String message) {
        super(message);
    }

    public MongoSeaException(String message, Throwable cause) {
        super(message, cause);
    }
}
