# TradeX — CLI Stock Trading & Exchange Simulator

- **Student Name**: Mayank Anand  
- **Registration No**: 25BAI11209  

A console-based stock trading platform and matching engine written in native Java. TradeX simulates the core operations of an equity exchange: limit order books, price-time priority matching, circuit breakers, portfolio valuation, and price simulation—all running locally in the terminal without external servers or frameworks.

---

## 1. Project Title
**TradeX — CLI Stock Trading & Exchange Simulator**

---

## 2. Overview of the Project
TradeX is an educational trading simulator built from scratch using pure Java. In many student projects, buying a stock is handled by a direct balance deduction and a simple count increase. TradeX models how real exchanges work by using an in-memory double-auction order book with two priority queues (bids and asks), enforcing price-time priority matching, executing trades at the resting maker's price, and settling cash and shares atomically.

The program runs entirely inside the command line, requires no web browser or external database server, and comes with a built-in automated test suite.

---

## 3. Features

- **Live Market View**: View live prices, percentage changes, daily high/low, and trading volume for 10 preloaded stocks (RELIANCE, TCS, INFY, HDFCBANK, ICICIBANK, ITC, SBIN, LT, WIPRO, HCLTECH).
- **Multiple Order Types**: Supports Market, Limit, Stop, and Stop-Limit orders for both buying and selling.
- **Double-Auction Order Book**: Maintains bids (max-heap) and asks (min-heap) with strict Price-Time priority and a Level-5 depth display.
- **Matching & Settlement Engine**: Automatically checks for crossing prices, matches orders, calculates brokerage, updates cash and holdings, and handles partial fills.
- **Portfolio & P&L Tracking**: Displays cash balance, stock holdings, average acquisition price, unrealized profit/loss, and total net worth.
- **Circuit Breaker Protection**: Daily 10% upper and lower price bands to block unrealistic order prices.
- **Market Simulation**: Simulates market price movement with random fluctuations, news events, and automated trading bots.
- **Technical Indicators**: Calculates 20-day and 50-day Simple Moving Averages (SMA) and the 14-day Relative Strength Index (RSI).
- **SQLite Persistence & CSV Export**: Automatically saves users, orders, trades, and holdings to an embedded SQLite database using JDBC, and exports reports to CSV files.

---

## 4. Technologies / Tools Used

- **Language**: Java (JDK 17 or higher)
- **Build System**: Native Java (`javac` and `jar` scripts) — no Maven or Gradle required
- **Database**: SQLite 3 via `sqlite-jdbc` (embedded, zero-configuration)
- **File I/O**: Java NIO.2 (`java.nio.file`) for CSV report generation
- **Logging**: SLF4J with SimpleLogger
- **Operating System**: Windows / Linux / macOS

---

## 5. Steps to Install & Run the Project

### Prerequisites
Make sure you have JDK 17 or higher installed:
```cmd
java -version
javac -version
```

### Installation
Clone or download the project folder:
```cmd
cd "vityarthi project"
```

### Building & Running

**On Windows (PowerShell):**
```powershell
# Build the application
.\build.bat

# Launch the interactive CLI
.\run.bat
```

**On Windows (Command Prompt):**
```cmd
build.bat
run.bat
```

**Direct Execution:**
```cmd
java -jar tradex.jar
```

*(On Linux or macOS, run `chmod +x *.sh` followed by `./build.sh` and `./run.sh`)*

### Default Login Accounts
On the first run, the program creates `data/tradex.db` and loads initial test accounts:
- **Trader Account**: Username `demo` | Password `demo123` (starts with ₹10,00,000 cash balance)
- **Admin Account**: Username `admin` | Password `admin123`

---

## 6. Instructions for Testing

An automated test suite containing 14 verification test cases is built into the project.

**On Windows (PowerShell):**
```powershell
.\test.bat
```

**On Windows (Command Prompt):**
```cmd
test.bat
```

*(On Linux / macOS: `./test.sh`)*

### What the Tests Verify:
1. **Valid Market Order Placement**: Tests execution of immediate market orders.
2. **Invalid Quantity Rejection**: Checks rejection of zero and negative quantities.
3. **Insufficient Funds Check**: Ensures buy orders fail if available balance is inadequate.
4. **Insufficient Holdings Check**: Ensures sell orders fail if shares are not owned.
5. **Limit Order Price Crossing**: Verifies that crossing limit orders execute properly.
6. **Partial Order Fills**: Verifies remainder order handling when volume is partially matched.
7. **Price-Time Priority**: Validates that orders with better prices and earlier timestamps match first.
8. **Order Cancellation & Margin Release**: Tests cancellation and freeing of blocked margin funds.
9. **Cash Settlement Accuracy**: Confirms buyer cash debit and seller cash credit.
10. **Holdings & Average Cost**: Tests share transfer and weighted average purchase price calculation.
11. **Concurrency Invariance**: Verifies account balance integrity under simultaneous operations.
12. **Technical Indicators**: Checks mathematical correctness of SMA and RSI formulas.
13. **Database Persistence**: Verifies relational integrity and foreign key constraints in SQLite.
14. **File Exporting**: Confirms correct generation of CSV export files via Java NIO.2.
