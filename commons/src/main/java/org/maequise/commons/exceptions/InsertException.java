package org.maequise.commons.exceptions;

/**
 * <p>Error thrown when the persisting entity fails</p>
 */
public class InsertException extends Exception {
    public InsertException() {
        super();
    }

    public InsertException(String message) {
        super(message);
    }

    public InsertException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertException(Throwable cause) {
        super(cause);
    }
}
