package tradex.test;

import tradex.database.DatabaseManager;
import tradex.exception.*;
import tradex.exchange.*;
import tradex.model.*;
import tradex.model.enums.*;
import tradex.repository.*;
import tradex.service.*;
import tradex.util.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            TRADEX COMPREHENSIVE AUTOMATED TEST SUITE               ║");
        System.out.println("║                  14 Course Evaluation Suites                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        long start = System.currentTimeMillis();

        // Use a clean in-memory SQLite database for test isolation
        DatabaseManager db = DatabaseManager.initializeCustom("jdbc:sqlite:data/tradex_test.db");
        db.resetDatabase();

        StockRepository stockRepo = new StockRepository(db);
        UserRepository userRepo = new UserRepository(db);
        AccountRepository accountRepo = new AccountRepository(db);
        HoldingRepository holdingRepo = new HoldingRepository(db);
        OrderRepository orderRepo = new OrderRepository(db);
        TradeRepository tradeRepo = new TradeRepository(db);
        TransactionRepository txRepo = new TransactionRepository(db);

        SettlementEngine settlement = new SettlementEngine(accountRepo, holdingRepo, tradeRepo, txRepo);
        MatchingEngine matching = new MatchingEngine(settlement, orderRepo, stockRepo);
        Exchange exchange = new Exchange(matching, stockRepo, orderRepo, accountRepo, holdingRepo);

        // Seed test instruments
        try {
            stockRepo.save(new Stock("TEST1", "Test Stock 1", "Tech", 100.00));
            stockRepo.save(new Stock("TEST2", "Test Stock 2", "Finance", 500.00));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        exchange.initializeBooks();

        // 1. Valid Market Order
        runTest("Test 01: Valid Market Order", () -> {
            stockRepo.save(new Stock("TEST_MKT", "Market Test Stock", "Tech", 100.00));
            exchange.initializeBooks();

            User u1 = userRepo.save(new User("mkt_buyer", "hash", "TRADER"));
            Account a1 = accountRepo.save(new Account(u1.getId(), 50000.0));
            User u2 = userRepo.save(new User("mkt_seller", "hash", "TRADER"));
            Account a2 = accountRepo.save(new Account(u2.getId(), 10000.0));
            holdingRepo.saveOrUpdate(new Holding(a2.getAccountId(), "TEST_MKT", 100, 95.00));

            // Seller places resting limit ask at 100.00
            exchange.submitOrder(new Order.Builder()
                    .accountId(a2.getAccountId()).symbol("TEST_MKT").side(OrderSide.SELL)
                    .type(OrderType.LIMIT).quantity(20).price(100.00).build());

            // Buyer submits Market Buy for 10 shares
            List<Trade> trades = exchange.submitOrder(new Order.Builder()
                    .accountId(a1.getAccountId()).symbol("TEST_MKT").side(OrderSide.BUY)
                    .type(OrderType.MARKET).quantity(10).price(100.00).build());

            assertTrue(trades.size() == 1, "Expected 1 trade execution");
            assertTrue(trades.get(0).getQuantity() == 10, "Expected 10 shares executed");
            assertTrue(trades.get(0).getPrice() == 100.00, "Expected 100.00 execution price");
        });

        // 2. Invalid Quantity
        runTest("Test 02: Invalid Order Quantity Rejection", () -> {
            User u = userRepo.save(new User("qty_user", "hash", "TRADER"));
            Account a = accountRepo.save(new Account(u.getId(), 50000.0));

            boolean caught = false;
            try {
                new Order.Builder()
                        .accountId(a.getAccountId()).symbol("TEST1").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(0).price(100.00).build();
            } catch (IllegalArgumentException e) {
                caught = true;
            }
            assertTrue(caught, "Expected IllegalArgumentException on non-positive quantity");
        });

        // 3. Insufficient Funds
        runTest("Test 03: Insufficient Funds Validation", () -> {
            User u = userRepo.save(new User("poor_buyer", "hash", "TRADER"));
            Account a = accountRepo.save(new Account(u.getId(), 100.0)); // Only Rs.100

            boolean caught = false;
            try {
                // Wants to buy Rs.10,000 worth
                Order order = new Order.Builder()
                        .accountId(a.getAccountId()).symbol("TEST1").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(100).price(100.00).build();
                exchange.submitOrder(order);
            } catch (InsufficientFundsException e) {
                caught = true;
            } catch (TradeXException ignored) {}
            assertTrue(caught, "Expected InsufficientFundsException for order exceeding cash balance");
        });

        // 4. Insufficient Holdings
        runTest("Test 04: Insufficient Holdings Validation", () -> {
            User u = userRepo.save(new User("short_seller", "hash", "TRADER"));
            Account a = accountRepo.save(new Account(u.getId(), 50000.0));
            // No shares held in TEST1

            boolean caught = false;
            try {
                Order order = new Order.Builder()
                        .accountId(a.getAccountId()).symbol("TEST1").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(50).price(100.00).build();
                exchange.submitOrder(order);
            } catch (InsufficientHoldingsException e) {
                caught = true;
            } catch (TradeXException ignored) {}
            assertTrue(caught, "Expected InsufficientHoldingsException for naked short selling");
        });

        // 5. Limit Order Matching
        runTest("Test 05: Limit-Order Price Cross Matching", () -> {
            stockRepo.save(new Stock("TEST_LIM", "Limit Test Stock", "Tech", 100.00));
            exchange.initializeBooks();

            User u1 = userRepo.save(new User("lim_buyer", "hash", "TRADER"));
            Account a1 = accountRepo.save(new Account(u1.getId(), 50000.0));
            User u2 = userRepo.save(new User("lim_seller", "hash", "TRADER"));
            Account a2 = accountRepo.save(new Account(u2.getId(), 50000.0));
            holdingRepo.saveOrUpdate(new Holding(a2.getAccountId(), "TEST_LIM", 100, 95.00));

            // Seller asks Rs.102.00
            exchange.submitOrder(new Order.Builder()
                    .accountId(a2.getAccountId()).symbol("TEST_LIM").side(OrderSide.SELL)
                    .type(OrderType.LIMIT).quantity(25).price(102.00).build());

            // Buyer bids Rs.103.00 (crosses book!)
            List<Trade> trades = exchange.submitOrder(new Order.Builder()
                    .accountId(a1.getAccountId()).symbol("TEST_LIM").side(OrderSide.BUY)
                    .type(OrderType.LIMIT).quantity(25).price(103.00).build());

            assertTrue(trades.size() == 1, "Expected 1 matching trade");
            assertTrue(trades.get(0).getPrice() == 102.00, "Execution price should be resting ask price Rs.102.00");
            assertTrue(trades.get(0).getQuantity() == 25, "Trade quantity should be 25");
        });

        // 6. Partial Order Fill
        runTest("Test 06: Partial Order Fill Handling", () -> {
            stockRepo.save(new Stock("TEST_PART", "Partial Fill Stock", "Tech", 100.00));
            exchange.initializeBooks();

            User u1 = userRepo.save(new User("part_buyer", "hash", "TRADER"));
            Account a1 = accountRepo.save(new Account(u1.getId(), 100000.0));
            User u2 = userRepo.save(new User("part_seller", "hash", "TRADER"));
            Account a2 = accountRepo.save(new Account(u2.getId(), 10000.0));
            holdingRepo.saveOrUpdate(new Holding(a2.getAccountId(), "TEST_PART", 20, 95.00));

            // Resting ask for only 15 shares
            exchange.submitOrder(new Order.Builder()
                    .accountId(a2.getAccountId()).symbol("TEST_PART").side(OrderSide.SELL)
                    .type(OrderType.LIMIT).quantity(15).price(100.00).build());

            // Buyer wants 40 shares
            Order buyOrder = new Order.Builder()
                    .accountId(a1.getAccountId()).symbol("TEST_PART").side(OrderSide.BUY)
                    .type(OrderType.LIMIT).quantity(40).price(100.00).build();
            List<Trade> trades = exchange.submitOrder(buyOrder);

            assertTrue(trades.size() == 1, "Expected 1 trade for 15 shares");
            assertTrue(buyOrder.getFilledQuantity() == 15, "Filled quantity should be 15");
            assertTrue(buyOrder.getRemainingQuantity() == 25, "Remaining quantity should be 25");
            assertTrue(buyOrder.getStatus() == OrderStatus.PARTIALLY_FILLED, "Order status should be PARTIALLY_FILLED");
        });

        // 7. Price-Time Priority
        runTest("Test 07: Strict Price-Time Priority in OrderBook", () -> {
            OrderBook book = new OrderBook("TEST_PRIORITY");
            Order bid1 = new Order.Builder().accountId(1).symbol("TEST_PRIORITY").side(OrderSide.BUY).type(OrderType.LIMIT).quantity(10).price(100.00).build();
            try { Thread.sleep(5); } catch (Exception ignored) {}
            Order bid2 = new Order.Builder().accountId(2).symbol("TEST_PRIORITY").side(OrderSide.BUY).type(OrderType.LIMIT).quantity(10).price(105.00).build(); // Higher price
            try { Thread.sleep(5); } catch (Exception ignored) {}
            Order bid3 = new Order.Builder().accountId(3).symbol("TEST_PRIORITY").side(OrderSide.BUY).type(OrderType.LIMIT).quantity(10).price(100.00).build(); // Same price as bid1, but later

            book.addOrder(bid1);
            book.addOrder(bid2);
            book.addOrder(bid3);

            Order first = book.pollBestBid();
            Order second = book.pollBestBid();
            Order third = book.pollBestBid();

            assertTrue(first == bid2, "Highest price order (105.00) must be polled first");
            assertTrue(second == bid1, "Earlier order at 100.00 must be polled before later order");
            assertTrue(third == bid3, "Later order at 100.00 must be polled last");
        });

        // 8. Order Cancellation
        runTest("Test 08: Order Cancellation and Margin Unfreeze", () -> {
            User u = userRepo.save(new User("canceller", "hash", "TRADER"));
            Account a = accountRepo.save(new Account(u.getId(), 50000.0));

            Order buyOrder = new Order.Builder()
                    .accountId(a.getAccountId()).symbol("TEST1").side(OrderSide.BUY)
                    .type(OrderType.LIMIT).quantity(20).price(90.00).build();
            exchange.submitOrder(buyOrder);

            double frozenBefore = accountRepo.findById(a.getAccountId()).get().getFrozenCash();
            assertTrue(frozenBefore > 0, "Cash should be frozen while order rests");

            boolean cancelled = exchange.cancelOrder(buyOrder.getOrderId(), a.getAccountId());
            assertTrue(cancelled, "Order should be successfully cancelled");

            double frozenAfter = accountRepo.findById(a.getAccountId()).get().getFrozenCash();
            assertTrue(frozenAfter == 0.0, "Frozen cash should be returned to 0 after cancellation");
        });

        // 9. Cash Settlement Correctness
        runTest("Test 09: Settlement Deducts Buyer Cash and Credits Seller Cash", () -> {
            User u1 = userRepo.save(new User("settle_b", "hash", "TRADER"));
            Account bAcc = accountRepo.save(new Account(u1.getId(), 10000.0));
            User u2 = userRepo.save(new User("settle_s", "hash", "TRADER"));
            Account sAcc = accountRepo.save(new Account(u2.getId(), 5000.0));

            Trade trade = new Trade(null, "ord1", "ord2", bAcc.getAccountId(), sAcc.getAccountId(),
                    "TEST1", 10, 100.00, 10.0, 10.0, null);

            settlement.settle(trade);

            Account updatedBuyer = accountRepo.findById(bAcc.getAccountId()).get();
            Account updatedSeller = accountRepo.findById(sAcc.getAccountId()).get();

            // Buyer debited 1000 + 10 = 1010 -> 10000 - 1010 = 8990
            assertTrue(Math.abs(updatedBuyer.getCashBalance() - 8990.0) < 0.01, "Buyer balance should be 8990.00");
            // Seller credited 1000 - 10 = 990 -> 5000 + 990 = 5990
            assertTrue(Math.abs(updatedSeller.getCashBalance() - 5990.0) < 0.01, "Seller balance should be 5990.00");
        });

        // 10. Holdings Settlement Correctness
        runTest("Test 10: Settlement Updates Holdings and Weighted Average Buy Price", () -> {
            int accId = 999;
            Holding h = new Holding(accId, "TEST1", 10, 100.00); // Cost: 1000
            h.addShares(10, 120.00);                             // Cost: 1200, Total: 2200 for 20 shares

            assertTrue(h.getQuantity() == 20, "Total quantity should be 20");
            assertTrue(Math.abs(h.getAverageBuyPrice() - 110.00) < 0.01, "Weighted average price should be 110.00");

            h.removeShares(5, 130.00); // Sold 5 shares at 130 -> Profit per share = 130 - 110 = 20 -> Realized P&L = +100
            assertTrue(h.getQuantity() == 15, "Remaining shares should be 15");
            assertTrue(Math.abs(h.getRealizedPnL() - 100.00) < 0.01, "Realized P&L should be 100.00");
        });

        // 11. Concurrency Consistency Test
        runTest("Test 11: Concurrent High-Contention Balance & Settlement Invariance", () -> {
            Account sharedAcc = new Account(1, 100000.0);
            int threads = 10;
            int opsPerThread = 500;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    for (int j = 0; j < opsPerThread; j++) {
                        sharedAcc.deposit(10.0);
                        sharedAcc.withdraw(10.0);
                    }
                    latch.countDown();
                });
            }

            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
            pool.shutdown();

            assertTrue(Math.abs(sharedAcc.getCashBalance() - 100000.0) < 0.001,
                    "Account balance must remain invariant after 10,000 concurrent deposits & withdrawals");
        });

        // 12. Technical Analysis Calculation
        runTest("Test 12: Quantitative Technical Indicator Calculation (SMA & RSI)", () -> {
            List<Double> prices = Arrays.asList(10.0, 12.0, 14.0, 16.0, 18.0);
            double sma5 = TechnicalAnalysisService.calculateSMA(prices, 5);
            assertTrue(Math.abs(sma5 - 14.0) < 0.001, "SMA of [10, 12, 14, 16, 18] should be 14.0");

            List<Double> rsiPrices = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                rsiPrices.add(100.0 + (i * 2.0)); // Strictly ascending series
            }
            double rsi = TechnicalAnalysisService.calculateRSI(rsiPrices, 14);
            assertTrue(rsi == 100.0, "RSI of purely upward trending prices must equal 100.0");
        });

        // 13. Database CRUD Operations
        runTest("Test 13: SQLite JDBC Persistence and Relational Integrity", () -> {
            Stock s = new Stock("DBTEST", "Database Test Stock", "Test", 250.00);
            stockRepo.save(s);

            Optional<Stock> loaded = stockRepo.findBySymbol("DBTEST");
            assertTrue(loaded.isPresent(), "Stock should be retrieved from SQLite");
            assertTrue(loaded.get().getName().equals("Database Test Stock"), "Stock attributes must match");

            loaded.get().updatePrice(260.00, 100);
            stockRepo.updatePriceAndVolume(loaded.get());

            Stock reloaded = stockRepo.findBySymbol("DBTEST").get();
            assertTrue(reloaded.getCurrentPrice() == 260.00, "Price update must persist in SQLite");
        });

        // 14. Java NIO.2 File Export
        runTest("Test 14: Java NIO.2 File Export Verification", () -> {
            List<Stock> stocks = stockRepo.listAll();
            Path path = FileManager.exportDailyMarketReport(stocks);

            assertTrue(Files.exists(path), "Exported file must exist on disk");
            assertTrue(Files.size(path) > 0, "Exported file must not be empty");
            List<String> lines = Files.readAllLines(path);
            assertTrue(lines.size() >= 2, "Exported CSV must contain header and data rows");
        });

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n====================================================================");
        System.out.printf(" TEST RUN COMPLETE: %d Passed, %d Failed (Duration: %d ms)%n", passed, failed, duration);
        System.out.println("====================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestRunnable test) {
        try {
            test.run();
            System.out.printf("  [PASS] %s%n", testName);
            passed++;
        } catch (Throwable t) {
            System.out.printf("  [FAIL] %s - %s%n", testName, t.getMessage());
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }
}
