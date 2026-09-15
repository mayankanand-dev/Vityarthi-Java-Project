# TradeX — Stock Trading & Exchange Simulator
## Project Report & Implementation Guide

- **Student Name**: Mayank Anand  
- **Registration Number**: 25BAI11209  
- **Project Name**: TradeX  
- **Language**: Java (JDK 17+)  
- **Storage**: SQLite 3 (via JDBC)  

---

## 1. Project Overview

**TradeX** is a command-line stock exchange and trading simulator written entirely in core Java. 

The idea behind this project came from a simple observation: most student-level stock trading projects are essentially just glorified calculators. You click "buy", the program takes money out of your account, adds a number to your database, and that's it. There are no buyers, no sellers, no order queues, and no real market mechanics.

In the real world, stock exchanges like NSE, BSE, or NASDAQ don't work like that at all. Every trade needs two people—a buyer and a seller—who agree on a price. If you want to buy cheaper than anyone is selling, your order has to sit in a line (the order book) until someone comes along and takes your price.

I wanted to build a program from scratch that actually mimics how a real equity exchange operates under the hood. TradeX includes an in-memory double-auction order book (using binary heaps), automatic trade matching with price-time priority, circuit breakers to prevent crazy price spikes, simulated trading bots that place orders in the background, portfolio tracking with real-time profit and loss (P&L), technical indicators (SMA and RSI), and persistent SQLite storage.

---

## 2. Problem Statement & Motivation

### Why standard trading projects fall short:
- **No real counterparties**: In most college projects, the system itself acts as a magic store with endless shares and fixed prices.
- **Missing market microstructure**: Concepts like bid-ask spreads, order books, market depth, and slippage are completely ignored.
- **Ignoring concurrency**: Real markets handle trades happening at the exact same millisecond. If multiple users or bots trade at once without proper thread-safety, account balances get corrupted.

### What TradeX does differently:
1. **Real Order Book**: Buy orders (Bids) and Sell orders (Asks) are kept in two separate queues.
2. **Price-Time Priority**: The best price always goes first. If two people offer the same price, whoever got there first gets filled first.
3. **Maker vs. Taker Pricing**: If you submit an aggressive order that matches immediately, you get filled at the price the other person already had waiting on the book.
4. **Independent Bots**: Automated background bots (Momentum, Mean Reversion, and Liquidity bots) continuously place orders, creating an active market with moving prices.
5. **Completely Local & Self-Contained**: It runs straight from the terminal with zero external dependencies, no paid API keys, and no heavy frameworks.

---

## 3. Key Features

Here is a summary of what I built into the application:

- **Live Market Watch**: A live console ticker showing 10 popular Indian blue-chip stocks (RELIANCE, TCS, INFY, HDFCBANK, ICICIBANK, ITC, SBIN, LT, WIPRO, HCLTECH) with current price, day high/low, percentage change, and volume.
- **Multiple Order Types**:
  - **Market Order**: Executes immediately at whatever the best price is on the opposite side.
  - **Limit Order**: Sits on the order book until someone matches your exact price (or better).
  - **Stop-Loss Order**: Becomes an active order once a trigger price is hit, protecting against bad losses.
  - **Stop-Limit Order**: Triggers a limit order once the stop price is crossed.
- **Level-5 Market Depth**: Displays the top 5 bid prices and top 5 ask prices, showing the exact quantity and number of waiting orders at each price level.
- **Trade Settlement & Portfolio**: Updates cash balances, reserves margin when orders are placed, calculates average buying cost when you buy shares at different prices, and tracks realized vs. unrealized P&L.
- **Circuit Breakers (10% Bands)**: To protect against wild swings, the system automatically rejects any order placed more than 10% above or below the day's base price.
- **Market Simulation & News**: A background simulator that introduces small price walks, occasional breaking news events that shock prices, and bots that trade against the user.
- **Technical Indicators**: Calculates 20-period SMA, 50-period SMA, and 14-period RSI directly in code.
- **Data Persistence & CSV Export**: Saves accounts, orders, trades, and holdings in a local SQLite database (`data/tradex.db`) and exports trade history and portfolio reports to CSV files.
- **Automated Test Suite**: A built-in test suite with 14 unit and integration tests that verify balance integrity, matching rules, and edge cases.

---

## 4. System Architecture & Code Structure

I organized the codebase into clean packages so that the user interface, business logic, exchange matching, and database persistence don't step on each other's toes:

```
src/tradex/
├── app/          # Interactive CLI menus, ANSI colors, table formatting
├── exchange/     # OrderBook, MatchingEngine, CircuitBreaker logic
├── model/        # Core entities (Order, Trade, Stock, User, Portfolio)
├── service/      # Business logic (TradingService, PortfolioService, MarketService)
├── strategy/     # Trading bot algorithms (Momentum, MeanReversion, Liquidity)
├── simulation/   # Background price engine and bot runner threads
├── repository/   # SQLite database queries and CRUD operations
├── database/     # DB connection manager and table migration schema
├── exception/    # Custom exceptions (InsufficientFunds, CircuitBreaker, etc.)
├── util/         # CSV exporter, technical indicator calculations, password hashing
└── test/         # Automated 14-test test runner
```

### Design Patterns Used:

1. **Strategy Pattern (`tradex.strategy`)**:
   - I created an interface called `TradingStrategy` with a method `generateOrder(...)`. 
   - Then I implemented three different strategies: `MomentumStrategy` (buys when prices rise), `MeanReversionStrategy` (buys dips), and `RandomLiquidityStrategy` (places bids and asks around the middle price).
   - This made it really easy to add or tweak bots without having to rewrite any exchange code.

2. **Singleton Pattern (`DatabaseManager`)**:
   - SQLite works best when connection creation is centralized to avoid locking the database file. I used a singleton pattern to ensure only one database manager exists throughout the app runtime.

3. **Builder Pattern (`Order.Builder`)**:
   - Orders have a lot of fields: symbol, side, order type, quantity, limit price, stop price, user ID, timestamp, etc. 
   - Instead of writing a messy constructor with 8 different parameters, I used the Builder pattern so creating an order is clean and readable:
   ```java
   Order order = new Order.Builder()
       .symbol("RELIANCE")
       .side(OrderSide.BUY)
       .type(OrderType.LIMIT)
       .quantity(25)
       .price(2850.00)
       .userId(user.getId())
       .build();
   ```

4. **Repository Pattern (`tradex.repository`)**:
   - Kept all raw SQL queries inside repository classes (`UserRepository`, `OrderRepository`, `TradeRepository`, `PortfolioRepository`). The rest of the program just calls methods like `orderRepo.save(order)` without needing to know SQL.

---

## 5. How the Matching Engine & Order Book Work

The order book is the most important part of this project. Here is how I built it:

### 5.1 Dual Heaps (PriorityQueues)
For every stock, the order book holds two queues:
- **Bids (Buy Orders)**: Implemented as a **Max-Heap**. The highest buy price is always at the top because buyers offering the most money should get served first.
- **Asks (Sell Orders)**: Implemented as a **Min-Heap**. The lowest sell price is always at the top because sellers offering the cheapest price should get served first.

```
       BIDS (Max-Heap)                      ASKS (Min-Heap)
   Highest Price at the Top             Lowest Price at the Top
   ------------------------             ------------------------
   [Bid 1] 2,450.00 (Qty: 20)           [Ask 1] 2,455.00 (Qty: 15)  <-- Best Ask
   [Bid 2] 2,448.50 (Qty: 50)           [Ask 2] 2,458.00 (Qty: 30)
   [Bid 3] 2,445.00 (Qty: 100)          [Ask 3] 2,460.00 (Qty: 40)
             ^
          Best Bid
```

### 5.2 Price-Time Priority
What happens if two people place a buy order at the exact same price?
The engine uses **timestamps** as a tie-breaker. Whoever submitted their order first gets filled first.

Here is the exact comparator logic I wrote for the Bid side:
```java
public static final Comparator<Order> BID_COMPARATOR = (o1, o2) -> {
    // 1. Highest price gets higher priority
    int priceComp = Double.compare(o2.getLimitPrice(), o1.getLimitPrice());
    if (priceComp != 0) {
        return priceComp;
    }
    // 2. If prices are equal, earlier timestamp wins
    return Long.compare(o1.getTimestamp(), o2.getTimestamp());
};
```

### 5.3 Step-by-Step Matching Example (Maker vs. Taker Price)
Suppose the order book looks like this:
- Resting Maker Ask: **Seller Bob wants to sell 10 shares of TCS at ₹3,500**.

Now, Buyer Alice enters the market:
- Alice submits: **Limit Buy 10 shares of TCS at ₹3,510** (she is willing to pay up to ₹3,510).

What happens?
1. The engine checks if Alice's bid (₹3,510) is $\ge$ Bob's ask (₹3,500). Yes, the prices cross!
2. **What is the trade price?** In real stock exchanges, Alice gets filled at **₹3,500** (Bob's price), not ₹3,510. Bob was already resting on the book (the maker), so Alice (the taker) gets Bob's price and saves ₹10 per share.
3. 10 shares are transferred from Bob to Alice.
4. Alice's cash is debited by ₹35,000, Bob's cash is credited by ₹35,000.
5. Both orders are marked as `FILLED` and removed from the queues.

### 5.4 Handling Partial Fills
What if Alice wanted 25 shares, but Bob only had 10?
1. 10 shares execute immediately against Bob at ₹3,500. Bob's order is done (`FILLED`).
2. Alice still needs 15 shares. The engine checks the next seller in the queue.
3. If no other seller is willing to sell at or below ₹3,510, Alice's remaining 15 shares are converted into a resting Limit Buy order at ₹3,510 and placed on the Bid queue.

---

## 6. Concurrency & Thread Safety

Because background bots are constantly placing orders while the user is typing commands in the CLI menu, multithreading bugs were a big risk. Here is how I handled them:

### 6.1 `ReentrantReadWriteLock` for the Order Book
Whenever the user views the Level-5 order book or market depth, the program has to read through the priority queues. If a bot inserts an order at the exact same moment, Java throws a `ConcurrentModificationException`.

Using a simple `synchronized` keyword everywhere would slow the whole system down because multiple people wouldn't even be able to look at quotes at the same time.

Instead, I used `ReentrantReadWriteLock`:
- Multiple threads can acquire the **Read Lock** at the same time to inspect market depth without blocking each other.
- When an order is being added, matched, or removed, the engine acquires the exclusive **Write Lock**, quickly updates the queue, and releases it.

### 6.2 Safe Account Balances
To ensure a user's cash and margin don't get double-spent if two trades execute in parallel, account balance updates use synchronized methods:
```java
public synchronized boolean reserveFunds(double amount) {
    if (this.availableBalance >= amount) {
        this.availableBalance -= amount;
        this.lockedMargin += amount;
        return true;
    }
    return false;
}
```
When an order is placed, money is moved to `lockedMargin`. If the order is cancelled, it is immediately refunded. If it executes, the money is permanently settled.

---

## 7. Database Design (SQLite)

All data is stored locally in `data/tradex.db`. The database has 5 relational tables:

```
+---------------+        +----------------+        +---------------+
|     USERS     |        |     ORDERS     |        |    TRADES     |
+---------------+        +----------------+        +---------------+
| user_id (PK)  |<---+   | order_id (PK)  |<---+   | trade_id (PK) |
| username      |    +---| user_id (FK)   |    +---| buy_order_id  |
| password_hash |    |   | symbol         |    +---| sell_order_id |
| cash_balance  |    |   | side (BUY/SELL)|        | symbol        |
| locked_margin |    |   | type           |        | quantity      |
+---------------+    |   | quantity       |        | price         |
                     |   | limit_price    |        | executed_at   |
+---------------+    |   | status         |        +---------------+
|  PORTFOLIOS   |    |   +----------------+
+---------------+    |
| portfolio_id  |    |
| user_id (FK)  |----+
| symbol        |
| quantity      |
| average_cost  |
+---------------+
```

- **Foreign Keys**: Enabled using `PRAGMA foreign_keys = ON;` so orphaned records can't exist.
- **Prepared Statements**: Every database query uses parameterized `PreparedStatement` to ensure SQL injection is impossible.
- **Transactions**: Trade execution modifies buyer balance, seller balance, stock holdings, and order status inside a single transaction (`connection.setAutoCommit(false)`), rolling back if anything fails.

---

## 8. Java Concepts Applied in This Project

Here is a breakdown of how topics from our core Java curriculum were used in the actual code:

| Java Topic | Where and How It Was Applied |
| :--- | :--- |
| **Object-Oriented Programming** | Encapsulation in models (`Account`, `Order`, `Stock`), abstract classes and interfaces for `TradingStrategy` and `Repository`. |
| **Collections Framework** | `PriorityQueue` with custom comparators for the order book; `ConcurrentHashMap` for active stock lookup; `ArrayList` for trade history. |
| **Multithreading & Concurrency** | `ScheduledExecutorService` for running bot trading loops every few seconds; `ReentrantReadWriteLock` for safe order book access; synchronized methods for balance safety. |
| **Functional Programming & Streams** | Used `.stream().filter().sorted().collect()` to find top gainers/losers and calculate Level-5 aggregated depth. |
| **Custom Exception Handling** | Created custom exceptions like `InsufficientFundsException`, `CircuitBreakerException`, and `OrderValidationException` to give friendly error messages instead of raw stack traces. |
| **Modern File I/O (NIO.2)** | Used `java.nio.file.Files` and `Paths` to generate clean CSV reports for trade history and portfolio statements. |
| **JDBC Database Connectivity** | Connected to SQLite, executed DDL scripts on startup, and used `PreparedStatement` and `ResultSet` for CRUD operations. |

---

## 9. Visual Walkthrough & Screenshots

The application includes 10 terminal screenshots saved in the `screenshots/` directory that showcase its functionality:

| # | Screenshot | What it Shows |
| :---: | :--- | :--- |
| 1 | `screenshots/01-main-menu.webp` | **Main Menu**: Shows account overview, live wallet balance (₹10,00,000 for demo), and options to trade, view portfolio, or see market data. |
| 2 | `screenshots/02-market-view.webp` | **Market Watch**: Table of all 10 stocks showing live LTP (Last Traded Price), day change %, open, high, low, and traded volume. |
| 3 | `screenshots/03-order-placement.webp` | **Order Placement**: Placing a Limit Buy order on RELIANCE, with automatic circuit band checks and margin calculation. |
| 4 | `screenshots/04-order-book.webp` | **Level-5 Depth**: The live dual-sided order book showing the top 5 Bids and top 5 Asks with quantities and number of orders at each price. |
| 5 | `screenshots/05-trade-execution.webp` | **Execution & Settlement**: Real-time log showing two orders crossing, trade fill notice, and cash/stock transfer. |
| 6 | `screenshots/06-portfolio.webp` | **Portfolio Dashboard**: Displays owned shares, average buy price, current market price, and unrealized profit/loss. |
| 7 | `screenshots/07-market-simulation.webp` | **Market Simulation**: Background bots placing orders and simulated news events nudging prices up or down. |
| 8 | `screenshots/08-analytics.webp` | **Analytics & Screeners**: Stream-based market screeners displaying top gainers, top losers, and stocks with the highest turnover. |
| 9 | `screenshots/09-error-handling.webp` | **Input & Risk Validation**: Shows friendly rejection messages when attempting to buy with insufficient cash or placing orders outside the 10% circuit band. |
| 10 | `screenshots/10-database-persistence.webp` | **Database Verification**: Verifying that all trades and updated balances were written to `tradex.db`. |

---

## 10. Testing & Verification

I wrote a dedicated test runner (`tradex.test.TestRunner`) that runs 14 automated tests without needing external test frameworks like JUnit:

```
[TEST RUNNER] Running TradeX Automated Verification Suite...
----------------------------------------------------------------------
Test 01: Place valid market order and verify instant execution ... [PASS]
Test 02: Reject zero or negative order quantity ................. [PASS]
Test 03: Reject buy order when cash balance is insufficient ..... [PASS]
Test 04: Reject sell order when user does not own enough shares .. [PASS]
Test 05: Verify limit order price crossing and maker price fill . [PASS]
Test 06: Test partial fill and ensure remaining volume rests .... [PASS]
Test 07: Confirm strict Price-Time priority in order book queue . [PASS]
Test 08: Test order cancellation and margin unlock .............. [PASS]
Test 09: Verify exact cash debit on buyer and credit on seller .. [PASS]
Test 10: Check weighted average cost calculation on holdings .... [PASS]
Test 11: Multi-threaded stress test on account balance .......... [PASS]
Test 12: Verify mathematical accuracy of SMA and RSI formulas ... [PASS]
Test 13: SQLite JDBC database insert, update, and query test .... [PASS]
Test 14: Test CSV export generation via Java NIO.2 .............. [PASS]
----------------------------------------------------------------------
Result: 14/14 Tests Passed (0 Failures). Execution time: ~340ms.
```

---

## 11. Challenges I Ran Into & How I Fixed Them

1. **Calculating Average Buy Price**:
   - *Problem*: If you buy 10 shares of INFY at ₹1,500 and later buy 10 more at ₹1,600, what is your cost basis? When you later sell 5 shares, what happens to your cost?
   - *Fix*: I implemented a weighted average cost formula in `PortfolioService`:
     $$\text{New Avg Price} = \frac{(Q_{\text{old}} \times P_{\text{old}}) + (Q_{\text{new}} \times P_{\text{new}})}{Q_{\text{old}} + Q_{\text{new}}}$$
     When shares are sold, the average cost remains unchanged, and realized profit is simply $(P_{\text{sell}} - \text{Avg Price}) \times Q_{\text{sold}}$.

2. **Order Book Deadlocks & UI Freezes**:
   - *Problem*: When bots were actively placing orders in background threads while I was trying to print the market depth table on the screen, the terminal would occasionally hang or throw `ConcurrentModificationException`.
   - *Fix*: Switching to `ReentrantReadWriteLock` completely solved this. Multiple threads can now read the depth without waiting, while inserting an order briefly locks only the specific queue.

3. **Floating Point Rounding in Financials**:
   - *Problem*: Standard double precision (`double`) can introduce tiny rounding quirks (like `0.1 + 0.2 = 0.30000000000000004`), which looks terrible on financial balances.
   - *Fix*: Created a formatting utility that rounds prices and balances to 2 decimal places using `Math.round(val * 100.0) / 100.0` before display and storage.

---

## 12. Future Scope

If I have time to expand TradeX in the future, here are the top 3 features I'd love to add:
1. **Derivatives & Options**: Adding simple European Call and Put option contracts with Black-Scholes pricing.
2. **WebSockets / Simple GUI**: Exposing a lightweight WebSocket feed so a web dashboard or lightweight chart could display candlestick charts in real time.
3. **More Order Conditions**: Adding Good-Til-Cancelled (GTC) and Fill-Or-Kill (FOK) order conditions.

---

## 13. Conclusion

Building TradeX gave me a deep, hands-on understanding of how real financial exchanges work, far beyond simple database operations. It allowed me to combine data structures (binary heaps and custom comparators), multithreading (`ReentrantReadWriteLock` and scheduled thread pools), database programming (SQLite transactions and JDBC), and clean software architecture into a single, cohesive project.
