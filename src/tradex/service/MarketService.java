package tradex.service;

import tradex.model.Stock;
import tradex.repository.StockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketService {
    StockRepository stockRepo;

    public MarketService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    public List<Stock> getAllStocks() { return stockRepo.listAll(); }
    public Optional<Stock> getStock(String symbol) { return stockRepo.findBySymbol(symbol); }

    public List<Stock> searchStocks(String query) {
        String q = query.toLowerCase().trim();
        List<Stock> allStocks = stockRepo.listAll();
        List<Stock> results = new ArrayList<>();
        for (Stock s : allStocks) {
            if (s.getSymbol().toLowerCase().contains(q) || s.getName().toLowerCase().contains(q)
                    || s.getSector().toLowerCase().contains(q)) {
                results.add(s);
            }
        }
        return results;
    }

    // advances vs declines in the market
    public static class MarketBreadth {
        public int advances;
        public int declines;
        public int unchanged;
        public int total;

        public MarketBreadth(int advances, int declines, int unchanged, int total) {
            this.advances = advances;
            this.declines = declines;
            this.unchanged = unchanged;
            this.total = total;
        }
    }

    public MarketBreadth getMarketBreadth() {
        List<Stock> stocks = stockRepo.listAll();
        int advances = 0, declines = 0, unchanged = 0;
        for (Stock s : stocks) {
            double change = s.getChangeAmount();
            if (change > 0.001) advances++;
            else if (change < -0.001) declines++;
            else unchanged++;
        }
        return new MarketBreadth(advances, declines, unchanged, stocks.size());
    }

    public java.util.Map<String, List<Stock>> getStocksBySector() {
        List<Stock> allStocks = stockRepo.listAll();
        java.util.Map<String, List<Stock>> sectorMap = new java.util.HashMap<>();
        for (Stock s : allStocks) {
            String sector = s.getSector();
            if (!sectorMap.containsKey(sector)) sectorMap.put(sector, new ArrayList<>());
            sectorMap.get(sector).add(s);
        }
        return sectorMap;
    }
}
