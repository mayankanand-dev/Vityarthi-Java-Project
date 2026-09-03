# VITyarthi Evaluated Course Project Report
# TRADEX — CLI Stock Exchange Simulator

---

## 1. Project Title & Cover Information
- **Project Title**: TradeX — CLI Stock Exchange Simulator
- **Course**: Advanced Java Programming / Object-Oriented Software Development
- **Institution**: Vellore Institute of Technology (VIT / VITyarthi)
- **Execution Platform**: Pure Command-Line Terminal (Native Java 17/21/25)
- **Primary Technologies**: Core Java, Dual PriorityQueue Order Books, Multi-threading & Concurrency, SQLite JDBC, Java Streams, Java NIO.2

---

## 2. Introduction
In the contemporary global economy, financial exchanges are among the most latency-critical, high-concurrency software architectures ever constructed. Millions of bids and asks enter matching engines every second, requiring rigorous adherence to price-time priority, sub-millisecond trade execution, and absolute ledger invariants where not a single fraction of a rupee or share is unaccounted for.

**TradeX** is an original, fully functional command-line stock exchange simulator designed to demonstrate real-world financial infrastructure concepts purely through native Java. It bridges foundational computer science coursework with industry software practices by simulating an end-to-end stock exchange complete with double-auction limit order books, risk management circuit breakers, atomic settlement, quantitative technical analysis, stochastic market simulation, and persistent relational data storage.

---

## 3. Problem Statement & Scope
### Problem Statement
Students learning Java and financial engineering frequently lack hands-on exposure to how order books actually function. Conventional academic projects often implement simplistic e-commerce stores or basic CRUD inventory managers that lack algorithmic depth and real-world concurrency challenges. Furthermore, public stock API wrappers merely display delayed web data without explaining the underlying double-auction cross detection, maker-taker pricing conventions, or the mechanics of bid-ask spreads.

### Project Scope
- **Domain Modeling**: Realistic representation of Indian bluechip equities (RELIANCE, TCS, INFY, HDFCBANK, ICICIBANK, ITC, SBIN, LT, WIPRO, HCLTECH).
- **Simulated Integrity**: All cash and equities are virtual. No actual banking or broker credentials are used.
- **Strict CLI Operation**: The system executes 100% from the terminal without web servers, browsers, or GUI frameworks.

---

## 4. Functional Requirements
The project strictly implements the four governing modules and all corresponding sub-features:
1. **Module A — User & Account Management**: SHA-256 secure login and registration, virtual account creation, deposit/withdrawal operations, cash ledger passbook, and role-based permissions (Trader vs. Admin).
2. **Module B — Market & Stock Management**: Real-time ticker quotes, sector aggregation, previous close, day open/high/low tracking, cumulative volume, 52-week price bands, and advance/decline market breadth.
3. **Module C — Order Management**: Comprehensive order entry for Market, Limit, Stop, and Stop-Limit orders across Buy and Sell sides, order cancellation, and active order queue inspection.
4. **Module D — Double-Auction Order Book & Matching Engine**: Price-time priority dual heaps, maker-taker execution pricing, partial fill execution, and spread calculations.
5. **Module E — Atomic Clearing & Settlement**: Post-trade cash debit/credit, inventory transfer, dynamic weighted average acquisition price computation, and realized P&L accounting.
6. **Module F — Quantitative Technical Analysis**: Rolling SMA-20, SMA-50, EMA-20, RSI-14 momentum oscillators, and multi-factor bullish/bearish signal generation.
7. **Module G — Concurrency & Market Simulation**: Stochastic Geometric Brownian Motion price ticks, macroeconomic news injection, and background automated algorithmic trading bots (Momentum, Mean Reversion, Liquidity Provider).
8. **Module H — Reports & Java NIO.2 Export**: CSV generation of portfolio summaries, market quotes, and trade execution histories.

---

## 5. Non-Functional Requirements
- **Performance**: High-throughput in-memory order matching capable of matching orders in under 1 millisecond using Dual PriorityQueue heaps.
- **Reliability & Consistency**: Complete transactional consistency under multi-threaded concurrency, verified by high-contention latch tests ensuring balance invariants remain intact.
- **Defensive Usability**: Non-crashing terminal interface with robust input validation that gracefully traps type mismatches, out-of-range choices, and EOF conditions.
- **Security & Integrity**: Parameterized SQL statements (`PreparedStatement`) eliminating SQL injection risks, and cryptographic hashing (SHA-256) for authentication.
- **Zero-Dependency Portability**: Built with 100% native Java JDK tools (`javac` and `jar`) and embedded SQLite JDBC, requiring zero external server configuration.

---

## 6. System Architecture
TradeX is organized using a clean, decoupled 4-tier architecture:
1. **Presentation Tier (`tradex.app`)**: Terminal UI, menu controllers, ANSI dashboards, and input validators.
2. **Service & Domain Tier (`tradex.service`, `tradex.model`)**: High-level business logic, portfolio valuation, quantitative indicators, and account operations.
3. **Exchange Core (`tradex.exchange`, `tradex.simulation`, `tradex.strategy`)**: In-memory double-auction order books, matching algorithms, atomic settlement, circuit breakers, and algorithmic trading bots.
4. **Persistence & Storage Tier (`tradex.repository`, `tradex.database`, `tradex.util`)**: SQLite JDBC repositories with prepared statements and Java NIO.2 file export services.

---

## 7. Design Diagrams
The project includes five comprehensive software engineering design diagrams located in `docs/diagrams/`:
- **Use Case Diagram** (`docs/diagrams/use-case.png`): Details interactions between Traders, Simulation Bots, and Exchange Operators across 12 distinct operations.
- **Class Diagram** (`docs/diagrams/class-diagram.png`): UML depiction of domain models, dual priority queue order books, strategy patterns, and repositories.
- **Sequence Diagram** (`docs/diagrams/sequence-diagram.png`): Step-by-step trace of order submission, price band validation, margin freezing, cross matching, and ACID settlement.
- **Workflow Diagram** (`docs/diagrams/workflow.png`): Operational state-machine lifecycle from order entry to post-trade portfolio recalculation.
- **Entity-Relationship Diagram** (`docs/diagrams/er-diagram.png`): Complete relational schema showing 9 SQLite tables, primary keys, foreign keys, and cardinalities.

---

## 8. Design Decisions & Architectural Rationale

### 8.1 Why In-Memory PriorityQueues for the Order Book?
In high-frequency exchanges, writing every single resting order to disk before matching introduces unacceptable latency bottlenecks. By maintaining resting bids in a max-heap and resting asks in a min-heap, finding the best bid or ask is an $O(1)$ operation, and inserting or deleting an order is $O(\log N)$. Thread safety is guaranteed via `ReentrantReadWriteLock`.

### 8.2 Why the Strategy Pattern for Automated Traders?
Algorithmic participants possess fundamentally distinct behavioral objectives:
- `MomentumStrategy` buys on upward acceleration.
- `MeanReversionStrategy` buys oversold pullbacks relative to the day open.
- `RandomLiquidityStrategy` quotes dual-sided bid-ask spreads around the mid-market.
Using the Strategy Pattern allows adding new automated trading algorithms without modifying the worker threads or matching engine.

### 8.3 Why Embedded SQLite with JDBC?
Rather than requiring the evaluator to install, configure, and maintain an external database server (which frequently leads to environment setup failures during evaluation), SQLite provides an ACID-compliant, zero-configuration local database that resides entirely in `data/tradex.db`.

---

## 9. Implementation Details

### Java Course Concepts Demonstrably Utilized:
- **Object-Oriented Programming**: Encapsulation of account state, inheritance and polymorphism across `TradingStrategy`, and custom domain exceptions.
- **Collections Framework**: `PriorityQueue` for order books, `ConcurrentHashMap` for active symbols, `CopyOnWriteArrayList` for thread-safe event logging.
- **Generics**: Generic data handling across repositories, comparators, and optionals.
- **Concurrency & Multithreading**: `ScheduledExecutorService`, `CountDownLatch`, `ReentrantReadWriteLock`, synchronized blocks, and atomic balance invariants.
- **Functional Programming**: Java Streams, Lambdas, and Collectors for market ranking, volume filtering, and sector turnover calculations.
- **File I/O**: Modern Java NIO.2 (`java.nio.file.Files`, `Paths`, `StandardOpenOption`) for report exports and logging.
- **Database Connectivity**: JDBC `Connection`, `PreparedStatement`, `ResultSet`, and transactional schema migrations.
- **Design Patterns**: Singleton (`DatabaseManager`), Builder (`Order.Builder`), Strategy (`TradingStrategy`).

---

## 10. Execution Results & Screenshots
All screenshots represent actual, authentic terminal sessions captured during live execution and are embedded in the project documentation under `docs/screenshots/`:
1. `01-main-menu.png`: Interactive CLI Dashboard & Overview.
2. `02-market-view.png`: Real-time quote table for 10 bluechip equities.
3. `03-order-placement.png`: Order entry, circuit band validation, and margin reservation.
4. `04-order-book.png`: Dual-sided Level-5 Market Depth for RELIANCE.
5. `05-trade-execution.png`: Cross-order execution, maker pricing, and atomic clearing.
6. `06-portfolio.png`: Portfolio summary with cash, invested capital, P&L, and net worth.
7. `07-market-simulation.png`: Geometric Brownian Motion price ticks and news event shocks.
8. `08-analytics.png`: Java Streams market leader rankings and exchange turnover.
9. `09-error-handling.png`: Defensive circuit band rejections and fund validations.
10. `10-database-persistence.png`: Evaluator 15-step walkthrough and SQLite persistence verification.

---

## 11. Testing Approach & Validation Evidence
The project contains an automated test runner (`tradex.test.TestRunner`) verifying 14 distinct business and technical criteria:
- **Test 01**: Valid Market Order Execution $\rightarrow$ **PASS**
- **Test 02**: Invalid Order Quantity Rejection $\rightarrow$ **PASS**
- **Test 03**: Insufficient Funds Validation $\rightarrow$ **PASS**
- **Test 04**: Insufficient Holdings Validation $\rightarrow$ **PASS**
- **Test 05**: Limit-Order Price Cross Matching $\rightarrow$ **PASS**
- **Test 06**: Partial Order Fill Handling $\rightarrow$ **PASS**
- **Test 07**: Strict Price-Time Priority in OrderBook $\rightarrow$ **PASS**
- **Test 08**: Order Cancellation and Margin Unfreeze $\rightarrow$ **PASS**
- **Test 09**: Settlement Cash Deductions & Credits $\rightarrow$ **PASS**
- **Test 10**: Holdings Cost Basis Recalculation $\rightarrow$ **PASS**
- **Test 11**: Concurrent Multi-Threaded Stress Test (10,000 Ops) $\rightarrow$ **PASS**
- **Test 12**: Quantitative Technical Analysis (SMA & RSI) $\rightarrow$ **PASS**
- **Test 13**: SQLite JDBC Relational Persistence & CRUD $\rightarrow$ **PASS**
- **Test 14**: Java NIO.2 CSV File Export Verification $\rightarrow$ **PASS**

**Result**: 14 Passed, 0 Failed, executed in under 1 second.

---

## 12. Challenges Faced & Engineering Solutions
1. **Handling Concurrency in Double-Auction Heaps**: Concurrent access between user order entry and background simulation bots initially created potential race conditions. This was resolved using `ReentrantReadWriteLock`, allowing non-blocking concurrent depth reads while serializing order insertions and cancellations.
2. **Partial Fills and Maker-Taker Pricing**: Ensuring that when an incoming order partially consumes multiple price levels, each fill executes strictly at the resting maker's limit price. This was addressed by implementing an iterative matching loop that computes the execution price dynamically per fill.
3. **Multi-Threaded Balance Invariance**: Ensuring that concurrent rapid deposits, withdrawals, and trade debits never corrupt account cash balances. This was resolved by implementing fine-grained object-level synchronization on `Account` instances.

---

## 13. Learnings & Key Takeaways
- Gained practical mastery over Java's concurrency primitives, executor thread pools, and memory synchronization models.
- Understood the concrete mathematical and algorithmic mechanics of financial exchange matching engines, bid-ask spreads, and double-auction price discovery.
- Learned how to design and build self-contained, native Java command-line applications that are resilient to malformed user input and platform differences.

---

## 14. Future Enhancements
- **Multi-Asset Support**: Extending the matching engine beyond equities to support derivative options contracts (Call/Put options) and futures.
- **WebSocket Gateway**: Exposing a lightweight binary or JSON socket interface for external algorithmic bots to trade over network sockets.
- **Advanced Order Types**: Implementing Good-Til-Cancelled (GTC), Immediate-or-Cancel (IOC), and Fill-or-Kill (FOK) order validity durations.

---

## 15. References & Academic Citations
1. Bloch, J. (2018). *Effective Java* (3rd ed.). Addison-Wesley Professional.
2. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
3. Harris, L. (2003). *Trading and Exchanges: Market Microstructure for Practitioners*. Oxford University Press.
4. Oracle Corporation. (2025). *Java Platform, Standard Edition Documentation (Java 21/25)*. Oracle Technology Network.
5. SQLite Consortium. (2025). *SQLite Database Engine Documentation and Architecture*.
