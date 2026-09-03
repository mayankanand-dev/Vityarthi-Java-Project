package tradex.exchange;

import tradex.model.Order;
import tradex.model.enums.OrderSide;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory double-auction Limit Order Book (LOB) for an individual equity symbol.
 * Maintained with strict Price-Time priority via dual PriorityQueues:
 * - Bids: Max-heap (highest price first; earlier timestamp breaks ties)
 * - Asks: Min-heap (lowest price first; earlier timestamp breaks ties)
 */
public class OrderBook {
    private final String symbol;
    private final PriorityQueue<Order> bids;
    private final PriorityQueue<Order> asks;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Comparator for Bids: Price DESC, Timestamp ASC
    private static final Comparator<Order> BID_COMPARATOR = (o1, o2) -> {
        int priceComp = Double.compare(o2.getPrice(), o1.getPrice());
        if (priceComp != 0) return priceComp;
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    };

    // Comparator for Asks: Price ASC, Timestamp ASC
    private static final Comparator<Order> ASK_COMPARATOR = (o1, o2) -> {
        int priceComp = Double.compare(o1.getPrice(), o2.getPrice());
        if (priceComp != 0) return priceComp;
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    };

    public OrderBook(String symbol) {
        this.symbol = symbol.toUpperCase().trim();
        this.bids = new PriorityQueue<>(BID_COMPARATOR);
        this.asks = new PriorityQueue<>(ASK_COMPARATOR);
    }

    public String getSymbol() {
        return symbol;
    }

    public void addOrder(Order order) {
        rwLock.writeLock().lock();
        try {
            if (order.getSide() == OrderSide.BUY) {
                bids.offer(order);
            } else {
                asks.offer(order);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public boolean cancelOrder(String orderId) {
        rwLock.writeLock().lock();
        try {
            boolean removed = bids.removeIf(o -> o.getOrderId().equals(orderId));
            if (!removed) {
                removed = asks.removeIf(o -> o.getOrderId().equals(orderId));
            }
            return removed;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Optional<Order> peekBestBid() {
        rwLock.readLock().lock();
        try {
            return Optional.ofNullable(bids.peek());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Optional<Order> peekBestAsk() {
        rwLock.readLock().lock();
        try {
            return Optional.ofNullable(asks.peek());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Order pollBestBid() {
        rwLock.writeLock().lock();
        try {
            return bids.poll();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Order pollBestAsk() {
        rwLock.writeLock().lock();
        try {
            return asks.poll();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public double getSpread() {
        rwLock.readLock().lock();
        try {
            Order bestBid = bids.peek();
            Order bestAsk = asks.peek();
            if (bestBid != null && bestAsk != null) {
                return Math.max(0.0, bestAsk.getPrice() - bestBid.getPrice());
            }
            return 0.0;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public static class LevelDepth {
        public final double price;
        public final int totalQuantity;
        public final int orderCount;

        public LevelDepth(double price, int totalQuantity, int orderCount) {
            this.price = price;
            this.totalQuantity = totalQuantity;
            this.orderCount = orderCount;
        }
    }

    /**
     * Aggregates order queue into price levels for market depth display.
     */
    public List<LevelDepth> getBidsDepth(int maxLevels) {
        rwLock.readLock().lock();
        try {
            Map<Double, int[]> aggregated = new TreeMap<>(Collections.reverseOrder());
            for (Order o : bids) {
                if (o.getRemainingQuantity() > 0) {
                    int[] curr = aggregated.computeIfAbsent(o.getPrice(), k -> new int[2]);
                    curr[0] += o.getRemainingQuantity(); // total quantity
                    curr[1] += 1;                       // count of orders
                }
            }
            List<LevelDepth> depth = new ArrayList<>();
            for (Map.Entry<Double, int[]> entry : aggregated.entrySet()) {
                if (depth.size() >= maxLevels) break;
                depth.add(new LevelDepth(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
            }
            return depth;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public List<LevelDepth> getAsksDepth(int maxLevels) {
        rwLock.readLock().lock();
        try {
            Map<Double, int[]> aggregated = new TreeMap<>();
            for (Order o : asks) {
                if (o.getRemainingQuantity() > 0) {
                    int[] curr = aggregated.computeIfAbsent(o.getPrice(), k -> new int[2]);
                    curr[0] += o.getRemainingQuantity();
                    curr[1] += 1;
                }
            }
            List<LevelDepth> depth = new ArrayList<>();
            for (Map.Entry<Double, int[]> entry : aggregated.entrySet()) {
                if (depth.size() >= maxLevels) break;
                depth.add(new LevelDepth(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
            }
            return depth;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getTotalBidQuantity() {
        rwLock.readLock().lock();
        try {
            return bids.stream().mapToInt(Order::getRemainingQuantity).sum();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getTotalAskQuantity() {
        rwLock.readLock().lock();
        try {
            return asks.stream().mapToInt(Order::getRemainingQuantity).sum();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
