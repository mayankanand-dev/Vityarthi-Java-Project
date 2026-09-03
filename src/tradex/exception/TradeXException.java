package tradex.exception;

/**
 * Base checked exception class for all domain-specific errors in TradeX.
 */
public class TradeXException extends Exception {
    public TradeXException(String message) {
        super(message);
    }

    public TradeXException(String message, Throwable cause) {
        super(message, cause);
    }
}
