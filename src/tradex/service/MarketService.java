package tradex.service;

import tradex.model.Stock;
import tradex.model.enums.MarketStatus;
import tradex.repository.StockRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MarketService {
    private final StockRepository stockRepo;

    public MarketService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    public List<Stock> getAllStocks() {
        return stockRepo.listAll();
    }

    public Optional<Stock> getStock(String symbol) {
        return stockRepo.findBySymbol(symbol);
    }

    public List<Stock> searchStocks(String query) {
        String q = query.toLowerCase().trim();
        return stockRepo.listAll().stream()
                .filter(s -> s.getSymbol().toLowerCase().contains(q) ||
                        s.getName().toLowerCase().contains(q) ||
                        s.getSector().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static class MarketBreadth {
        public final int advances;
        public final int declines;
        public final int unchanged;
        public final int total;

        public MarketBreadth(int advances, int declines, int unchanged, int total) {
            this.advances = advances;
            this.declines = declines;
            this.unchanged = unchanged;
            this.total = total;
        }
    }

    public MarketBreadth getMarketBreadth() {
        List<Stock> stocks = stockRepo.listAll();
        int advances = 0;
        int declines = 0;
        int unchanged = 0;

        for (Stock s : stocks) {
            double change = s.getChangeAmount();
            if (change > 0.001) advances++;
            else if (change < -0.001) declines++;
            else unchanged++;
        }

        return new MarketBreadth(advances, declines, unchanged, stocks.size());
    }

    public Map<String, List<Stock>> getStocksBySector() {
        return stockRepo.listAll().stream()
                .collect(Collectors.groupingBy(Stock::getSector));
    }
}
