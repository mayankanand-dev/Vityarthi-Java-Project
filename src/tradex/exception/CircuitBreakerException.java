package tradex.exception;


public class CircuitBreakerException extends TradeXException {
    public CircuitBreakerException(String message) {
        super(message);
    }
}

