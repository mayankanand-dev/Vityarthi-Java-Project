package tradex.model;

import java.time.LocalDateTime;

public class Trade {
    String tradeId;
    String buyOrderId;
    String sellOrderId;
    int buyerAccountId;
    int sellerAccountId;
    String symbol;
    int quantity;
    double price;
    double brokerageBuyer;
    double brokerageSeller;
    LocalDateTime timestamp;

    public Trade(String tradeId, String buyOrderId, String sellOrderId,
                 int buyerAccountId, int sellerAccountId, String symbol,
                 int quantity, double price, double brokerageBuyer, double brokerageSeller,
                 LocalDateTime timestamp) {
        if (tradeId != null) {
            this.tradeId = tradeId;
        } else {
            int randomNum = (int)(Math.random() * 900 + 100);
            this.tradeId = "TRD-" + System.currentTimeMillis() + "-" + randomNum;
        }
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerAccountId = buyerAccountId;
        this.sellerAccountId = sellerAccountId;
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.price = price;
        this.brokerageBuyer = brokerageBuyer;
        this.brokerageSeller = brokerageSeller;
        if (timestamp != null) {
            this.timestamp = timestamp;
        } else {
            this.timestamp = LocalDateTime.now();
        }
    }

    public String getTradeId() { return tradeId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public int getBuyerAccountId() { return buyerAccountId; }
    public int getSellerAccountId() { return sellerAccountId; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public double getGrossAmount() {
        double total = quantity * price;
        return total;
    }

    public double getBrokerageBuyer() { return brokerageBuyer; }
    public double getBrokerageSeller() { return brokerageSeller; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Trade[" + tradeId + " " + symbol + " Qty:" + quantity + " @ Rs." + price
                + " Buyer:" + buyerAccountId + " Seller:" + sellerAccountId + " Time:" + timestamp + "]";
    }
}
