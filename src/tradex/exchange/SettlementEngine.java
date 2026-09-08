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

public class SettlementEngine {
    AccountRepository accountRepo;
    HoldingRepository holdingRepo;
    TradeRepository tradeRepo;
    TransactionRepository txRepo;

    // brokerage is 0.05% with minimum Rs.10
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

    public synchronized void settle(Trade trade) throws SQLException {
        int buyerAccId = trade.getBuyerAccountId();
        int sellerAccId = trade.getSellerAccountId();

        double grossAmount = trade.getGrossAmount();
        double buyerFee = trade.getBrokerageBuyer();
        double sellerFee = trade.getBrokerageSeller();
        double totalBuyerCost = grossAmount + buyerFee;
        double netSellerProceeds = grossAmount - sellerFee;

        // deduct from buyer
        Optional<Account> buyerOpt = accountRepo.findById(buyerAccId);
        if (buyerOpt.isPresent()) {
            Account buyer = buyerOpt.get();
            synchronized (buyer) {
                buyer.deductSettledCash(totalBuyerCost, grossAmount);
                accountRepo.updateBalances(buyer);
                txRepo.save(new Transaction(buyerAccId, "TRADE_BUY", -totalBuyerCost,
                        buyer.getCashBalance(),
                        String.format("Bought %d %s @ Rs.%.2f (Brokerage: Rs.%.2f)",
                                trade.getQuantity(), trade.getSymbol(), trade.getPrice(), buyerFee)));
            }
        }

        // credit to seller
        Optional<Account> sellerOpt = accountRepo.findById(sellerAccId);
        if (sellerOpt.isPresent()) {
            Account seller = sellerOpt.get();
            synchronized (seller) {
                seller.creditSettledCash(netSellerProceeds);
                accountRepo.updateBalances(seller);
                txRepo.save(new Transaction(sellerAccId, "TRADE_SELL", netSellerProceeds,
                        seller.getCashBalance(),
                        String.format("Sold %d %s @ Rs.%.2f (Brokerage: Rs.%.2f)",
                                trade.getQuantity(), trade.getSymbol(), trade.getPrice(), sellerFee)));
            }
        }

        // update buyer's holdings
        Optional<Holding> buyerHoldingOpt = holdingRepo.findByAccountAndSymbol(buyerAccId, trade.getSymbol());
        Holding buyerHolding;
        if (buyerHoldingOpt.isPresent()) {
            buyerHolding = buyerHoldingOpt.get();
        } else {
            buyerHolding = new Holding(buyerAccId, trade.getSymbol(), 0, 0.0);
        }
        synchronized (buyerHolding) {
            buyerHolding.addShares(trade.getQuantity(), trade.getPrice());
            holdingRepo.saveOrUpdate(buyerHolding);
        }

        // update seller's holdings
        Optional<Holding> sellerHoldingOpt = holdingRepo.findByAccountAndSymbol(sellerAccId, trade.getSymbol());
        if (sellerHoldingOpt.isPresent()) {
            Holding sellerHolding = sellerHoldingOpt.get();
            synchronized (sellerHolding) {
                sellerHolding.removeShares(trade.getQuantity(), trade.getPrice());
                holdingRepo.saveOrUpdate(sellerHolding);
            }
        }

        tradeRepo.save(trade);
    }
}
