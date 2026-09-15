# Project Statement — TradeX

- **Student Name**: Mayank Anand  
- **Registration No**: 25BAI11209  

---

## 1. Problem Statement

When learning about financial applications in computer science coursework, students often encounter oversimplified stock market projects. In most of these implementations, buying a share simply means clicking a button to subtract money from a balance and increment an integer in a database table at a static price. 

In actual stock exchanges, transactions do not work this way. Real exchanges rely on an electronic double-auction order book where:
- Every trade requires an opposing buyer and seller who agree on a price.
- Limit orders sit in an order book queue and are matched based on strict Price-Time Priority (orders at better prices fill first, and orders at the same price fill in the order they arrived).
- Market orders execute immediately against the best available opposite quotes, often taking multiple price levels if quantity is large.
- Trades require atomic clearing and settlement so that cash deductions and share transfers happen together without race conditions.
- Exchanges enforce volatility safeguards like circuit breakers (price bands) to prevent catastrophic swings during abnormal market conditions.

Because live financial market data feeds and brokerage APIs are restricted, expensive, and involve financial risk, it is difficult for students to experiment with or understand these exchange mechanics first-hand.

**TradeX** addresses this issue by providing a self-contained, console-based stock trading and matching engine built in core Java. It gives learners an interactive environment to observe order book queues, submit different types of orders, trigger trades against simulated counterparties, and inspect portfolio valuations and settlement records.

---

## 2. Scope of the Project

### What the Project Covers:
- **Terminal-Based Interface**: A text-based interactive menu for account operations, market browsing, order entry, and portfolio inspection.
- **Order Management & Types**: Support for Market, Limit, Stop, and Stop-Limit orders across both Buy and Sell sides.
- **In-Memory Matching Engine**: Continuous double-auction matching engine with two priority queues (max-heap for bids, min-heap for asks) enforcing price-time priority.
- **Trade Settlement**: Immediate clearing logic that debits/credits buyer and seller cash, updates stock inventory, tracks realized profit/loss, and recalculates weighted average acquisition costs.
- **Market Guardrails**: Pre-trade validation including available funds/holdings checks and daily 10% upper/lower circuit breaker bands.
- **Market Simulation**: A price generator based on random steps and simulated news events, along with automated background trading bots that provide market liquidity.
- **Technical Analysis**: Basic mathematical calculation of 20-period and 50-period Simple Moving Averages (SMA) and the 14-period Relative Strength Index (RSI).
- **Relational Persistence**: Local storage of users, transactions, orders, and portfolios using SQLite through standard JDBC prepared statements.
- **Exporting**: Exporting portfolio summaries and market quotes to CSV files using Java NIO.2.

### What is Kept Out of Scope:
- **Real Money Transactions**: The system works entirely with virtual cash; no actual payment gateways or banking APIs are used.
- **Live Market Connectivity**: The platform runs locally and does not connect to external stock exchanges or broker web APIs.
- **Graphical Web or Mobile UI**: The application is kept strictly to a terminal interface to prioritize backend logic, concurrency, and data structures over front-end web design.
- **Derivatives & Leverage**: Options, futures, short-selling with borrowed margin, and intraday leverage are not implemented.

---

## 3. Target Users

- **Computer Science & Engineering Students**: Students learning core Java, object-oriented principles, custom comparators with priority queues, multithreading, and JDBC database programming.
- **Finance & Business Students**: Learners who want to understand how order books match orders, what bid-ask spreads represent, and how limit orders behave without financial risk.
- **Academic Evaluators**: Faculty evaluating coursework who require a clean, reproducible Java application that compiles and runs directly from the command line without complex environment setups.

---

## 4. High-Level Features

- **User Accounts & Authentication**: Password hashing using SHA-256, user login sessions, and virtual deposit/withdrawal capabilities.
- **Stock Market Catalog**: Live ticker displaying current prices, percentage change, day high/low, and trading volume for 10 pre-seeded Indian equities.
- **Double-Auction Order Book**: Separate bid and ask priority queues with Level-5 market depth visualization showing price, aggregated quantity, and order count.
- **Order Execution Engine**: Automatic detection of price crosses, trade execution at the maker's resting price, and tracking of partial fills.
- **Portfolio Tracking**: Real-time overview of available cash, invested capital, total portfolio value, unrealized P&L, and realized P&L per stock.
- **Market Price Simulation**: Automatic price updates driven by random walk fluctuations, macroeconomic headlines, and background bot orders.
- **Automated Verification**: Built-in test runner containing 14 unit test cases checking order validation, price-time priority, margin unfreezing, and balance invariance.
