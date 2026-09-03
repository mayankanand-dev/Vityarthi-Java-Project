package tradex.exception;

/**
 * Thrown when an order fails business rule validation (e.g. non-positive quantity, price out of bounds).
 */
public class InvalidOrderException extends TradeXException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
