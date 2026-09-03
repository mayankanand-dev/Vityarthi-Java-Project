# TradeX CLI Stock Exchange — System Architecture Documentation

## 1. Architectural Overview

TradeX is engineered around an **Event-Driven, Layered Architecture** that decouples user interaction, order matching, transaction settlement, and relational persistence. The system operates strictly as a native command-line Java application without external server runtimes or GUI frameworks.

```
+-------------------------------------------------------------------------+
|                       Presentation Layer (CLI)                          |
|   tradex.app.Main, InputValidator, Menu Controllers, ANSI Dashboards     |
+-------------------------------------------------------------------------+
                                   |
                                   v
+-------------------------------------------------------------------------+
|                        Service & Application Layer                      |
|   AccountService, MarketService, OrderService, PortfolioService,        |
|   AnalyticsService (Streams/Lambdas), TechnicalAnalysisService          |
+-------------------------------------------------------------------------+
                                   |
                                   v
+-------------------------------------------------------------------------+
|                       Exchange & Simulation Core                        |
|   - Double-Auction MatchingEngine (Price-Time Priority, Maker Pricing)   |
|   - Dual-Queue OrderBook (PriorityQueue Bids & Asks, Concurrency Locks) |
|   - Atomic SettlementEngine (Cash & Share Ledgers, Brokerage)            |
|   - CircuitBreaker Risk Monitor (+/-10% Price Bands, Volatility Halts)   |
|   - MarketSimulationEngine (Geometric Brownian Motion + Macro News)     |
|   - AutomatedTrader Bots (Strategy Pattern: Momentum, Mean Reversion)   |
+-------------------------------------------------------------------------+
                                   |
                                   v
+-------------------------------------------------------------------------+
|                       Persistence & I/O Layer                           |
|   - JDBC Repositories (PreparedStatements, Connection Management)       |
|   - SQLite Database Engine (Embedded local ACID storage)                |
|   - Java NIO.2 FileManager (CSV Report Exports, System Audit Logs)       |
+-------------------------------------------------------------------------+
```

---

## 2. Core Architectural Components

### 2.1 The Double-Auction Order Book (`OrderBook.java`)
At the core of the financial simulation is an in-memory double-auction Limit Order Book maintained for each listed security:
- **Bids Heap**: Implemented via `PriorityQueue<Order>` configured with a descending price comparator (`o2.getPrice() - o1.getPrice()`). Ties are resolved deterministically using ascending timestamp comparison (`o1.getTimestamp().compareTo(o2.getTimestamp())`).
- **Asks Heap**: Implemented via `PriorityQueue<Order>` configured with an ascending price comparator (`o1.getPrice() - o2.getPrice()`).
- **Concurrency & Locking**: Shared state between active CLI traders and concurrent simulation bots is synchronized using `ReentrantReadWriteLock`. Read operations (such as depth aggregation and spread queries) acquire shared read locks, while order insertion and cancellations acquire exclusive write locks.

### 2.2 Continuous Matching Engine (`MatchingEngine.java`)
The matching engine implements continuous double-auction mechanics:
1. **Order Reception**: Orders pass through pre-trade risk controls (circuit band adherence and margin verification).
2. **Cross Detection**: A cross condition occurs whenever `bestBid.getPrice() >= bestAsk.getPrice()`.
3. **Execution Pricing**: Trade executions adhere to the standard exchange maker-taker price rule: trades execute at the price of the resting order that was already in the book.
4. **Partial Fills**: Unfilled balances remain active with their original priority timestamp, while fully satisfied orders are evicted from the queue.

### 2.3 Atomic Settlement Engine (`SettlementEngine.java`)
Post-trade clearing ensures transactional integrity:
- Cash is atomically debited from the buyer's account and credited to the seller's account after calculating simulated regulatory brokerage (0.05% with a ₹10 minimum).
- Securities inventory is updated via `Holding` positions. For buys, the weighted average acquisition price is recalculated. For sells, shares are decremented and realized profit/loss is accrued.
- Synchronized locks prevent race conditions and double-spending across concurrent trading sessions.

### 2.4 Quantitative Technical Analysis Engine (`TechnicalAnalysisService.java`)
The analysis engine computes authentic quantitative statistics:
- **SMA-20 & SMA-50**: Rolling arithmetic mean over 20 and 50 discrete price periods.
- **EMA-20**: Weighted exponential smoothing using multiplier $k = \frac{2}{N + 1}$.
- **RSI-14**: Relative Strength Index tracking average up-moves vs. down-moves over a 14-period lookback window.
- **Historical Volatility & Momentum**: Standard deviation of percentage returns and rate-of-change momentum metrics.

---

## 3. Design Patterns Applied

| Design Pattern | Implementation Class | Architectural Rationale |
| :--- | :--- | :--- |
| **Singleton** | `DatabaseManager` | Centralizes SQLite JDBC connection lifecycle, ensuring single-point schema initialization and migration. |
| **Builder** | `Order.Builder` | Provides readable, immutable construction of complex order specifications with optional stop parameters and default statuses. |
| **Strategy** | `TradingStrategy` | Decouples algorithmic behavior (`MomentumStrategy`, `MeanReversionStrategy`, `RandomLiquidityStrategy`) from worker execution threads. |
| **Repository** | `*Repository` classes | Isolates SQL persistence from domain models, adhering to Separation of Concerns. |
