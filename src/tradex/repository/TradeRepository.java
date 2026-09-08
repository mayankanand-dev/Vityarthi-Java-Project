package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Trade;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeRepository {
    DatabaseManager dbManager;

    public TradeRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public TradeRepository() {
        this(DatabaseManager.getInstance());
    }

    public void save(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades (" +
                "trade_id, buy_order_id, sell_order_id, buyer_account_id, " +
                "seller_account_id, symbol, quantity, price, brokerage_buyer, " +
                "brokerage_seller, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trade.getTradeId());
            stmt.setString(2, trade.getBuyOrderId());
            stmt.setString(3, trade.getSellOrderId());
            stmt.setInt(4, trade.getBuyerAccountId());
            stmt.setInt(5, trade.getSellerAccountId());
            stmt.setString(6, trade.getSymbol());
            stmt.setInt(7, trade.getQuantity());
            stmt.setDouble(8, trade.getPrice());
            stmt.setDouble(9, trade.getBrokerageBuyer());
            stmt.setDouble(10, trade.getBrokerageSeller());
            stmt.setString(11, trade.getTimestamp().toString());
            stmt.executeUpdate();
        }
    }

    public List<Trade> findByAccountId(int accountId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades WHERE buyer_account_id = ? OR seller_account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trades.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TradeRepository] findByAccountId error: " + e.getMessage());
        }
        return trades;
    }

    public List<Trade> findBySymbol(String symbol) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades WHERE UPPER(symbol) = UPPER(?) ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, symbol.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trades.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TradeRepository] findBySymbol error: " + e.getMessage());
        }
        return trades;
    }

    public List<Trade> listAll() {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trades.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TradeRepository] listAll error: " + e.getMessage());
        }
        return trades;
    }

    private Trade mapRow(ResultSet rs) throws SQLException {
        return new Trade(
                rs.getString("trade_id"),
                rs.getString("buy_order_id"),
                rs.getString("sell_order_id"),
                rs.getInt("buyer_account_id"),
                rs.getInt("seller_account_id"),
                rs.getString("symbol"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                rs.getDouble("brokerage_buyer"),
                rs.getDouble("brokerage_seller"),
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }
}

