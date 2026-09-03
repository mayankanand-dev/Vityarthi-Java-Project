package tradex.simulation;

import tradex.exchange.Exchange;
import tradex.model.MarketEvent;
import tradex.model.Stock;
import tradex.repository.StockRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * High-performance market simulation engine driven by a Geometric Brownian Motion
 * stochastic process, deterministic random seeds, macroeconomic news events,
 * and concurrent algorithmic bot agents.
 */
public class MarketSimulationEngine {
    private final Exchange exchange;
    private final StockRepository stockRepo;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private final List<AutomatedTrader> automatedTraders = new CopyOnWriteArrayList<>();
    private final List<MarketEvent> newsHistory = new CopyOnWriteArrayList<>();
    private final Map<String, List<Double>> historicalPrices = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;

    // News headlines catalog for realistic event injection
    private static final String[][] NEWS_CATALOG = {
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
        this.random = seed != null ? new Random(seed) : new Random();
        this.scheduler = Executors.newScheduledThreadPool(4);
        initializePriceHistory();
    }

    public MarketSimulationEngine(Exchange exchange, StockRepository stockRepo) {
        this(exchange, stockRepo, null);
    }

    private void initializePriceHistory() {
        for (Stock s : stockRepo.listAll()) {
            List<Double> history = new ArrayList<>();
            // Generate synthetic 50-day preceding history around previous close
            double base = s.getPreviousClose();
            for (int i = 0; i < 50; i++) {
                double drift = (random.nextDouble() - 0.49) * 0.015;
                base = Math.max(10.0, base * (1.0 + drift));
                history.add(Math.round(base * 100.0) / 100.0);
            }
            history.add(s.getCurrentPrice());
            historicalPrices.put(s.getSymbol(), history);
        }
    }

    public void registerTrader(AutomatedTrader trader) {
        automatedTraders.add(trader);
    }

    public List<AutomatedTrader> getRegisteredTraders() {
        return Collections.unmodifiableList(automatedTraders);
    }

    public List<Double> getPriceHistory(String symbol) {
        return historicalPrices.getOrDefault(symbol.toUpperCase(), Collections.emptyList());
    }

    public List<MarketEvent> getNewsHistory() {
        return Collections.unmodifiableList(newsHistory);
    }

    public synchronized void startContinuousSimulation(long intervalMs) {
        if (isRunning) return;
        isRunning = true;

        // Schedule periodic price tick & bot executions
        scheduler.scheduleAtFixedRate(this::tick, 1000, intervalMs, TimeUnit.MILLISECONDS);

        // Schedule random news events every 20-30 ticks
        scheduler.scheduleAtFixedRate(this::triggerRandomNewsEvent, 10000, 25000, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopContinuousSimulation() {
        isRunning = false;
        scheduler.shutdown();
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Executes a single discrete simulation step:
     * 1. Updates stock price drifts using Geometric Brownian Motion.
     * 2. Executes automated trader bots.
     */
    public synchronized void stepSimulation() {
        tick();
    }

    private void tick() {
        List<Stock> stocks = stockRepo.listAll();
        for (Stock stock : stocks) {
            // Geometric Brownian Motion step: dS = S * (mu*dt + sigma*dW)
            // Daily drift mu ~ 0.0002, volatility sigma ~ 0.008
            double drift = 0.0001;
            double volatility = 0.006;
            double shock = random.nextGaussian();
            double pctChange = drift + (volatility * shock);

            double currentPrice = stock.getCurrentPrice();
            double newPrice = Math.round((currentPrice * (1.0 + pctChange)) * 20.0) / 20.0;

            // Constrain strictly within circuit limits
            newPrice = Math.min(stock.getUpperCircuit(), Math.max(stock.getLowerCircuit(), newPrice));

            long simulatedTickVolume = 5 + random.nextInt(40);
            stock.updatePrice(newPrice, simulatedTickVolume);

            // Record into historical series
            List<Double> history = historicalPrices.computeIfAbsent(stock.getSymbol(), k -> new ArrayList<>());
            history.add(newPrice);
            if (history.size() > 100) {
                history.remove(0);
            }

            try {
                stockRepo.updatePriceAndVolume(stock);
            } catch (SQLException ignored) {}
        }

        // Trigger bot strategy evaluations
        for (AutomatedTrader bot : automatedTraders) {
            bot.run();
        }
    }

    /**
     * Injects a market news event and applies immediate directional impact.
     */
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

        // Apply impact
        if (symbol != null) {
            stockRepo.findBySymbol(symbol).ifPresent(s -> {
                double target = Math.round((s.getCurrentPrice() * (1.0 + (impactPct / 100.0))) * 20.0) / 20.0;
                target = Math.min(s.getUpperCircuit(), Math.max(s.getLowerCircuit(), target));
                s.updatePrice(target, 50);
                try {
                    stockRepo.updatePriceAndVolume(s);
                } catch (SQLException ignored) {}
            });
        } else {
            // Broad market event: impacts all stocks moderately
            for (Stock s : stockRepo.listAll()) {
                double broadImpact = (impactPct * 0.4) + ((random.nextDouble() - 0.5) * 0.5);
                double target = Math.round((s.getCurrentPrice() * (1.0 + (broadImpact / 100.0))) * 20.0) / 20.0;
                target = Math.min(s.getUpperCircuit(), Math.max(s.getLowerCircuit(), target));
                s.updatePrice(target, 20);
                try {
                    stockRepo.updatePriceAndVolume(s);
                } catch (SQLException ignored) {}
            }
        }

        return event;
    }
}
