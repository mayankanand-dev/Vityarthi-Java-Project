package tradex.exception;

/**
 * Thrown on invalid login credentials or unauthorized operations.
 */
public class AuthenticationException extends TradeXException {
    public AuthenticationException(String message) {
        super(message);
    }
}
