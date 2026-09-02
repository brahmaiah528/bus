package exception;

/**
 * User-defined custom checked exception thrown when an operation requires an active pass
 * but the pass has already expired or exceeded permissible grace periods.
 */
public class PassValidityExpiredException extends Exception {
    private static final long serialVersionUID = 1L;

    public PassValidityExpiredException() {
        super("The bus pass validity has already expired.");
    }

    public PassValidityExpiredException(String message) {
        super(message);
    }

    public PassValidityExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
