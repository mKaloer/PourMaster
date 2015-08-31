package pourmaster.exceptions;

/**
 * Thrown when a specific field cannot be found.
 */
public class FieldNotFoundException extends RuntimeException {

    public FieldNotFoundException(String message) {
        super(message);
    }

    public FieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
