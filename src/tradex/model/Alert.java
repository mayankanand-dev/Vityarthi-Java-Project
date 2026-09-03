package tradex.model;

import tradex.model.enums.AlertType;

import java.time.LocalDateTime;

/**
 * User-configured price or volume trigger condition.
 */
public class Alert {
    private final int alertId;
    private final int accountId;
    private final String symbol;
    private final AlertType type;
    private final double targetValue;
    private boolean triggered;
    private final LocalDateTime createdAt;

    public Alert(int alertId, int accountId, String symbol, AlertType type, double targetValue, boolean triggered, LocalDateTime createdAt) {
        this.alertId = alertId;
        this.accountId = accountId;
        this.symbol = symbol.toUpperCase().trim();
        this.type = type;
        this.targetValue = targetValue;
        this.triggered = triggered;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Alert(int accountId, String symbol, AlertType type, double targetValue) {
        this(0, accountId, symbol, type, targetValue, false, LocalDateTime.now());
    }

    public int getAlertId() {
        return alertId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public AlertType getType() {
        return type;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public synchronized boolean isTriggered() {
        return triggered;
    }

    public synchronized void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Alert[#%d %s %s %.2f triggered=%s]",
                alertId, symbol, type, targetValue, triggered);
    }
}
