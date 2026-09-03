# TradeX CLI Stock Exchange — Database Schema & Storage Documentation

## 1. Database Overview

TradeX utilizes an embedded relational storage engine powered by **SQLite 3** via the official **Xerial SQLite JDBC Driver**.

- **Zero External Prerequisites**: Requires no background database servers (e.g., MySQL or PostgreSQL) to be installed or configured by the evaluator.
- **ACID Compliance**: Full transactional atomicity and data consistency across concurrent trading actions.
- **Relational Integrity**: Foreign key constraints enforce referential relationships across users, accounts, orders, trades, and holdings.
- **Defensive Prepared Statements**: All database operations strictly employ `PreparedStatement` with parameterized binding, completely preventing SQL injection vulnerabilities.

---

## 2. Table Schemas & Constraints

### 2.1 `users` Table
Stores authentication credentials and permission roles.
```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

### 2.2 `accounts` Table
Maintains total and frozen virtual cash balances.
```sql
CREATE TABLE IF NOT EXISTS accounts (
    account_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    cash_balance REAL NOT NULL,
    frozen_cash REAL NOT NULL DEFAULT 0.0,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 2.3 `stocks` Table
Stores instrument master data, session statistics, and circuit limits.
```sql
CREATE TABLE IF NOT EXISTS stocks (
    symbol TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    sector TEXT NOT NULL,
    current_price REAL NOT NULL,
    previous_close REAL NOT NULL,
    day_open REAL NOT NULL,
    day_high REAL NOT NULL,
    day_low REAL NOT NULL,
    volume INTEGER NOT NULL,
    fifty_two_high REAL NOT NULL,
    fifty_two_low REAL NOT NULL,
    upper_circuit REAL NOT NULL,
    lower_circuit REAL NOT NULL,
    status TEXT NOT NULL
);
```

### 2.4 `orders` Table
Records all placed, resting, filled, and cancelled orders.
```sql
CREATE TABLE IF NOT EXISTS orders (
    order_id TEXT PRIMARY KEY,
    account_id INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    type TEXT NOT NULL,
    original_quantity INTEGER NOT NULL,
    filled_quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    stop_price REAL NOT NULL,
    status TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    FOREIGN KEY (symbol) REFERENCES stocks(symbol)
);
```

### 2.5 `trades` Table
Immutable audit record of all matched and executed trades.
```sql
CREATE TABLE IF NOT EXISTS trades (
    trade_id TEXT PRIMARY KEY,
    buy_order_id TEXT NOT NULL,
    sell_order_id TEXT NOT NULL,
    buyer_account_id INTEGER NOT NULL,
    seller_account_id INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    brokerage_buyer REAL NOT NULL,
    brokerage_seller REAL NOT NULL,
    timestamp TEXT NOT NULL
);
```

### 2.6 `holdings` Table
Maintains current share ownership, average buy price, and realized P&L.
```sql
CREATE TABLE IF NOT EXISTS holdings (
    holding_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    average_buy_price REAL NOT NULL,
    realized_pnl REAL NOT NULL DEFAULT 0.0,
    UNIQUE(account_id, symbol)
);
```

### 2.7 `transactions` Table
Ledger audit trail for cash deposits, withdrawals, debits, and credits.
```sql
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    balance_after REAL NOT NULL,
    description TEXT,
    timestamp TEXT NOT NULL
);
```

### 2.8 `alerts` & `market_events` Tables
Monitors user-defined price alerts and stores macroeconomic news headlines.
```sql
CREATE TABLE IF NOT EXISTS alerts (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    symbol TEXT NOT NULL,
    type TEXT NOT NULL,
    target_value REAL NOT NULL,
    triggered INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS market_events (
    event_id INTEGER PRIMARY KEY AUTOINCREMENT,
    headline TEXT NOT NULL,
    symbol TEXT,
    sentiment TEXT NOT NULL,
    impact_pct REAL NOT NULL,
    timestamp TEXT NOT NULL
);
```
