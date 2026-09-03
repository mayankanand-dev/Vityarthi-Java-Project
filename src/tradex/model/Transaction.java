package tradex.model;

import java.time.LocalDateTime;

/**
 * Audit ledger entry for cash movements (deposits, withdrawals, trades, brokerage).
 */
public class Transaction {
    private final int transactionId;
    private final int accountId;
    private final String type; // DEPOSIT, WITHDRAWAL, BUY_DEBIT, SELL_CREDIT, BROKERAGE
    private final double amount;
    private final double balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;

    public Transaction(int transactionId, int accountId, String type, double amount, double balanceAfter, String description, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public Transaction(int accountId, String type, double amount, double balanceAfter, String description) {
        this(0, accountId, type, amount, balanceAfter, description, LocalDateTime.now());
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] Account %d | %-12s | ₹%-9.2f | Balance: ₹%-9.2f | %s",
                timestamp, accountId, type, amount, balanceAfter, description);
    }
}
