package tradex.model;

import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;

import java.time.LocalDateTime;

public class Order implements Comparable<Order> {
    String orderId;
    int accountId;
    String symbol;
    OrderSide side;
    OrderType type;
    int originalQuantity;
    int filledQuantity;
    double price;
    double stopPrice;
    OrderStatus status;
    LocalDateTime timestamp;

    public Order(String orderId, int accountId, String symbol, OrderSide side, OrderType type,
                 int originalQuantity, int filledQuantity, double price, double stopPrice,
                 OrderStatus status, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.accountId = accountId;
        if (symbol != null) {
            this.symbol = symbol.toUpperCase().trim();
        } else {
            this.symbol = "";
        }
        this.side = side;
        this.type = type;
        this.originalQuantity = originalQuantity;
        this.filledQuantity = filledQuantity;
        this.price = price;
        this.stopPrice = stopPrice;
        if (status != null) {
            this.status = status;
        } else {
            this.status = OrderStatus.OPEN;
        }
        if (timestamp != null) {
            this.timestamp = timestamp;
        } else {
            this.timestamp = LocalDateTime.now();
        }
    }

    public String getOrderId() { return orderId; }
    public int getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public int getOriginalQuantity() { return originalQuantity; }
    public synchronized int getFilledQuantity() { return filledQuantity; }
    public synchronized int getRemainingQuantity() { return originalQuantity - filledQuantity; }
    public synchronized double getPrice() { return price; }
    public synchronized void setPrice(double price) { this.price = price; }
    public synchronized double getStopPrice() { return stopPrice; }
    public synchronized OrderStatus getStatus() { return status; }
    public synchronized void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public synchronized void fill(int qty) {
        if (qty <= 0) return;
        this.filledQuantity = this.filledQuantity + qty;
        if (this.filledQuantity >= this.originalQuantity) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public synchronized boolean isTerminal() {
        if (status == OrderStatus.FILLED) return true;
        if (status == OrderStatus.CANCELLED) return true;
        if (status == OrderStatus.REJECTED) return true;
        if (status == OrderStatus.EXPIRED) return true;
        return false;
    }

    @Override
    public int compareTo(Order other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return "Order[" + orderId + " " + side + " " + filledQuantity + "/" + originalQuantity
                + " " + symbol + " @ " + price + " status=" + status + "]";
    }

    // Builder makes it easier to create orders step by step
    public static class Builder {
        String orderId;
        int accountId;
        String symbol;
        OrderSide side;
        OrderType type = OrderType.LIMIT;
        int quantity;
        int filledQuantity = 0;
        double price = 0.0;
        double stopPrice = 0.0;
        OrderStatus status = OrderStatus.OPEN;
        LocalDateTime timestamp = LocalDateTime.now();

        public Builder() {}

        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder accountId(int accountId) { this.accountId = accountId; return this; }
        public Builder symbol(String symbol) { this.symbol = symbol; return this; }
        public Builder side(OrderSide side) { this.side = side; return this; }
        public Builder type(OrderType type) { this.type = type; return this; }
        public Builder quantity(int quantity) { this.quantity = quantity; return this; }
        public Builder filledQuantity(int filledQuantity) { this.filledQuantity = filledQuantity; return this; }
        public Builder price(double price) { this.price = price; return this; }
        public Builder stopPrice(double stopPrice) { this.stopPrice = stopPrice; return this; }
        public Builder status(OrderStatus status) { this.status = status; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public Order build() {
            if (quantity <= 0) throw new IllegalArgumentException("Order quantity must be positive");
            if (symbol == null || symbol.trim().isEmpty()) throw new IllegalArgumentException("Order symbol cannot be empty");
            if (orderId == null) {
                int randomNum = (int) (Math.random() * 900 + 100);
                this.orderId = "ORD-" + System.currentTimeMillis() + "-" + randomNum;
            }
            return new Order(orderId, accountId, symbol, side, type, quantity, filledQuantity,
                    price, stopPrice, status, timestamp);
        }
    }
}
