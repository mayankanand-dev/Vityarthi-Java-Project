package tradex.util;

import tradex.database.DatabaseManager;
import tradex.exchange.Exchange;
import tradex.model.Account;
import tradex.model.Holding;
import tradex.model.Order;
import tradex.model.Stock;
import tradex.model.User;
import tradex.model.enums.MarketStatus;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderType;
import tradex.repository.AccountRepository;
import tradex.repository.HoldingRepository;
import tradex.repository.StockRepository;
import tradex.repository.UserRepository;
import tradex.service.AccountService;

import java.sql.SQLException;


public class DatabaseSeeder {

    public static void seed(DatabaseManager dbManager, Exchange exchange) {
        dbManager.initializeSchema();

        StockRepository stockRepo = new StockRepository(dbManager);
        UserRepository userRepo = new UserRepository(dbManager);
        AccountRepository accountRepo = new AccountRepository(dbManager);
        HoldingRepository holdingRepo = new HoldingRepository(dbManager);

        try {
            // 1. Seed 10 Bluechip Stocks
            Stock[] initialStocks = {
                    new Stock("RELIANCE", "Reliance Industries Ltd", "Energy & Retail", 2850.00),
                    new Stock("TCS", "Tata Consultancy Services", "Information Technology", 4120.00),
                    new Stock("INFY", "Infosys Limited", "Information Technology", 1780.00),
                    new Stock("HDFCBANK", "HDFC Bank Limited", "Banking & Finance", 1640.00),
                    new Stock("ICICIBANK", "ICICI Bank Limited", "Banking & Finance", 1180.00),
                    new Stock("ITC", "ITC Limited", "FMCG & Conglomerate", 495.00),
                    new Stock("SBIN", "State Bank of India", "Banking & Finance", 815.00),
                    new Stock("LT", "Larsen & Toubro Ltd", "Engineering & Infra", 3620.00),
                    new Stock("WIPRO", "Wipro Limited", "Information Technology", 530.00),
                    new Stock("HCLTECH", "HCL Technologies Ltd", "Information Technology", 1720.00)
            };

            for (Stock s : initialStocks) {
                stockRepo.save(s);
            }

            // 2. Seed Admin and Evaluator Accounts
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User("admin", AccountService.hashPassword("admin123"), "ADMIN");
                User savedAdmin = userRepo.save(admin);
                accountRepo.save(new Account(savedAdmin.getId(), 5000000.0));
            }

            User trader;
            if (userRepo.findByUsername("demo").isEmpty()) {
                trader = new User("demo", AccountService.hashPassword("demo123"), "TRADER");
                trader = userRepo.save(trader);
                Account demoAcc = accountRepo.save(new Account(trader.getId(), 1000000.0)); // Rs.10,00,000 initial capital

                // Give demo trader initial inventory in RELIANCE & TCS
                holdingRepo.saveOrUpdate(new Holding(demoAcc.getAccountId(), "RELIANCE", 100, 2800.00));
                holdingRepo.saveOrUpdate(new Holding(demoAcc.getAccountId(), "TCS", 50, 4050.00));
            }

            // 3. Seed Automated Liquidity Bot User & Account
            User botUser;
            Account botAccount;
            if (userRepo.findByUsername("liquidity_bot").isEmpty()) {
                botUser = userRepo.save(new User("liquidity_bot", AccountService.hashPassword("botpass"), "TRADER"));
                botAccount = accountRepo.save(new Account(botUser.getId(), 10000000.0)); // Rs.1 Crore bot capital

                // Seed ample holdings for the bot across all stocks so it can quote both bids and asks
                for (Stock s : initialStocks) {
                    holdingRepo.saveOrUpdate(new Holding(botAccount.getAccountId(), s.getSymbol(), 1000, s.getCurrentPrice()));
                }
            } else {
                botUser = userRepo.findByUsername("liquidity_bot").get();
                botAccount = accountRepo.findByUserId(botUser.getId()).orElse(null);
            }

            // 4. Pre-seed Order Book with initial resting bids and asks for RELIANCE and TCS
            if (exchange != null && botAccount != null) {
                exchange.initializeBooks();

                // RELIANCE Order Book Depth
                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(120).price(2860.00).build());
                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(300).price(2855.00).build());
                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(180).price(2850.00).build());

                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(250).price(2840.00).build());
                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(410).price(2835.00).build());
                exchange.getOrderBook("RELIANCE").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("RELIANCE").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(175).price(2830.00).build());

                // TCS Order Book Depth
                exchange.getOrderBook("TCS").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("TCS").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(50).price(4140.00).build());
                exchange.getOrderBook("TCS").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("TCS").side(OrderSide.SELL)
                        .type(OrderType.LIMIT).quantity(100).price(4130.00).build());

                exchange.getOrderBook("TCS").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("TCS").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(80).price(4110.00).build());
                exchange.getOrderBook("TCS").addOrder(new Order.Builder()
                        .accountId(botAccount.getAccountId()).symbol("TCS").side(OrderSide.BUY)
                        .type(OrderType.LIMIT).quantity(120).price(4100.00).build());
            }

        } catch (SQLException e) {
            System.err.println("[DatabaseSeeder] Seeding error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Seeding TradeX SQLite Database...");
        DatabaseManager dbManager = DatabaseManager.getInstance();
        seed(dbManager, null);
        System.out.println("Seeding completed successfully!");
    }
}

