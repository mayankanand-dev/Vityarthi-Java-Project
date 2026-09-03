package tradex.exception;

/**
 * Thrown when an account attempts to place a buy order or withdrawal exceeding available funds.
 */
public class InsufficientFundsException extends TradeXException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
