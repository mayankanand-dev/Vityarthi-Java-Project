package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;

import java.util.Optional;

/**
 * Strategy interface for automated algorithmic participants.
 * Adheres to the classic Gang-of-Four Strategy Pattern.
 */
public interface TradingStrategy {
    String getName();

    /**
     * Evaluates current instrument statistics and generates a decision order,
     * or Optional.empty() if no trade condition is met.
     */
    Optional<Order> evaluate(Stock stock, int botAccountId, double availableCash, int currentShareHolding);
}
