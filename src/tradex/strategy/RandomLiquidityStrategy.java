package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderType;

import java.util.Optional;
import java.util.Random;

public class RandomLiquidityStrategy implements TradingStrategy {
    Random random;

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
