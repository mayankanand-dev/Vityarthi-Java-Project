package tradex.model;

import tradex.model.enums.AlertType;
import java.time.LocalDateTime;

public class Alert {
    int alertId;
    int accountId;
    String symbol;
    AlertType type;
    double targetValue;
    boolean triggered;
    LocalDateTime createdAt;

    public Alert(int alertId, int accountId, String symbol, AlertType type, double targetValue, boolean triggered, LocalDateTime createdAt) {
        this.alertId = alertId;
        this.accountId = accountId;
        this.symbol = symbol.toUpperCase().trim();
        this.type = type;
        this.targetValue = targetValue;
        this.triggered = triggered;
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        } else {
            this.createdAt = createdAt;
        }
    }

    public Alert(int accountId, String symbol, AlertType type, double targetValue) {
        this(0, accountId, symbol, type, targetValue, false, LocalDateTime.now());
    }

    public int getAlertId() { return alertId; }
    public int getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public AlertType getType() { return type; }
    public double getTargetValue() { return targetValue; }
    public synchronized boolean isTriggered() { return triggered; }
    public synchronized void setTriggered(boolean triggered) { this.triggered = triggered; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Alert[#" + alertId + " " + symbol + " " + type + " " + targetValue + " triggered=" + triggered + "]";
    }
}
