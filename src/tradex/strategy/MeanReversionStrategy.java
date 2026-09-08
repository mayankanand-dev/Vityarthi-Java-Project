package tradex.strategy;

import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderType;

import java.util.Optional;

public class MeanReversionStrategy implements TradingStrategy {

    @Override
    public String getName() {
        return "MEAN_REVERSION";
    }

    @Override
    public Optional<Order> evaluate(Stock stock, int botAccountId, double availableCash, int currentShareHolding) {
        double open = stock.getDayOpen();
        double current = stock.getCurrentPrice();
        if (open <= 0) return Optional.empty();

        double devPct = ((current - open) / open) * 100.0;

        if (devPct < -2.5 && availableCash >= current * 10) {
            double buyPrice = Math.round((current * 1.001) * 20.0) / 20.0;
            int qty = Math.min(30, (int) (availableCash / buyPrice));
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
        } else if (devPct > 2.5 && currentShareHolding >= 5) {
            double sellPrice = Math.round((current * 0.999) * 20.0) / 20.0;
            int qty = Math.min(currentShareHolding, 25);
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
