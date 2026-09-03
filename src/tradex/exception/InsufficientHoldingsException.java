package tradex.exception;

/**
 * Thrown when an account attempts to place a sell order without owning sufficient shares.
 */
public class InsufficientHoldingsException extends TradeXException {
    public InsufficientHoldingsException(String message) {
        super(message);
    }
}
