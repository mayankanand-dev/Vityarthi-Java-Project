package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Order;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {
    DatabaseManager dbManager;

    public OrderRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public OrderRepository() {
        this(DatabaseManager.getInstance());
    }

    public void save(Order order) throws SQLException {
        String sql = "INSERT OR REPLACE INTO orders (" +
                "order_id, account_id, symbol, side, type, original_quantity, " +
                "filled_quantity, price, stop_price, status, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.getOrderId());
            stmt.setInt(2, order.getAccountId());
            stmt.setString(3, order.getSymbol());
            stmt.setString(4, order.getSide().name());
            stmt.setString(5, order.getType().name());
            stmt.setInt(6, order.getOriginalQuantity());
            stmt.setInt(7, order.getFilledQuantity());
            stmt.setDouble(8, order.getPrice());
            stmt.setDouble(9, order.getStopPrice());
            stmt.setString(10, order.getStatus().name());
            stmt.setString(11, order.getTimestamp().toString());
            stmt.executeUpdate();
        }
    }

    public Optional<Order> findById(String orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderRepository] findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Order> findByAccountId(int accountId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderRepository] findByAccountId error: " + e.getMessage());
        }
        return list;
    }

    public List<Order> findOpenOrdersByAccountId(int accountId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE account_id = ? AND status IN ('OPEN', 'PARTIALLY_FILLED') ORDER BY timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderRepository] findOpenOrdersByAccountId error: " + e.getMessage());
        }
        return list;
    }

    public void updateOrderStatus(String orderId, OrderStatus status, int filledQty) throws SQLException {
        String sql = "UPDATE orders SET status = ?, filled_quantity = ? WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, filledQty);
            stmt.setString(3, orderId);
            stmt.executeUpdate();
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        return new Order.Builder()
                .orderId(rs.getString("order_id"))
                .accountId(rs.getInt("account_id"))
                .symbol(rs.getString("symbol"))
                .side(OrderSide.valueOf(rs.getString("side")))
                .type(OrderType.valueOf(rs.getString("type")))
                .quantity(rs.getInt("original_quantity"))
                .filledQuantity(rs.getInt("filled_quantity"))
                .price(rs.getDouble("price"))
                .stopPrice(rs.getDouble("stop_price"))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .timestamp(LocalDateTime.parse(rs.getString("timestamp")))
                .build();
    }
}

