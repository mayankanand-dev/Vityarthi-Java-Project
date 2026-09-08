package tradex.exchange;

import tradex.exception.CircuitBreakerException;
import tradex.model.Stock;
import tradex.model.enums.MarketStatus;

public class CircuitBreaker {

    public static void validatePriceBand(Stock stock, double requestedPrice) throws CircuitBreakerException {
        if (requestedPrice <= 0) {
            throw new CircuitBreakerException("Order price must be strictly positive");
        }
        if (requestedPrice > stock.getUpperCircuit()) {
            throw new CircuitBreakerException(String.format(
                    "Price Rs.%.2f breaches upper circuit band (Rs.%.2f) for %s",
                    requestedPrice, stock.getUpperCircuit(), stock.getSymbol()));
        }
        if (requestedPrice < stock.getLowerCircuit()) {
            throw new CircuitBreakerException(String.format(
                    "Price Rs.%.2f breaches lower circuit band (Rs.%.2f) for %s",
                    requestedPrice, stock.getLowerCircuit(), stock.getSymbol()));
        }
    }

    public static void checkMarketStatus(Stock stock) throws CircuitBreakerException {
        if (stock.getStatus() == MarketStatus.HALTED) {
            throw new CircuitBreakerException("Trading in " + stock.getSymbol() + " is currently HALTED by exchange circuit limits.");
        }
        if (stock.getStatus() == MarketStatus.CLOSED) {
            throw new CircuitBreakerException("The market for " + stock.getSymbol() + " is currently CLOSED.");
        }
    }
}
