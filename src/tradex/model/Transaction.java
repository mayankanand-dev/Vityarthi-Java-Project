package tradex.model;

import java.time.LocalDateTime;

public class Transaction {
    int transactionId;
    int accountId;
    String type; // DEPOSIT, WITHDRAWAL, TRADE_BUY, TRADE_SELL, BROKERAGE
    double amount;
    double balanceAfter;
    String description;
    LocalDateTime timestamp;

    public Transaction(int transactionId, int accountId, String type, double amount,
                       double balanceAfter, String description, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        if (timestamp != null) {
            this.timestamp = timestamp;
        } else {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Transaction(int accountId, String type, double amount, double balanceAfter, String description) {
        this(0, accountId, type, amount, balanceAfter, description, LocalDateTime.now());
    }

    public int getTransactionId() { return transactionId; }
    public int getAccountId() { return accountId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] Account " + accountId + " | " + type + " | Rs." + amount
                + " | Balance: Rs." + balanceAfter + " | " + description;
    }
}
