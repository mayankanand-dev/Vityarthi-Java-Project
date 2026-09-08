package tradex.service;

import tradex.model.Alert;
import tradex.model.Stock;
import tradex.model.enums.AlertType;
import tradex.repository.AlertRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertService {
    AlertRepository alertRepo;

    public AlertService(AlertRepository alertRepo) {
        this.alertRepo = alertRepo;
    }

    public Alert createAlert(int accountId, String symbol, AlertType type, double targetValue) throws SQLException {
        Alert alert = new Alert(accountId, symbol, type, targetValue);
        return alertRepo.save(alert);
    }

    public List<Alert> getUserAlerts(int accountId) {
        return alertRepo.listByAccountId(accountId);
    }

    // check all active alerts for a stock and trigger if condition is met
    public List<Alert> checkAndTriggerAlerts(Stock stock) {
        List<Alert> triggeredAlerts = new ArrayList<>();
        List<Alert> activeAlerts = alertRepo.listActiveBySymbol(stock.getSymbol());

        for (Alert alert : activeAlerts) {
            boolean triggered = false;

            if (alert.getType() == AlertType.PRICE_ABOVE) {
                if (stock.getCurrentPrice() >= alert.getTargetValue()) triggered = true;
            } else if (alert.getType() == AlertType.PRICE_BELOW) {
                if (stock.getCurrentPrice() <= alert.getTargetValue()) triggered = true;
            } else if (alert.getType() == AlertType.PCT_CHANGE) {
                if (Math.abs(stock.getChangePercentage()) >= alert.getTargetValue()) triggered = true;
            } else if (alert.getType() == AlertType.VOLUME_ABOVE) {
                if (stock.getVolume() >= (long) alert.getTargetValue()) triggered = true;
            }

            if (triggered) {
                alert.setTriggered(true);
                try {
                    alertRepo.markTriggered(alert.getAlertId());
                    triggeredAlerts.add(alert);
                } catch (SQLException e) {
                    System.err.println("[AlertService] Error marking alert triggered: " + e.getMessage());
                }
            }
        }
        return triggeredAlerts;
    }
}
