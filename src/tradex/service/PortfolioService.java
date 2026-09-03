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
    private final HoldingRepository holdingRepo;
    private final StockRepository stockRepo;
    private final AccountRepository accountRepo;

    public PortfolioService(HoldingRepository holdingRepo, StockRepository stockRepo, AccountRepository accountRepo) {
        this.holdingRepo = holdingRepo;
        this.stockRepo = stockRepo;
        this.accountRepo = accountRepo;
    }

    public static class PositionView {
        public final String symbol;
        public final int quantity;
        public final double averagePrice;
        public final double currentPrice;
        public final double investedValue;
        public final double currentValue;
        public final double unrealizedPnL;
        public final double unrealizedPnLPct;
        public final double realizedPnL;

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
        public final int accountId;
        public final double cashBalance;
        public final double frozenCash;
        public final double availableCash;
        public final double totalInvested;
        public final double totalCurrentValue;
        public final double totalUnrealizedPnL;
        public final double totalRealizedPnL;
        public final double netWorth;
        public final List<PositionView> positions;

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
        Account account = accountRepo.findById(accountId)
                .orElse(new Account(accountId, 0, 0.0, 0.0, null));

        List<Holding> holdings = holdingRepo.listByAccountId(accountId);
        List<PositionView> views = new ArrayList<>();

        double totalInvested = 0.0;
        double totalCurrent = 0.0;
        double totalRealized = 0.0;

        for (Holding h : holdings) {
            Optional<Stock> sOpt = stockRepo.findBySymbol(h.getSymbol());
            double ltp = sOpt.map(Stock::getCurrentPrice).orElse(h.getAverageBuyPrice());

            double invested = h.getInvestedValue();
            double current = h.getCurrentValue(ltp);
            double unPnl = h.getUnrealizedPnL(ltp);
            double unPnlPct = h.getUnrealizedPnLPct(ltp);

            totalInvested += invested;
            totalCurrent += current;
            totalRealized += h.getRealizedPnL();

            views.add(new PositionView(
                    h.getSymbol(),
                    h.getQuantity(),
                    h.getAverageBuyPrice(),
                    ltp,
                    invested,
                    current,
                    unPnl,
                    unPnlPct,
                    h.getRealizedPnL()
            ));
        }

        double totalUnrealized = totalCurrent - totalInvested;
        double netWorth = account.getCashBalance() + totalCurrent;

        return new PortfolioSummary(
                accountId,
                account.getCashBalance(),
                account.getFrozenCash(),
                account.getAvailableCash(),
                totalInvested,
                totalCurrent,
                totalUnrealized,
                totalRealized,
                netWorth,
                views
        );
    }
}
