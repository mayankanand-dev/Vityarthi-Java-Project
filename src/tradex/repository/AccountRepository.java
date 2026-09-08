package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Account;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class AccountRepository {
    DatabaseManager dbManager;

    public AccountRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public AccountRepository() {
        this(DatabaseManager.getInstance());
    }

    public Account save(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, cash_balance, frozen_cash, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, account.getUserId());
            stmt.setDouble(2, account.getCashBalance());
            stmt.setDouble(3, account.getFrozenCash());
            stmt.setString(4, account.getCreatedAt().toString());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    return new Account(accountId, account.getUserId(), account.getCashBalance(), account.getFrozenCash(), account.getCreatedAt());
                }
            }
        }
        return account;
    }

    public Optional<Account> findById(int accountId) {
        String sql = "SELECT account_id, user_id, cash_balance, frozen_cash, created_at FROM accounts WHERE account_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountRepository] findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Account> findByUserId(int userId) {
        String sql = "SELECT account_id, user_id, cash_balance, frozen_cash, created_at FROM accounts WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountRepository] findByUserId error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateBalances(Account account) throws SQLException {
        String sql = "UPDATE accounts SET cash_balance = ?, frozen_cash = ? WHERE account_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, account.getCashBalance());
            stmt.setDouble(2, account.getFrozenCash());
            stmt.setInt(3, account.getAccountId());
            stmt.executeUpdate();
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("account_id"),
                rs.getInt("user_id"),
                rs.getDouble("cash_balance"),
                rs.getDouble("frozen_cash"),
                LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}

