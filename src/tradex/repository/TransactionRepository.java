package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    DatabaseManager dbManager;

    public TransactionRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public TransactionRepository() {
        this(DatabaseManager.getInstance());
    }

    public void save(Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, balance_after, description, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tx.getAccountId());
            stmt.setString(2, tx.getType());
            stmt.setDouble(3, tx.getAmount());
            stmt.setDouble(4, tx.getBalanceAfter());
            stmt.setString(5, tx.getDescription());
            stmt.setString(6, tx.getTimestamp().toString());
            stmt.executeUpdate();
        }
    }

    public List<Transaction> listByAccountId(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionRepository] listByAccountId error: " + e.getMessage());
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("account_id"),
                rs.getString("type"),
                rs.getDouble("amount"),
                rs.getDouble("balance_after"),
                rs.getString("description"),
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }
}

