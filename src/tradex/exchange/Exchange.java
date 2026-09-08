package tradex.exchange;

import tradex.exception.*;
import tradex.model.Account;
import tradex.model.Holding;
import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.Trade;
import tradex.model.enums.MarketStatus;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;
import tradex.repository.AccountRepository;
import tradex.repository.HoldingRepository;
import tradex.repository.OrderRepository;
import tradex.repository.StockRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Exchange {
    Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    MatchingEngine matchingEngine;
    StockRepository stockRepo;
    OrderRepository orderRepo;
    AccountRepository accountRepo;
    HoldingRepository holdingRepo;
    MarketStatus marketStatus = MarketStatus.OPEN;

    public Exchange(MatchingEngine matchingEngine, StockRepository stockRepo,
                    OrderRepository orderRepo, AccountRepository accountRepo,
                    HoldingRepository holdingRepo) {
        this.matchingEngine = matchingEngine;
        this.stockRepo = stockRepo;
        this.orderRepo = orderRepo;
        this.accountRepo = accountRepo;
        this.holdingRepo = holdingRepo;
        initializeBooks();
    }

    public void initializeBooks() {
        List<Stock> stocks = stockRepo.listAll();
        for (Stock s : stocks) {
            orderBooks.putIfAbsent(s.getSymbol(), new OrderBook(s.getSymbol()));
        }
    }

    public OrderBook getOrderBook(String symbol) {
        String sym = symbol.toUpperCase().trim();
        if (!orderBooks.containsKey(sym)) {
            orderBooks.put(sym, new OrderBook(sym));
        }
        return orderBooks.get(sym);
    }

    public MarketStatus getMarketStatus() { return marketStatus; }
    public void setMarketStatus(MarketStatus marketStatus) { this.marketStatus = marketStatus; }

    public synchronized List<Trade> submitOrder(Order order) throws TradeXException {
        if (marketStatus != MarketStatus.OPEN) {
            throw new MarketClosedException("TradeX Exchange is currently " + marketStatus);
        }

        String symbol = order.getSymbol();
        Optional<Stock> stockOpt = stockRepo.findBySymbol(symbol);
        if (!stockOpt.isPresent()) {
            throw new InvalidStockException("Stock " + symbol + " is not listed on TradeX.");
        }
        Stock stock = stockOpt.get();

        CircuitBreaker.checkMarketStatus(stock);

        if (order.getOriginalQuantity() <= 0) {
            throw new InvalidOrderException("Order quantity must be strictly greater than 0.");
        }

        if (order.getType() == OrderType.LIMIT) {
            if (order.getPrice() <= 0) throw new InvalidOrderException("Limit order price must be strictly positive.");
            CircuitBreaker.validatePriceBand(stock, order.getPrice());
        }

        // check buyer has enough cash
        if (order.getSide() == OrderSide.BUY) {
            Optional<Account> accountOpt = accountRepo.findById(order.getAccountId());
            if (!accountOpt.isPresent()) throw new TradeXException("Account " + order.getAccountId() + " not found.");
            Account account = accountOpt.get();

            double estimatedPrice;
            if (order.getType() == OrderType.MARKET) {
                estimatedPrice = stock.getCurrentPrice();
            } else {
                estimatedPrice = order.getPrice();
            }
            double requiredCash = (order.getOriginalQuantity() * estimatedPrice) +
                    SettlementEngine.calculateBrokerage(order.getOriginalQuantity() * estimatedPrice);

            if (account.getAvailableCash() < requiredCash) {
                throw new InsufficientFundsException(String.format(
                        "Insufficient funds. Required: Rs.%.2f, Available: Rs.%.2f", requiredCash, account.getAvailableCash()));
            }

            account.freezeCash(order.getOriginalQuantity() * estimatedPrice);
            try {
                accountRepo.updateBalances(account);
            } catch (SQLException e) {
                throw new TradeXException("Failed to update account balance state: " + e.getMessage());
            }
        }

        // check seller has enough shares
        if (order.getSide() == OrderSide.SELL) {
            Optional<Holding> holdingOpt = holdingRepo.findByAccountAndSymbol(order.getAccountId(), symbol);
            if (!holdingOpt.isPresent()) {
                throw new InsufficientHoldingsException("No holdings found for " + symbol);
            }
            Holding holding = holdingOpt.get();
            if (holding.getQuantity() < order.getOriginalQuantity()) {
                throw new InsufficientHoldingsException(String.format(
                        "Insufficient shares. Held: %d, Requested to sell: %d", holding.getQuantity(), order.getOriginalQuantity()));
            }
        }

        OrderBook book = getOrderBook(symbol);
        return matchingEngine.match(order, book, stock);
    }

    public synchronized boolean cancelOrder(String orderId, int accountId) throws TradeXException {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (!orderOpt.isPresent()) throw new InvalidOrderException("Order " + orderId + " does not exist.");

        Order order = orderOpt.get();
        if (order.getAccountId() != accountId) throw new TradeXException("Unauthorized: Cannot cancel another trader's order.");
        if (order.isTerminal()) throw new InvalidOrderException("Order " + orderId + " is already " + order.getStatus());

        OrderBook book = getOrderBook(order.getSymbol());
        boolean removed = book.cancelOrder(orderId);

        order.setStatus(OrderStatus.CANCELLED);
        try {
            orderRepo.updateOrderStatus(orderId, OrderStatus.CANCELLED, order.getFilledQuantity());
            if (order.getSide() == OrderSide.BUY) {
                Optional<Account> accOpt = accountRepo.findById(accountId);
                if (accOpt.isPresent()) {
                    Account acc = accOpt.get();
                    double remainingUnfilledCash = order.getRemainingQuantity() * order.getPrice();
                    acc.unfreezeCash(remainingUnfilledCash);
                    accountRepo.updateBalances(acc);
                }
            }
        } catch (SQLException e) {
            throw new TradeXException("Error finalizing order cancellation: " + e.getMessage());
        }

        return removed;
    }
}
