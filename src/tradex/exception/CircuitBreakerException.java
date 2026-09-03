package tradex.exception;

/**
 * Thrown when an order price violates the daily upper or lower price circuit limit.
 */
public class CircuitBreakerException extends TradeXException {
    public CircuitBreakerException(String message) {
        super(message);
    }
}
