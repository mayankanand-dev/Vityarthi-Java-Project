package tradex.simulation;

import tradex.exchange.Exchange;
import tradex.model.Account;
import tradex.model.Holding;
import tradex.model.Order;
import tradex.model.Stock;
import tradex.repository.AccountRepository;
import tradex.repository.HoldingRepository;
import tradex.repository.StockRepository;
import tradex.strategy.TradingStrategy;

import java.util.List;
import java.util.Optional;

/**
 * Autonomous trading agent executing concurrently on background thread pools.
 * Interacts directly with the Exchange matching engine to simulate dynamic market activity.
 */
public class AutomatedTrader implements Runnable {
    private final String botName;
    private final int botAccountId;
    private final TradingStrategy strategy;
    private final Exchange exchange;
    private final StockRepository stockRepo;
    private final AccountRepository accountRepo;
    private final HoldingRepository holdingRepo;

    public AutomatedTrader(String botName, int botAccountId, TradingStrategy strategy,
                           Exchange exchange, StockRepository stockRepo,
                           AccountRepository accountRepo, HoldingRepository holdingRepo) {
        this.botName = botName;
        this.botAccountId = botAccountId;
        this.strategy = strategy;
        this.exchange = exchange;
        this.stockRepo = stockRepo;
        this.accountRepo = accountRepo;
        this.holdingRepo = holdingRepo;
    }

    public String getBotName() {
        return botName;
    }

    @Override
    public void run() {
        try {
            Optional<Account> accOpt = accountRepo.findById(botAccountId);
            if (accOpt.isEmpty()) return;
            Account account = accOpt.get();

            List<Stock> stocks = stockRepo.listAll();
            if (stocks.isEmpty()) return;

            // Pick a random stock or iterate over active stocks
            Stock stock = stocks.get((int) (Math.random() * stocks.size()));
            Optional<Holding> holdingOpt = holdingRepo.findByAccountAndSymbol(botAccountId, stock.getSymbol());
            int currentHolding = holdingOpt.map(Holding::getQuantity).orElse(0);

            Optional<Order> orderOpt = strategy.evaluate(stock, botAccountId, account.getAvailableCash(), currentHolding);
            if (orderOpt.isPresent()) {
                exchange.submitOrder(orderOpt.get());
            }
        } catch (Exception ignored) {
            // Background bot errors should never crash the main simulation thread
        }
    }
}
