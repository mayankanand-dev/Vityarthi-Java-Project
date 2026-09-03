package tradex.exception;

/**
 * Thrown when an unrecognized or unlisted ticker symbol is queried or referenced.
 */
public class InvalidStockException extends TradeXException {
    public InvalidStockException(String message) {
        super(message);
    }
}
