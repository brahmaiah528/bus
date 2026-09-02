package exception;

/**
 * User-defined custom checked exception thrown when an invalid, suspended,
 * or non-existent bus route is referenced during pass issuance or route query.
 */
public class InvalidRouteException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidRouteException() {
        super("Invalid or inactive bus route specified.");
    }

    public InvalidRouteException(String message) {
        super(message);
    }

    public InvalidRouteException(String message, Throwable cause) {
        super(message, cause);
    }
}
