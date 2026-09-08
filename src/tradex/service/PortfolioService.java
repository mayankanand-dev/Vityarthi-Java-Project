package tradex.service;

import tradex.model.Account;
import tradex.model.Holding;
import tradex.model.Stock;
import tradex.repository.AccountRepository;
import tradex.repository.HoldingRepository;
import tradex.repository.StockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PortfolioService {
    HoldingRepository holdingRepo;
    StockRepository stockRepo;
    AccountRepository accountRepo;

    public PortfolioService(HoldingRepository holdingRepo, StockRepository stockRepo, AccountRepository accountRepo) {
        this.holdingRepo = holdingRepo;
        this.stockRepo = stockRepo;
        this.accountRepo = accountRepo;
    }

    public static class PositionView {
        public String symbol;
        public int quantity;
        public double averagePrice;
        public double currentPrice;
        public double investedValue;
        public double currentValue;
        public double unrealizedPnL;
        public double unrealizedPnLPct;
        public double realizedPnL;

        public PositionView(String symbol, int quantity, double averagePrice, double currentPrice,
                            double investedValue, double currentValue, double unrealizedPnL,
                            double unrealizedPnLPct, double realizedPnL) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.averagePrice = averagePrice;
            this.currentPrice = currentPrice;
            this.investedValue = investedValue;
            this.currentValue = currentValue;
            this.unrealizedPnL = unrealizedPnL;
            this.unrealizedPnLPct = unrealizedPnLPct;
            this.realizedPnL = realizedPnL;
        }
    }

    public static class PortfolioSummary {
        public int accountId;
        public double cashBalance;
        public double frozenCash;
        public double availableCash;
        public double totalInvested;
        public double totalCurrentValue;
        public double totalUnrealizedPnL;
        public double totalRealizedPnL;
        public double netWorth;
        public List<PositionView> positions;

        public PortfolioSummary(int accountId, double cashBalance, double frozenCash, double availableCash,
                                double totalInvested, double totalCurrentValue, double totalUnrealizedPnL,
                                double totalRealizedPnL, double netWorth, List<PositionView> positions) {
            this.accountId = accountId;
            this.cashBalance = cashBalance;
            this.frozenCash = frozenCash;
            this.availableCash = availableCash;
            this.totalInvested = totalInvested;
            this.totalCurrentValue = totalCurrentValue;
            this.totalUnrealizedPnL = totalUnrealizedPnL;
            this.totalRealizedPnL = totalRealizedPnL;
            this.netWorth = netWorth;
            this.positions = positions;
        }
    }

    public PortfolioSummary getPortfolioSummary(int accountId) {
        Optional<Account> accountOpt = accountRepo.findById(accountId);
        Account account;
        if (accountOpt.isPresent()) {
            account = accountOpt.get();
        } else {
            account = new Account(accountId, 0, 0.0, 0.0, null);
        }

        List<Holding> holdings = holdingRepo.listByAccountId(accountId);
        List<PositionView> views = new ArrayList<>();

        double totalInvested = 0.0;
        double totalCurrent = 0.0;
        double totalRealized = 0.0;

        for (Holding h : holdings) {
            Optional<Stock> stockOpt = stockRepo.findBySymbol(h.getSymbol());
            double ltp;
            if (stockOpt.isPresent()) {
                ltp = stockOpt.get().getCurrentPrice();
            } else {
                ltp = h.getAverageBuyPrice();
            }

            double invested = h.getInvestedValue();
            double current = h.getCurrentValue(ltp);
            double unPnl = h.getUnrealizedPnL(ltp);
            double unPnlPct = h.getUnrealizedPnLPct(ltp);

            totalInvested = totalInvested + invested;
            totalCurrent = totalCurrent + current;
            totalRealized = totalRealized + h.getRealizedPnL();

            views.add(new PositionView(h.getSymbol(), h.getQuantity(), h.getAverageBuyPrice(),
                    ltp, invested, current, unPnl, unPnlPct, h.getRealizedPnL()));
        }

        double totalUnrealized = totalCurrent - totalInvested;
        double netWorth = account.getCashBalance() + totalCurrent;

        return new PortfolioSummary(accountId, account.getCashBalance(), account.getFrozenCash(),
                account.getAvailableCash(), totalInvested, totalCurrent, totalUnrealized, totalRealized, netWorth, views);
    }
}
