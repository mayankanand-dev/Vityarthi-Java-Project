package tradex.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {
    static volatile DatabaseManager instance;
    String dbUrl;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] SQLite JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        ensureDirectoryExists();
        initializeSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager("jdbc:sqlite:data/tradex.db");
                }
            }
        }
        return instance;
    }

    
    public static synchronized DatabaseManager initializeCustom(String customJdbcUrl) {
        instance = new DatabaseManager(customJdbcUrl);
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void ensureDirectoryExists() {
        if (dbUrl.startsWith("jdbc:sqlite:data/")) {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        }
    }

    public void initializeSchema() {
        String[] ddlStatements = {
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password_hash TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS accounts (" +
                        "account_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "cash_balance REAL NOT NULL, " +
                        "frozen_cash REAL NOT NULL DEFAULT 0.0, " +
                        "created_at TEXT NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS stocks (" +
                        "symbol TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "sector TEXT NOT NULL, " +
                        "current_price REAL NOT NULL, " +
                        "previous_close REAL NOT NULL, " +
                        "day_open REAL NOT NULL, " +
                        "day_high REAL NOT NULL, " +
                        "day_low REAL NOT NULL, " +
                        "volume INTEGER NOT NULL, " +
                        "fifty_two_high REAL NOT NULL, " +
                        "fifty_two_low REAL NOT NULL, " +
                        "upper_circuit REAL NOT NULL, " +
                        "lower_circuit REAL NOT NULL, " +
                        "status TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS orders (" +
                        "order_id TEXT PRIMARY KEY, " +
                        "account_id INTEGER NOT NULL, " +
                        "symbol TEXT NOT NULL, " +
                        "side TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "original_quantity INTEGER NOT NULL, " +
                        "filled_quantity INTEGER NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "stop_price REAL NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "timestamp TEXT NOT NULL, " +
                        "FOREIGN KEY (account_id) REFERENCES accounts(account_id), " +
                        "FOREIGN KEY (symbol) REFERENCES stocks(symbol)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS trades (" +
                        "trade_id TEXT PRIMARY KEY, " +
                        "buy_order_id TEXT NOT NULL, " +
                        "sell_order_id TEXT NOT NULL, " +
                        "buyer_account_id INTEGER NOT NULL, " +
                        "seller_account_id INTEGER NOT NULL, " +
                        "symbol TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "brokerage_buyer REAL NOT NULL, " +
                        "brokerage_seller REAL NOT NULL, " +
                        "timestamp TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS holdings (" +
                        "holding_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "account_id INTEGER NOT NULL, " +
                        "symbol TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "average_buy_price REAL NOT NULL, " +
                        "realized_pnl REAL NOT NULL DEFAULT 0.0, " +
                        "UNIQUE(account_id, symbol)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS transactions (" +
                        "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "account_id INTEGER NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "balance_after REAL NOT NULL, " +
                        "description TEXT, " +
                        "timestamp TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS alerts (" +
                        "alert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "account_id INTEGER NOT NULL, " +
                        "symbol TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "target_value REAL NOT NULL, " +
                        "triggered INTEGER NOT NULL DEFAULT 0, " +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS market_events (" +
                        "event_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "headline TEXT NOT NULL, " +
                        "symbol TEXT, " +
                        "sentiment TEXT NOT NULL, " +
                        "impact_pct REAL NOT NULL, " +
                        "timestamp TEXT NOT NULL" +
                        ");"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : ddlStatements) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Schema initialization failed: " + e.getMessage());
        }
    }

    
    public void resetDatabase() {
        String[] dropStatements = {
                "DROP TABLE IF EXISTS market_events;",
                "DROP TABLE IF EXISTS alerts;",
                "DROP TABLE IF EXISTS transactions;",
                "DROP TABLE IF EXISTS holdings;",
                "DROP TABLE IF EXISTS trades;",
                "DROP TABLE IF EXISTS orders;",
                "DROP TABLE IF EXISTS stocks;",
                "DROP TABLE IF EXISTS accounts;",
                "DROP TABLE IF EXISTS users;"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : dropStatements) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Reset failed: " + e.getMessage());
        }
        initializeSchema();
    }
}

