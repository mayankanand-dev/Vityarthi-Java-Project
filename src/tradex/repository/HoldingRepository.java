package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Holding;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HoldingRepository {
    DatabaseManager dbManager;

    public HoldingRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public HoldingRepository() {
        this(DatabaseManager.getInstance());
    }

    public void saveOrUpdate(Holding holding) throws SQLException {
        String sql = "INSERT INTO holdings (account_id, symbol, quantity, average_buy_price, realized_pnl) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(account_id, symbol) DO UPDATE SET " +
                "quantity = excluded.quantity, " +
                "average_buy_price = excluded.average_buy_price, " +
                "realized_pnl = excluded.realized_pnl";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, holding.getAccountId());
            stmt.setString(2, holding.getSymbol());
            stmt.setInt(3, holding.getQuantity());
            stmt.setDouble(4, holding.getAverageBuyPrice());
            stmt.setDouble(5, holding.getRealizedPnL());
            stmt.executeUpdate();
        }
    }

    public Optional<Holding> findByAccountAndSymbol(int accountId, String symbol) {
        String sql = "SELECT * FROM holdings WHERE account_id = ? AND UPPER(symbol) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, symbol.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[HoldingRepository] findByAccountAndSymbol error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Holding> listByAccountId(int accountId) {
        List<Holding> list = new ArrayList<>();
        String sql = "SELECT * FROM holdings WHERE account_id = ? AND quantity > 0 ORDER BY symbol ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[HoldingRepository] listByAccountId error: " + e.getMessage());
        }
        return list;
    }

    private Holding mapRow(ResultSet rs) throws SQLException {
        return new Holding(
                rs.getInt("holding_id"),
                rs.getInt("account_id"),
                rs.getString("symbol"),
                rs.getInt("quantity"),
                rs.getDouble("average_buy_price"),
                rs.getDouble("realized_pnl")
        );
    }
}

