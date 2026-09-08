package tradex.simulation;

import tradex.exchange.Exchange;
import tradex.model.MarketEvent;
import tradex.model.Stock;
import tradex.repository.StockRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class MarketSimulationEngine {
    Exchange exchange;
    StockRepository stockRepo;
    ScheduledExecutorService scheduler;
    Random random;
    List<AutomatedTrader> automatedTraders = new CopyOnWriteArrayList<>();
    List<MarketEvent> newsHistory = new CopyOnWriteArrayList<>();
    Map<String, List<Double>> historicalPrices = new ConcurrentHashMap<>();
    volatile boolean isRunning = false;

    static final String[][] NEWS_CATALOG = {
            {"Reliance announces record quarterly net profit, beating estimates", "RELIANCE", "BULLISH", "2.8"},
            {"TCS secures mega multi-year $1.5B cloud transformation contract", "TCS", "BULLISH", "2.2"},
            {"Infosys lowers annual revenue growth guidance amidst global tech slowdown", "INFY", "BEARISH", "-2.5"},
            {"HDFC Bank reports solid loan growth and stable asset quality in Q2", "HDFCBANK", "BULLISH", "1.7"},
            {"ICICI Bank digital lending surges 35% year-over-year", "ICICIBANK", "BULLISH", "1.9"},
            {"ITC reports steady FMCG segment revenue surge of 14%", "ITC", "BULLISH", "1.4"},
            {"State Bank of India expands corporate credit pipeline by 18%", "SBIN", "BULLISH", "2.1"},
            {"L&T bags massive international EPC infrastructure contract in Middle East", "LT", "BULLISH", "3.0"},
            {"Wipro undertakes organizational restructuring, incurring restructuring cost", "WIPRO", "BEARISH", "-1.8"},
            {"HCLTech signs strategic AI collaboration with major enterprise partners", "HCLTECH", "BULLISH", "2.4"},
            {"Reserve Bank of India keeps policy repo rate unchanged at 6.50%", null, "NEUTRAL", "0.4"},
            {"Global crude oil prices stabilize, boosting Indian manufacturing outlook", null, "BULLISH", "1.1"}
    };

    public MarketSimulationEngine(Exchange exchange, StockRepository stockRepo, Long seed) {
        this.exchange = exchange;
        this.stockRepo = stockRepo;
        if (seed != null) {
            this.random = new Random(seed);
        } else {
            this.random = new Random();
        }
        this.scheduler = Executors.newScheduledThreadPool(4);
        initializePriceHistory();
    }

    public MarketSimulationEngine(Exchange exchange, StockRepository stockRepo) {
        this(exchange, stockRepo, null);
    }

    private void initializePriceHistory() {
        for (Stock s : stockRepo.listAll()) {
            List<Double> history = new ArrayList<>();
            double base = s.getPreviousClose();
            // generate 50 historical prices for technical indicator calculations
            for (int i = 0; i < 50; i++) {
                double drift = (random.nextDouble() - 0.49) * 0.015;
                base = Math.max(10.0, base * (1.0 + drift));
                history.add(Math.round(base * 100.0) / 100.0);
            }
            history.add(s.getCurrentPrice());
            historicalPrices.put(s.getSymbol(), history);
        }
    }

    public void registerTrader(AutomatedTrader trader) { automatedTraders.add(trader); }
    public List<AutomatedTrader> getRegisteredTraders() { return Collections.unmodifiableList(automatedTraders); }
    public List<Double> getPriceHistory(String symbol) { return historicalPrices.getOrDefault(symbol.toUpperCase(), Collections.emptyList()); }
    public List<MarketEvent> getNewsHistory() { return Collections.unmodifiableList(newsHistory); }

    public synchronized void startContinuousSimulation(long intervalMs) {
        if (isRunning) return;
        isRunning = true;
        scheduler.scheduleAtFixedRate(this::tick, 1000, intervalMs, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::triggerRandomNewsEvent, 10000, 25000, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopContinuousSimulation() {
        isRunning = false;
        scheduler.shutdown();
    }

    public boolean isRunning() { return isRunning; }

    public synchronized void stepSimulation() { tick(); }

    private void tick() {
        List<Stock> stocks = stockRepo.listAll();
        for (Stock stock : stocks) {
            // GBM price drift: small random walk each tick
            double drift = 0.0001;
            double volatility = 0.006;
            double shock = random.nextGaussian();
            double pctChange = drift + (volatility * shock);

            double currentPrice = stock.getCurrentPrice();
            double newPrice = Math.round((currentPrice * (1.0 + pctChange)) * 20.0) / 20.0;
            newPrice = Math.min(stock.getUpperCircuit(), Math.max(stock.getLowerCircuit(), newPrice));

            long simulatedTickVolume = 5 + random.nextInt(40);
            stock.updatePrice(newPrice, simulatedTickVolume);

            if (!historicalPrices.containsKey(stock.getSymbol())) {
                historicalPrices.put(stock.getSymbol(), new ArrayList<>());
            }
            List<Double> history = historicalPrices.get(stock.getSymbol());
            history.add(newPrice);
            if (history.size() > 100) history.remove(0);

            try {
                stockRepo.updatePriceAndVolume(stock);
            } catch (SQLException ignored) {}
        }

        for (AutomatedTrader bot : automatedTraders) {
            bot.run();
        }
    }

    public synchronized MarketEvent triggerRandomNewsEvent() {
        int idx = random.nextInt(NEWS_CATALOG.length);
        String[] meta = NEWS_CATALOG[idx];
        String headline = meta[0];
        String symbol = meta[1];
        String sentiment = meta[2];
        double impactPct = Double.parseDouble(meta[3]);

        MarketEvent event = new MarketEvent(headline, symbol, sentiment, impactPct);
        newsHistory.add(0, event);
        if (newsHistory.size() > 50) newsHistory.remove(newsHistory.size() - 1);

        if (symbol != null) {
            Optional<Stock> stockOpt = stockRepo.findBySymbol(symbol);
            if (stockOpt.isPresent()) {
                Stock s = stockOpt.get();
                double target = Math.round((s.getCurrentPrice() * (1.0 + (impactPct / 100.0))) * 20.0) / 20.0;
                target = Math.min(s.getUpperCircuit(), Math.max(s.getLowerCircuit(), target));
                s.updatePrice(target, 50);
                try { stockRepo.updatePriceAndVolume(s); } catch (SQLException ignored) {}
            }
        } else {
            // broad market event affects all stocks
            for (Stock s : stockRepo.listAll()) {
                double broadImpact = (impactPct * 0.4) + ((random.nextDouble() - 0.5) * 0.5);
                double target = Math.round((s.getCurrentPrice() * (1.0 + (broadImpact / 100.0))) * 20.0) / 20.0;
                target = Math.min(s.getUpperCircuit(), Math.max(s.getLowerCircuit(), target));
                s.updatePrice(target, 20);
                try { stockRepo.updatePriceAndVolume(s); } catch (SQLException ignored) {}
            }
        }

        return event;
    }
}
