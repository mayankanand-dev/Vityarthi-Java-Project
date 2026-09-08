package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Alert;
import tradex.model.enums.AlertType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertRepository {
    DatabaseManager dbManager;

    public AlertRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public AlertRepository() {
        this(DatabaseManager.getInstance());
    }

    public Alert save(Alert alert) throws SQLException {
        String sql = "INSERT INTO alerts (account_id, symbol, type, target_value, triggered, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, alert.getAccountId());
            stmt.setString(2, alert.getSymbol());
            stmt.setString(3, alert.getType().name());
            stmt.setDouble(4, alert.getTargetValue());
            stmt.setInt(5, alert.isTriggered() ? 1 : 0);
            stmt.setString(6, alert.getCreatedAt().toString());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Alert(id, alert.getAccountId(), alert.getSymbol(), alert.getType(), alert.getTargetValue(), alert.isTriggered(), alert.getCreatedAt());
                }
            }
        }
        return alert;
    }

    public List<Alert> listActiveBySymbol(String symbol) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE UPPER(symbol) = UPPER(?) AND triggered = 0";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, symbol.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AlertRepository] listActiveBySymbol error: " + e.getMessage());
        }
        return list;
    }

    public List<Alert> listByAccountId(int accountId) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE account_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AlertRepository] listByAccountId error: " + e.getMessage());
        }
        return list;
    }

    public void markTriggered(int alertId) throws SQLException {
        String sql = "UPDATE alerts SET triggered = 1 WHERE alert_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alertId);
            stmt.executeUpdate();
        }
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        return new Alert(
                rs.getInt("alert_id"),
                rs.getInt("account_id"),
                rs.getString("symbol"),
                AlertType.valueOf(rs.getString("type")),
                rs.getDouble("target_value"),
                rs.getInt("triggered") == 1,
                LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}

