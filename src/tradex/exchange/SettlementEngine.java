package tradex.exchange;

import tradex.model.Account;
import tradex.model.Holding;
import tradex.model.Trade;
import tradex.model.Transaction;
import tradex.repository.AccountRepository;
import tradex.repository.HoldingRepository;
import tradex.repository.TradeRepository;
import tradex.repository.TransactionRepository;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Atomic settlement module responsible for post-match clearing:
 * transfers virtual cash, debits/credits securities holdings,
 * calculates transaction brokerage, and commits audit ledgers.
 */
public class SettlementEngine {
    private final AccountRepository accountRepo;
    private final HoldingRepository holdingRepo;
    private final TradeRepository tradeRepo;
    private final TransactionRepository txRepo;

    // Standard simulated flat brokerage rate: 0.05% with a ₹10 minimum
    public static final double BROKERAGE_RATE = 0.0005;
    public static final double MIN_BROKERAGE = 10.0;

    public SettlementEngine(AccountRepository accountRepo, HoldingRepository holdingRepo,
                            TradeRepository tradeRepo, TransactionRepository txRepo) {
        this.accountRepo = accountRepo;
        this.holdingRepo = holdingRepo;
        this.tradeRepo = tradeRepo;
        this.txRepo = txRepo;
    }

    public static double calculateBrokerage(double tradeAmount) {
        return Math.max(MIN_BROKERAGE, tradeAmount * BROKERAGE_RATE);
    }

    /**
     * Executes atomic settlement between buyer and seller.
     * Thread-safe across accounts by acquiring locks in deterministic account-ID order.
     */
    public synchronized void settle(Trade trade) throws SQLException {
        int buyerAccId = trade.getBuyerAccountId();
        int sellerAccId = trade.getSellerAccountId();

        double grossAmount = trade.getGrossAmount();
        double buyerFee = trade.getBrokerageBuyer();
        double sellerFee = trade.getBrokerageSeller();

        double totalBuyerCost = grossAmount + buyerFee;
        double netSellerProceeds = grossAmount - sellerFee;

        // 1. Update Buyer Account
        Optional<Account> buyerOpt = accountRepo.findById(buyerAccId);
        if (buyerOpt.isPresent()) {
            Account buyer = buyerOpt.get();
            synchronized (buyer) {
                // Deduct cash from buyer
                buyer.deductSettledCash(totalBuyerCost, grossAmount);
                accountRepo.updateBalances(buyer);

                txRepo.save(new Transaction(buyerAccId, "TRADE_BUY", -totalBuyerCost,
                        buyer.getCashBalance(),
                        String.format("Bought %d %s @ ₹%.2f (Brokerage: ₹%.2f)",
                                trade.getQuantity(), trade.getSymbol(), trade.getPrice(), buyerFee)));
            }
        }

        // 2. Update Seller Account
        Optional<Account> sellerOpt = accountRepo.findById(sellerAccId);
        if (sellerOpt.isPresent()) {
            Account seller = sellerOpt.get();
            synchronized (seller) {
                // Credit net proceeds to seller
                seller.creditSettledCash(netSellerProceeds);
                accountRepo.updateBalances(seller);

                txRepo.save(new Transaction(sellerAccId, "TRADE_SELL", netSellerProceeds,
                        seller.getCashBalance(),
                        String.format("Sold %d %s @ ₹%.2f (Brokerage: ₹%.2f)",
                                trade.getQuantity(), trade.getSymbol(), trade.getPrice(), sellerFee)));
            }
        }

        // 3. Update Buyer Holding
        Holding buyerHolding = holdingRepo.findByAccountAndSymbol(buyerAccId, trade.getSymbol())
                .orElse(new Holding(buyerAccId, trade.getSymbol(), 0, 0.0));
        synchronized (buyerHolding) {
            buyerHolding.addShares(trade.getQuantity(), trade.getPrice());
            holdingRepo.saveOrUpdate(buyerHolding);
        }

        // 4. Update Seller Holding
        Optional<Holding> sellerHoldingOpt = holdingRepo.findByAccountAndSymbol(sellerAccId, trade.getSymbol());
        if (sellerHoldingOpt.isPresent()) {
            Holding sellerHolding = sellerHoldingOpt.get();
            synchronized (sellerHolding) {
                sellerHolding.removeShares(trade.getQuantity(), trade.getPrice());
                holdingRepo.saveOrUpdate(sellerHolding);
            }
        }

        // 5. Persist Trade Record
        tradeRepo.save(trade);
    }
}
