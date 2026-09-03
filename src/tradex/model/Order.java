package tradex.model;

import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Represents a buy or sell order submitted to the matching engine.
 * Employs the Builder design pattern for flexible and readable instantiation.
 */
public class Order implements Comparable<Order> {
    private final String orderId;
    private final int accountId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final int originalQuantity;
    private int filledQuantity;
    private double price;       // Limit price for LIMIT/STOP_LIMIT
    private double stopPrice;   // Trigger price for STOP/STOP_LIMIT
    private OrderStatus status;
    private final LocalDateTime timestamp;

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.accountId = builder.accountId;
        this.symbol = builder.symbol != null ? builder.symbol.toUpperCase().trim() : "";
        this.side = builder.side;
        this.type = builder.type;
        this.originalQuantity = builder.quantity;
        this.filledQuantity = builder.filledQuantity;
        this.price = builder.price;
        this.stopPrice = builder.stopPrice;
        this.status = builder.status != null ? builder.status : OrderStatus.OPEN;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public synchronized int getFilledQuantity() {
        return filledQuantity;
    }

    public synchronized int getRemainingQuantity() {
        return originalQuantity - filledQuantity;
    }

    public synchronized double getPrice() {
        return price;
    }

    public synchronized void setPrice(double price) {
        this.price = price;
    }

    public synchronized double getStopPrice() {
        return stopPrice;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public synchronized void fill(int quantity) {
        if (quantity <= 0) return;
        this.filledQuantity += quantity;
        if (this.filledQuantity >= this.originalQuantity) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public synchronized boolean isTerminal() {
        return status == OrderStatus.FILLED ||
                status == OrderStatus.CANCELLED ||
                status == OrderStatus.REJECTED ||
                status == OrderStatus.EXPIRED;
    }

    /**
     * Default comparator: sorts orders by creation timestamp.
     */
    @Override
    public int compareTo(Order other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Order[%s %s %d/%d %s @ %.2f status=%s]",
                orderId, side, filledQuantity, originalQuantity, symbol, price, status);
    }

    // ================= Builder Pattern =================

    public static class Builder {
        private String orderId;
        private int accountId;
        private String symbol;
        private OrderSide side;
        private OrderType type = OrderType.LIMIT;
        private int quantity;
        private int filledQuantity = 0;
        private double price = 0.0;
        private double stopPrice = 0.0;
        private OrderStatus status = OrderStatus.OPEN;
        private LocalDateTime timestamp = LocalDateTime.now();

        public Builder() {
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder accountId(int accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder side(OrderSide side) {
            this.side = side;
            return this;
        }

        public Builder type(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder filledQuantity(int filledQuantity) {
            this.filledQuantity = filledQuantity;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder stopPrice(double stopPrice) {
            this.stopPrice = stopPrice;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Order build() {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Order quantity must be positive");
            }
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Order symbol cannot be empty");
            }
            if (orderId == null) {
                this.orderId = "ORD-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 900 + 100);
            }
            return new Order(this);
        }
    }
}
