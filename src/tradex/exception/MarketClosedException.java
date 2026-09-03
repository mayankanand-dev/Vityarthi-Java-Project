package tradex.exception;

/**
 * Thrown when an order or trade is attempted while trading on an instrument or the exchange is paused/closed.
 */
public class MarketClosedException extends TradeXException {
    public MarketClosedException(String message) {
        super(message);
    }
}
