package org.maequise.commons.exceptions;

/**
 * <p>Error thrown when no {@code @Id} determining the column of id is found</p>
 */
public class UnknownIdException extends RuntimeException {
    public UnknownIdException(String message) {
        super(message);
    }
}
