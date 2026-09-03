package tradex.exchange;

import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.Trade;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;
import tradex.repository.OrderRepository;
import tradex.repository.StockRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Continuous double-auction Matching Engine.
 * Evaluates orders against the OrderBook using price-time priority,
 * executes trades, updates stock market statistics, and triggers settlement.
 */
public class MatchingEngine {
    private final SettlementEngine settlementEngine;
    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;

    public MatchingEngine(SettlementEngine settlementEngine, OrderRepository orderRepo, StockRepository stockRepo) {
        this.settlementEngine = settlementEngine;
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
    }

    /**
     * Ingests an incoming order and matches it against resting orders in the book.
     * Any unfilled remainder of a limit order is placed into the order book.
     */
    public synchronized List<Trade> match(Order incomingOrder, OrderBook book, Stock stock) {
        List<Trade> trades = new ArrayList<>();

        // Handle Stop / Stop-Limit pre-checks
        if (incomingOrder.getType() == OrderType.STOP || incomingOrder.getType() == OrderType.STOP_LIMIT) {
            double ltp = stock.getCurrentPrice();
            boolean triggered = (incomingOrder.getSide() == OrderSide.SELL && ltp <= incomingOrder.getStopPrice()) ||
                    (incomingOrder.getSide() == OrderSide.BUY && ltp >= incomingOrder.getStopPrice());
            if (!triggered) {
                // Not yet triggered; keep resting until price activates
                book.addOrder(incomingOrder);
                try {
                    orderRepo.save(incomingOrder);
                } catch (SQLException ignored) {}
                return trades;
            }
        }

        if (incomingOrder.getSide() == OrderSide.BUY) {
            matchBuyOrder(incomingOrder, book, stock, trades);
        } else {
            matchSellOrder(incomingOrder, book, stock, trades);
        }

        // Persist final order state
        try {
            orderRepo.save(incomingOrder);
        } catch (SQLException e) {
            System.err.println("[MatchingEngine] Failed to persist incoming order state: " + e.getMessage());
        }

        return trades;
    }

    private void matchBuyOrder(Order buyOrder, OrderBook book, Stock stock, List<Trade> trades) {
        while (buyOrder.getRemainingQuantity() > 0) {
            Optional<Order> bestAskOpt = book.peekBestAsk();
            if (bestAskOpt.isEmpty()) {
                break; // No resting sell orders available
            }

            Order restingAsk = bestAskOpt.get();

            // Price cross check for limit orders
            if (buyOrder.getType() == OrderType.LIMIT && buyOrder.getPrice() < restingAsk.getPrice()) {
                break; // Buyer willing to pay less than lowest asking price
            }

            // A match is found! Maker price (resting order's price) is the execution price
            double execPrice = restingAsk.getPrice();
            int matchQty = Math.min(buyOrder.getRemainingQuantity(), restingAsk.getRemainingQuantity());

            // Execute fill on both orders
            buyOrder.fill(matchQty);
            restingAsk.fill(matchQty);

            // If resting ask is completely filled, remove from order book
            if (restingAsk.getRemainingQuantity() == 0) {
                book.pollBestAsk();
            }

            // Create and execute trade
            double buyerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);
            double sellerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);

            Trade trade = new Trade(
                    null,
                    buyOrder.getOrderId(),
                    restingAsk.getOrderId(),
                    buyOrder.getAccountId(),
                    restingAsk.getAccountId(),
                    stock.getSymbol(),
                    matchQty,
                    execPrice,
                    buyerFee,
                    sellerFee,
                    LocalDateTime.now()
            );

            try {
                settlementEngine.settle(trade);
                orderRepo.updateOrderStatus(restingAsk.getOrderId(), restingAsk.getStatus(), restingAsk.getFilledQuantity());
                stock.updatePrice(execPrice, matchQty);
                stockRepo.updatePriceAndVolume(stock);
            } catch (SQLException e) {
                System.err.println("[MatchingEngine] Error during settlement/update: " + e.getMessage());
            }

            trades.add(trade);
        }

        // If limit order still has unfilled quantity, place it in the book
        if (buyOrder.getRemainingQuantity() > 0 && buyOrder.getType() == OrderType.LIMIT) {
            book.addOrder(buyOrder);
        } else if (buyOrder.getRemainingQuantity() > 0 && buyOrder.getType() == OrderType.MARKET) {
            // Market order remainder with no opposing liquidity is cancelled/expired
            buyOrder.setStatus(buyOrder.getFilledQuantity() > 0 ? OrderStatus.PARTIALLY_FILLED : OrderStatus.CANCELLED);
        }
    }

    private void matchSellOrder(Order sellOrder, OrderBook book, Stock stock, List<Trade> trades) {
        while (sellOrder.getRemainingQuantity() > 0) {
            Optional<Order> bestBidOpt = book.peekBestBid();
            if (bestBidOpt.isEmpty()) {
                break; // No resting buy orders available
            }

            Order restingBid = bestBidOpt.get();

            // Price cross check for limit orders
            if (sellOrder.getType() == OrderType.LIMIT && sellOrder.getPrice() > restingBid.getPrice()) {
                break; // Seller asking more than highest bid price
            }

            double execPrice = restingBid.getPrice();
            int matchQty = Math.min(sellOrder.getRemainingQuantity(), restingBid.getRemainingQuantity());

            sellOrder.fill(matchQty);
            restingBid.fill(matchQty);

            if (restingBid.getRemainingQuantity() == 0) {
                book.pollBestBid();
            }

            double buyerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);
            double sellerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);

            Trade trade = new Trade(
                    null,
                    restingBid.getOrderId(),
                    sellOrder.getOrderId(),
                    restingBid.getAccountId(),
                    sellOrder.getAccountId(),
                    stock.getSymbol(),
                    matchQty,
                    execPrice,
                    buyerFee,
                    sellerFee,
                    LocalDateTime.now()
            );

            try {
                settlementEngine.settle(trade);
                orderRepo.updateOrderStatus(restingBid.getOrderId(), restingBid.getStatus(), restingBid.getFilledQuantity());
                stock.updatePrice(execPrice, matchQty);
                stockRepo.updatePriceAndVolume(stock);
            } catch (SQLException e) {
                System.err.println("[MatchingEngine] Error during settlement/update: " + e.getMessage());
            }

            trades.add(trade);
        }

        if (sellOrder.getRemainingQuantity() > 0 && sellOrder.getType() == OrderType.LIMIT) {
            book.addOrder(sellOrder);
        } else if (sellOrder.getRemainingQuantity() > 0 && sellOrder.getType() == OrderType.MARKET) {
            sellOrder.setStatus(sellOrder.getFilledQuantity() > 0 ? OrderStatus.PARTIALLY_FILLED : OrderStatus.CANCELLED);
        }
    }
}
