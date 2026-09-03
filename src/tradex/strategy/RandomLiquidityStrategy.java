package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderType;

import java.util.Optional;
import java.util.Random;

/**
 * Liquidity Provider Strategy: Quotes dual-sided limit orders near the mid-market price
 * to seed depth into the double-auction order book.
 */
public class RandomLiquidityStrategy implements TradingStrategy {
    private final Random random;

    public RandomLiquidityStrategy(Random random) {
        this.random = random;
    }

    public RandomLiquidityStrategy() {
        this(new Random());
    }

    @Override
    public String getName() {
        return "LIQUIDITY_PROVIDER";
    }

    @Override
    public Optional<Order> evaluate(Stock stock, int botAccountId, double availableCash, int currentShareHolding) {
        double ltp = stock.getCurrentPrice();
        boolean isBuy = random.nextBoolean();

        if (isBuy) {
            // Place resting bid slightly below LTP (-0.1% to -0.6%)
            double discount = 0.001 + (random.nextDouble() * 0.005);
            double bidPrice = Math.round((ltp * (1.0 - discount)) * 20.0) / 20.0;
            int qty = 5 + random.nextInt(20);
            if (availableCash >= bidPrice * qty) {
                return Optional.of(new Order.Builder()
                        .accountId(botAccountId)
                        .symbol(stock.getSymbol())
                        .side(OrderSide.BUY)
                        .type(OrderType.LIMIT)
                        .quantity(qty)
                        .price(bidPrice)
                        .build());
            }
        } else {
            // Place resting ask slightly above LTP (+0.1% to +0.6%)
            if (currentShareHolding >= 5) {
                double premium = 0.001 + (random.nextDouble() * 0.005);
                double askPrice = Math.round((ltp * (1.0 + premium)) * 20.0) / 20.0;
                int qty = Math.min(currentShareHolding, 5 + random.nextInt(15));
                return Optional.of(new Order.Builder()
                        .accountId(botAccountId)
                        .symbol(stock.getSymbol())
                        .side(OrderSide.SELL)
                        .type(OrderType.LIMIT)
                        .quantity(qty)
                        .price(askPrice)
                        .build());
            }
        }

        return Optional.empty();
    }
}
