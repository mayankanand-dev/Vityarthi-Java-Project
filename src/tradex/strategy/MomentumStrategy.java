package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderType;

import java.util.Optional;

public class MomentumStrategy implements TradingStrategy {

    @Override
    public String getName() {
        return "MOMENTUM";
    }

    @Override
    public Optional<Order> evaluate(Stock stock, int botAccountId, double availableCash, int currentShareHolding) {
        double pct = stock.getChangePercentage();
        double ltp = stock.getCurrentPrice();

        if (pct > 1.2 && availableCash >= ltp * 10) {
            double buyPrice = Math.round((ltp * 1.002) * 20.0) / 20.0;
            int qty = Math.min(25, (int) (availableCash / buyPrice));
            if (qty > 0) {
                return Optional.of(new Order.Builder()
                        .accountId(botAccountId)
                        .symbol(stock.getSymbol())
                        .side(OrderSide.BUY)
                        .type(OrderType.LIMIT)
                        .quantity(qty)
                        .price(buyPrice)
                        .build());
            }
        } else if (pct < -1.5 && currentShareHolding >= 5) {
            double sellPrice = Math.round((ltp * 0.998) * 20.0) / 20.0;
            int qty = Math.min(currentShareHolding, 20);
            return Optional.of(new Order.Builder()
                    .accountId(botAccountId)
                    .symbol(stock.getSymbol())
                    .side(OrderSide.SELL)
                    .type(OrderType.LIMIT)
                    .quantity(qty)
                    .price(sellPrice)
                    .build());
        }

        return Optional.empty();
    }
}
