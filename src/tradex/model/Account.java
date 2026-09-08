package tradex.model;

import java.time.LocalDateTime;

public class Account {
    int accountId;
    int userId;
    double cashBalance;
    double frozenCash;
    LocalDateTime createdAt;

    public Account(int accountId, int userId, double cashBalance, double frozenCash, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.cashBalance = cashBalance;
        this.frozenCash = frozenCash;
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        } else {
            this.createdAt = createdAt;
        }
    }

    public Account(int userId, double initialCash) {
        this(0, userId, initialCash, 0.0, LocalDateTime.now());
    }

    public int getAccountId() { return accountId; }
    public int getUserId() { return userId; }

    public synchronized double getCashBalance() { return cashBalance; }
    public synchronized double getFrozenCash() { return frozenCash; }

    // available = total - frozen
    public synchronized double getAvailableCash() {
        double available = cashBalance - frozenCash;
        if (available < 0) return 0.0;
        return available;
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be strictly positive");
        this.cashBalance = this.cashBalance + amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
        if (getAvailableCash() < amount) throw new IllegalStateException("Insufficient available funds for withdrawal");
        this.cashBalance = this.cashBalance - amount;
    }

    public synchronized void freezeCash(double amount) {
        if (amount <= 0) return;
        if (getAvailableCash() < amount) throw new IllegalStateException("Cannot freeze more cash than available balance");
        this.frozenCash = this.frozenCash + amount;
    }

    public synchronized void unfreezeCash(double amount) {
        if (amount <= 0) return;
        this.frozenCash = this.frozenCash - amount;
        if (this.frozenCash < 0) this.frozenCash = 0;
    }

    public synchronized void deductSettledCash(double amount, double previouslyFrozen) {
        unfreezeCash(previouslyFrozen);
        this.cashBalance = this.cashBalance - amount;
    }

    public synchronized void creditSettledCash(double amount) {
        this.cashBalance = this.cashBalance + amount;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Account[id=" + accountId + ", userId=" + userId + ", totalCash=" + cashBalance
                + ", frozen=" + frozenCash + ", available=" + getAvailableCash() + "]";
    }
}
