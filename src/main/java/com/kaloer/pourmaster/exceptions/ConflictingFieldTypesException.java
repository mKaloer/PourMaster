package com.kaloer.pourmaster.exceptions;

/**
 * Thrown when two different field types are used for the same field.
 */
public class ConflictingFieldTypesException extends RuntimeException {

    public ConflictingFieldTypesException(String message) {
        super(message);
    }

    public ConflictingFieldTypesException(String message, Throwable cause) {
        super(message, cause);
    }

}
