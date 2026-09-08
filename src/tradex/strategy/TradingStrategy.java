package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;

import java.util.Optional;

public interface TradingStrategy {
    String getName();
    Optional<Order> evaluate(Stock stock, int botAccountId, double availableCash, int currentShareHolding);
}
