package tradex.model;

import java.time.LocalDateTime;

/**
 * Immutable execution record generated when a buy and sell order match.
 */
public class Trade {
    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final int buyerAccountId;
    private final int sellerAccountId;
    private final String symbol;
    private final int quantity;
    private final double price;
    private final double brokerageBuyer;
    private final double brokerageSeller;
    private final LocalDateTime timestamp;

    public Trade(String tradeId, String buyOrderId, String sellOrderId,
                 int buyerAccountId, int sellerAccountId, String symbol,
                 int quantity, double price, double brokerageBuyer, double brokerageSeller,
                 LocalDateTime timestamp) {
        this.tradeId = tradeId != null ? tradeId : "TRD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerAccountId = buyerAccountId;
        this.sellerAccountId = sellerAccountId;
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.price = price;
        this.brokerageBuyer = brokerageBuyer;
        this.brokerageSeller = brokerageSeller;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public int getBuyerAccountId() {
        return buyerAccountId;
    }

    public int getSellerAccountId() {
        return sellerAccountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getGrossAmount() {
        return quantity * price;
    }

    public double getBrokerageBuyer() {
        return brokerageBuyer;
    }

    public double getBrokerageSeller() {
        return brokerageSeller;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Trade[%s %s Qty:%d @ ₹%.2f Buyer:%d Seller:%d Time:%s]",
                tradeId, symbol, quantity, price, buyerAccountId, sellerAccountId, timestamp);
    }
}
