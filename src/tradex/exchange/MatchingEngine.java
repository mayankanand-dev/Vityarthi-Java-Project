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

public class MatchingEngine {
    SettlementEngine settlementEngine;
    OrderRepository orderRepo;
    StockRepository stockRepo;

    public MatchingEngine(SettlementEngine settlementEngine, OrderRepository orderRepo, StockRepository stockRepo) {
        this.settlementEngine = settlementEngine;
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
    }

    public synchronized List<Trade> match(Order incomingOrder, OrderBook book, Stock stock) {
        List<Trade> trades = new ArrayList<>();

        // handle stop orders - only enter book when price hits trigger
        if (incomingOrder.getType() == OrderType.STOP || incomingOrder.getType() == OrderType.STOP_LIMIT) {
            double ltp = stock.getCurrentPrice();
            boolean sellTriggered = incomingOrder.getSide() == OrderSide.SELL && ltp <= incomingOrder.getStopPrice();
            boolean buyTriggered = incomingOrder.getSide() == OrderSide.BUY && ltp >= incomingOrder.getStopPrice();
            boolean triggered = sellTriggered || buyTriggered;
            if (!triggered) {
                book.addOrder(incomingOrder);
                try { orderRepo.save(incomingOrder); } catch (SQLException ignored) {}
                return trades;
            }
        }

        if (incomingOrder.getSide() == OrderSide.BUY) {
            matchBuyOrder(incomingOrder, book, stock, trades);
        } else {
            matchSellOrder(incomingOrder, book, stock, trades);
        }

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
            if (!bestAskOpt.isPresent()) break;

            Order restingAsk = bestAskOpt.get();

            if (buyOrder.getType() == OrderType.LIMIT && buyOrder.getPrice() < restingAsk.getPrice()) break;

            double execPrice = restingAsk.getPrice();
            int matchQty = Math.min(buyOrder.getRemainingQuantity(), restingAsk.getRemainingQuantity());

            buyOrder.fill(matchQty);
            restingAsk.fill(matchQty);

            if (restingAsk.getRemainingQuantity() == 0) book.pollBestAsk();

            double buyerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);
            double sellerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);

            Trade trade = new Trade(null, buyOrder.getOrderId(), restingAsk.getOrderId(),
                    buyOrder.getAccountId(), restingAsk.getAccountId(),
                    stock.getSymbol(), matchQty, execPrice, buyerFee, sellerFee, LocalDateTime.now());

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

        if (buyOrder.getRemainingQuantity() > 0 && buyOrder.getType() == OrderType.LIMIT) {
            book.addOrder(buyOrder);
        } else if (buyOrder.getRemainingQuantity() > 0 && buyOrder.getType() == OrderType.MARKET) {
            if (buyOrder.getFilledQuantity() > 0) {
                buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
            } else {
                buyOrder.setStatus(OrderStatus.CANCELLED);
            }
        }
    }

    private void matchSellOrder(Order sellOrder, OrderBook book, Stock stock, List<Trade> trades) {
        while (sellOrder.getRemainingQuantity() > 0) {
            Optional<Order> bestBidOpt = book.peekBestBid();
            if (!bestBidOpt.isPresent()) break;

            Order restingBid = bestBidOpt.get();

            if (sellOrder.getType() == OrderType.LIMIT && sellOrder.getPrice() > restingBid.getPrice()) break;

            double execPrice = restingBid.getPrice();
            int matchQty = Math.min(sellOrder.getRemainingQuantity(), restingBid.getRemainingQuantity());

            sellOrder.fill(matchQty);
            restingBid.fill(matchQty);

            if (restingBid.getRemainingQuantity() == 0) book.pollBestBid();

            double buyerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);
            double sellerFee = SettlementEngine.calculateBrokerage(matchQty * execPrice);

            Trade trade = new Trade(null, restingBid.getOrderId(), sellOrder.getOrderId(),
                    restingBid.getAccountId(), sellOrder.getAccountId(),
                    stock.getSymbol(), matchQty, execPrice, buyerFee, sellerFee, LocalDateTime.now());

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
            if (sellOrder.getFilledQuantity() > 0) {
                sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
            } else {
                sellOrder.setStatus(OrderStatus.CANCELLED);
            }
        }
    }
}
