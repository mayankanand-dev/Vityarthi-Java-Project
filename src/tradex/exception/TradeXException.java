package tradex.exception;

public class TradeXException extends Exception {
    public TradeXException(String message) { super(message); }
    public TradeXException(String message, Throwable cause) { super(message, cause); }
}

