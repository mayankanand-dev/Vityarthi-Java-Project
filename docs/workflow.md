# TradeX CLI Stock Exchange — System Workflow Documentation

## 1. End-to-End Trading Lifecycle

```
[Trader CLI Input]
        │
        ▼
[Pre-Trade Risk Controls] ────(Violates Circuit Band)────► [Reject: CircuitBreakerException]
        │                                                           ▲
        ▼                                                           │
[Margin & Share Verification] ──(Insufficient Cash/Stock)───────────┤
        │
        ▼ (Validation Passed)
[Order Routing to OrderBook]
        │
        ├─────────────────────────────────────────┐
        ▼ (No Cross Match Found)                  ▼ (Cross Condition Met)
[Resting Order in Book]                   [Continuous Matching Engine]
        │                                         │
        │                                         ▼
        │                                 [Trade Generation]
        │                                 (Maker Price + Fee)
        │                                         │
        │                                         ▼
        │                                 [Atomic Settlement]
        │                                 - Cash Debit/Credit
        │                                 - Shares Transferred
        │                                 - Recalculate Cost Basis
        │                                         │
        └─────────────────────────────────────────┴─────────► [SQLite Ledger & Audit Log]
                                                                      │
                                                                      ▼
                                                              [Portfolio & Net Worth]
```

---

## 2. Order Placement and Execution Scenarios

### Scenario A: Resting Limit Order Placement
1. **User Request**: Buyer enters `BUY 25 RELIANCE LIMIT ₹2840.00`.
2. **Circuit Check**: Current LTP is ₹2850.00. Permitted band is ₹2565.00 to ₹3135.00. ₹2840.00 is valid.
3. **Cash Reservation**: Required cash is $25 \times 2840.00 = ₹71,000.00$ plus brokerage. Available cash is verified and ₹71,000.00 is frozen.
4. **Order Book Placement**: Best resting ask is ₹2850.00. Since bid (₹2840.00) < ask (₹2850.00), no match occurs.
5. **Book Insertion**: The order is added to the `bids` PriorityQueue. Because of price priority, it rests behind higher bids.

### Scenario B: Crossing Limit Order & Double-Auction Match
1. **User Request**: Buyer enters `BUY 15 RELIANCE LIMIT ₹2850.00`.
2. **Cross Detection**: Best resting ask is at ₹2850.00 (submitted by resting seller for 120 shares).
3. **Price Matching**: The matching engine identifies that $2850.00 \ge 2850.00$.
4. **Trade Execution**:
   - Execution price: ₹2850.00 (resting maker's price).
   - Quantity: 15 shares.
   - Buyer's order: 15 of 15 filled $\rightarrow$ status becomes `FILLED`.
   - Resting seller's order: 15 shares filled $\rightarrow$ remaining quantity becomes 105, status becomes `PARTIALLY_FILLED`.
5. **Settlement**:
   - Buyer cash: ₹42,750 principal + ₹21.38 brokerage debited from frozen balance.
   - Seller cash: ₹42,750 principal - ₹21.38 brokerage credited.
   - Buyer holdings: +15 RELIANCE shares added, updating average cost basis.
   - Seller holdings: -15 RELIANCE shares debited, accruing realized P&L.
   - Trade record and audit transactions persisted to SQLite.

---

## 3. Evaluator 15-Step Verification Workflow

The application features a built-in automated walkthrough reproducing all 15 university evaluation steps:

| Step | Operation | Technical Mechanism |
| :--- | :--- | :--- |
| **01** | Start TradeX | Initializes `DatabaseManager` and SQLite embedded schema. |
| **02** | Trader Login | Authenticates demo account `demo` / `demo123` via SHA-256 password hash. |
| **03** | View Market | Reads all 10 listed bluechip equities from SQLite repository. |
| **04** | Stock Details | Displays circuit bands, 52-week ranges, open/high/low, and volume. |
| **05** | Order Book | Queries Dual PriorityQueue bids and asks depth. |
| **06** | Place Limit Order | Inserts resting buy order below best ask, freezing margin. |
| **07** | Market Simulation | Advances stochastic price drift using Geometric Brownian Motion. |
| **08** | Crossing Order | Submits aggressive buy order matching resting ask. |
| **09** | Execution Match | Evaluates price-time priority andmaker pricing. |
| **10** | Trade Generation | Creates immutable `Trade` audit entity with brokerage calculation. |
| **11** | Portfolio Update | Verifies updated cash balance, stock quantity, and unrealized P&L. |
| **12** | Technical Analysis | Computes SMA(20), SMA(50), and RSI(14) indicators. |
| **13** | Report Export | Exports portfolio summary to CSV using Java NIO.2 (`Files.write`). |
| **14** | Restart App | Closes and restarts application session to test state persistence. |
| **15** | Confirm Persistence | Queries SQLite database and confirms balances and holdings match. |
