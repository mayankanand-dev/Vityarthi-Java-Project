# TradeX — CLI Stock Exchange Simulator
## Project Problem Statement & Scope Specification

---

## 1. Problem Statement

Understanding the real-time operational mechanics of a modern financial exchange—specifically the **double-auction order book**, **price-time priority matching**, **atomic trade clearing**, **settlement**, and **circuit breaker risk controls**—presents a steep learning curve for students and software engineers. 

Most educational trading applications are either simplified portfolio trackers that query static delayed web APIs or superficial mock databases that execute unrealistic instantaneous buy/sell transactions without maintaining a matching engine or counterparty order book.

Consequently, learners rarely observe how bid-ask spreads fluctuate, how resting limit orders provide market liquidity, how crossing orders trigger executions at maker prices, or how concurrent automated traders interact with shared financial state. 

**TradeX** resolves this problem by implementing a comprehensive, self-contained, terminal-based stock exchange simulator written from scratch in native Java. It bridges foundational computer science concepts (Object-Oriented Design, Dual PriorityQueue heaps, Concurrency, JDBC, Java Streams, and NIO.2) with practical financial market infrastructure.

---

## 2. Project Scope

- **Simulated Environment**: TradeX operates exclusively in a simulated environment using virtual currency and synthetic market instruments modeled after major Indian blue-chip equities (e.g., RELIANCE, TCS, INFY, HDFCBANK).
- **No Real-Money Transactions**: The platform does not process real currency, initiate banking transactions, or connect to external production brokerage APIs.
- **Pure Terminal CLI**: The entire application runs natively within a command-line terminal interface, requiring zero web browsers, GUI dependencies, or external application servers.
- **Deterministic & Stochastic Simulation**: The platform provides both a discrete step simulator and continuous background market threads driven by Geometric Brownian Motion with support for deterministic random seeds during automated testing.

---

## 3. Target Users

1. **Undergraduate Computer Science Students**: Learners studying core and advanced Java concepts who seek an authentic, production-grade example of OOP, multithreading, custom collections, and design patterns.
2. **Finance & FinTech Students**: Learners who wish to explore the operational lifecycle of equity matching engines, bid-ask spreads, circuit breakers, and post-trade settlement without financial risk.
3. **Academic Course Evaluators**: Faculty and evaluators who require a self-contained, reproducible, zero-configuration software project with built-in test suites and clean architecture.

---

## 4. High-Level Functional Modules

| Module | Core Capabilities |
| :--- | :--- |
| **User & Account Management** | SHA-256 trader authentication, account creation, deposit/withdrawal of virtual cash, and transaction ledger. |
| **Market & Stock Catalog** | Real-time quote ticker for 10 blue-chip equities, sector classification, circuit limit bands, and advance/decline breadth. |
| **Order Management** | Validation and lifecycle management for Market, Limit, Stop, and Stop-Limit orders across Buy and Sell sides. |
| **Double-Auction Order Book** | Dual PriorityQueues enforcing strict Price-Time priority with thread-safe `ReentrantReadWriteLock` synchronization. |
| **Matching & Settlement Engine** | Continuous double-auction cross detection, maker price execution, partial fill processing, and atomic cash/share clearing. |
| **Portfolio & P&L Module** | Real-time calculation of share holdings, weighted average cost basis, unrealized P&L, realized P&L, and net worth. |
| **Technical Analysis Engine** | Quantitative computation of SMA(20), SMA(50), EMA(20), RSI(14), volatility, and algorithmic trading signals. |
| **Market Simulation & Bots** | Geometric Brownian Motion price simulation, macroeconomic news injection, and concurrent trading bot agents. |
| **Analytics & Reports** | Java Streams ranking for top gainers, losers, and turnover, plus Java NIO.2 CSV file exports. |
| **Database Persistence** | SQLite 3 embedded relational database with prepared statements and relational referential integrity. |
