package tradex.model.enums;

/**
 * State lifecycle of an order within the matching engine.
 */
public enum OrderStatus {
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED,
    EXPIRED
}
