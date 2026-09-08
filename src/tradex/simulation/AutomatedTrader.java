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

public class AutomatedTrader implements Runnable {
    String botName;
    int botAccountId;
    TradingStrategy strategy;
    Exchange exchange;
    StockRepository stockRepo;
    AccountRepository accountRepo;
    HoldingRepository holdingRepo;

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

    public String getBotName() { return botName; }

    @Override
    public void run() {
        try {
            Optional<Account> accOpt = accountRepo.findById(botAccountId);
            if (!accOpt.isPresent()) return;
            Account account = accOpt.get();

            List<Stock> stocks = stockRepo.listAll();
            if (stocks.isEmpty()) return;

            Stock stock = stocks.get((int) (Math.random() * stocks.size()));
            Optional<Holding> holdingOpt = holdingRepo.findByAccountAndSymbol(botAccountId, stock.getSymbol());
            int currentHolding = 0;
            if (holdingOpt.isPresent()) currentHolding = holdingOpt.get().getQuantity();

            Optional<Order> orderOpt = strategy.evaluate(stock, botAccountId, account.getAvailableCash(), currentHolding);
            if (orderOpt.isPresent()) {
                exchange.submitOrder(orderOpt.get());
            }
        } catch (Exception ignored) {
            // bot errors should not crash the simulation
        }
    }
}
