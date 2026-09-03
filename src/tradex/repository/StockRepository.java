package tradex.repository;

import tradex.database.DatabaseManager;
import tradex.model.Stock;
import tradex.model.enums.MarketStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockRepository {
    private final DatabaseManager dbManager;

    public StockRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public StockRepository() {
        this(DatabaseManager.getInstance());
    }

    public void save(Stock stock) throws SQLException {
        String sql = "INSERT OR REPLACE INTO stocks (" +
                "symbol, name, sector, current_price, previous_close, day_open, " +
                "day_high, day_low, volume, fifty_two_high, fifty_two_low, " +
                "upper_circuit, lower_circuit, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stock.getSymbol());
            stmt.setString(2, stock.getName());
            stmt.setString(3, stock.getSector());
            stmt.setDouble(4, stock.getCurrentPrice());
            stmt.setDouble(5, stock.getPreviousClose());
            stmt.setDouble(6, stock.getDayOpen());
            stmt.setDouble(7, stock.getDayHigh());
            stmt.setDouble(8, stock.getDayLow());
            stmt.setLong(9, stock.getVolume());
            stmt.setDouble(10, stock.getFiftyTwoWeekHigh());
            stmt.setDouble(11, stock.getFiftyTwoWeekLow());
            stmt.setDouble(12, stock.getUpperCircuit());
            stmt.setDouble(13, stock.getLowerCircuit());
            stmt.setString(14, stock.getStatus().name());
            stmt.executeUpdate();
        }
    }

    public Optional<Stock> findBySymbol(String symbol) {
        String sql = "SELECT * FROM stocks WHERE UPPER(symbol) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, symbol.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StockRepository] findBySymbol error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Stock> listAll() {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stocks ORDER BY symbol ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stocks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StockRepository] listAll error: " + e.getMessage());
        }
        return stocks;
    }

    public void updatePriceAndVolume(Stock stock) throws SQLException {
        String sql = "UPDATE stocks SET " +
                "current_price = ?, day_high = ?, day_low = ?, volume = ?, " +
                "fifty_two_high = ?, fifty_two_low = ?, status = ? " +
                "WHERE symbol = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, stock.getCurrentPrice());
            stmt.setDouble(2, stock.getDayHigh());
            stmt.setDouble(3, stock.getDayLow());
            stmt.setLong(4, stock.getVolume());
            stmt.setDouble(5, stock.getFiftyTwoWeekHigh());
            stmt.setDouble(6, stock.getFiftyTwoWeekLow());
            stmt.setString(7, stock.getStatus().name());
            stmt.setString(8, stock.getSymbol());
            stmt.executeUpdate();
        }
    }

    private Stock mapRow(ResultSet rs) throws SQLException {
        return new Stock(
                rs.getString("symbol"),
                rs.getString("name"),
                rs.getString("sector"),
                rs.getDouble("current_price"),
                rs.getDouble("previous_close"),
                rs.getDouble("day_open"),
                rs.getDouble("day_high"),
                rs.getDouble("day_low"),
                rs.getLong("volume"),
                rs.getDouble("fifty_two_high"),
                rs.getDouble("fifty_two_low"),
                rs.getDouble("upper_circuit"),
                rs.getDouble("lower_circuit"),
                MarketStatus.valueOf(rs.getString("status"))
        );
    }
}
