package tradex.model;

import java.time.LocalDateTime;

public class Account {
    private final int accountId;
    private final int userId;
    private double cashBalance;
    private double frozenCash;
    private final LocalDateTime createdAt;

    public Account(int accountId, int userId, double cashBalance, double frozenCash, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.cashBalance = cashBalance;
        this.frozenCash = frozenCash;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Account(int userId, double initialCash) {
        this(0, userId, initialCash, 0.0, LocalDateTime.now());
    }

    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public synchronized double getCashBalance() {
        return cashBalance;
    }

    public synchronized double getFrozenCash() {
        return frozenCash;
    }

    public synchronized double getAvailableCash() {
        return Math.max(0.0, cashBalance - frozenCash);
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }
        this.cashBalance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
        }
        if (getAvailableCash() < amount) {
            throw new IllegalStateException("Insufficient available funds for withdrawal");
        }
        this.cashBalance -= amount;
    }

    public synchronized void freezeCash(double amount) {
        if (amount <= 0) return;
        if (getAvailableCash() < amount) {
            throw new IllegalStateException("Cannot freeze more cash than available balance");
        }
        this.frozenCash += amount;
    }

    public synchronized void unfreezeCash(double amount) {
        if (amount <= 0) return;
        this.frozenCash = Math.max(0.0, this.frozenCash - amount);
    }

    public synchronized void deductSettledCash(double amount, double previouslyFrozen) {
        unfreezeCash(previouslyFrozen);
        this.cashBalance -= amount;
    }

    public synchronized void creditSettledCash(double amount) {
        this.cashBalance += amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Account[id=%d, userId=%d, totalCash=%.2f, frozen=%.2f, available=%.2f]",
                accountId, userId, cashBalance, frozenCash, getAvailableCash());
    }
}
