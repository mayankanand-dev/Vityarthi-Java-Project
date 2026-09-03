package tradex.service;

import tradex.model.Stock;
import tradex.model.Trade;
import tradex.repository.StockRepository;
import tradex.repository.TradeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Functional analytics service utilizing Java Streams and Lambdas
 * for statistical rankings, volume distribution, and market leadership.
 */
public class AnalyticsService {
    private final StockRepository stockRepo;
    private final TradeRepository tradeRepo;

    public AnalyticsService(StockRepository stockRepo, TradeRepository tradeRepo) {
        this.stockRepo = stockRepo;
        this.tradeRepo = tradeRepo;
    }

    public List<Stock> getTopGainers(int limit) {
        return stockRepo.listAll().stream()
                .filter(s -> s.getChangePercentage() > 0)
                .sorted(Comparator.comparingDouble(Stock::getChangePercentage).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Stock> getTopLosers(int limit) {
        return stockRepo.listAll().stream()
                .filter(s -> s.getChangePercentage() < 0)
                .sorted(Comparator.comparingDouble(Stock::getChangePercentage))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Stock> getMostActiveByVolume(int limit) {
        return stockRepo.listAll().stream()
                .sorted(Comparator.comparingLong(Stock::getVolume).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getSectorTurnover() {
        List<Stock> stocks = stockRepo.listAll();
        return stocks.stream()
                .collect(Collectors.groupingBy(
                        Stock::getSector,
                        Collectors.summingDouble(s -> s.getVolume() * s.getCurrentPrice())
                ));
    }

    public double getAverageMarketReturn() {
        return stockRepo.listAll().stream()
                .mapToDouble(Stock::getChangePercentage)
                .average()
                .orElse(0.0);
    }

    public double getTotalExchangeTurnover() {
        return tradeRepo.listAll().stream()
                .mapToDouble(Trade::getGrossAmount)
                .sum();
    }
}
