package tradex.service;

import tradex.model.Stock;
import tradex.model.Trade;
import tradex.repository.StockRepository;
import tradex.repository.TradeRepository;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsService {
    StockRepository stockRepo;
    TradeRepository tradeRepo;

    public AnalyticsService(StockRepository stockRepo, TradeRepository tradeRepo) {
        this.stockRepo = stockRepo;
        this.tradeRepo = tradeRepo;
    }

    public List<Stock> getTopGainers(int limit) {
        List<Stock> allStocks = stockRepo.listAll();
        List<Stock> gainers = new ArrayList<>();
        for (Stock s : allStocks) {
            if (s.getChangePercentage() > 0) gainers.add(s);
        }
        // sort descending by change% - bubble sort
        for (int i = 0; i < gainers.size() - 1; i++) {
            for (int j = 0; j < gainers.size() - i - 1; j++) {
                if (gainers.get(j).getChangePercentage() < gainers.get(j + 1).getChangePercentage()) {
                    Stock temp = gainers.get(j);
                    gainers.set(j, gainers.get(j + 1));
                    gainers.set(j + 1, temp);
                }
            }
        }
        List<Stock> result = new ArrayList<>();
        for (int i = 0; i < limit && i < gainers.size(); i++) result.add(gainers.get(i));
        return result;
    }

    public List<Stock> getTopLosers(int limit) {
        List<Stock> allStocks = stockRepo.listAll();
        List<Stock> losers = new ArrayList<>();
        for (Stock s : allStocks) {
            if (s.getChangePercentage() < 0) losers.add(s);
        }
        for (int i = 0; i < losers.size() - 1; i++) {
            for (int j = 0; j < losers.size() - i - 1; j++) {
                if (losers.get(j).getChangePercentage() > losers.get(j + 1).getChangePercentage()) {
                    Stock temp = losers.get(j);
                    losers.set(j, losers.get(j + 1));
                    losers.set(j + 1, temp);
                }
            }
        }
        List<Stock> result = new ArrayList<>();
        for (int i = 0; i < limit && i < losers.size(); i++) result.add(losers.get(i));
        return result;
    }

    public List<Stock> getMostActiveByVolume(int limit) {
        List<Stock> allStocks = stockRepo.listAll();
        for (int i = 0; i < allStocks.size() - 1; i++) {
            for (int j = 0; j < allStocks.size() - i - 1; j++) {
                if (allStocks.get(j).getVolume() < allStocks.get(j + 1).getVolume()) {
                    Stock temp = allStocks.get(j);
                    allStocks.set(j, allStocks.get(j + 1));
                    allStocks.set(j + 1, temp);
                }
            }
        }
        List<Stock> result = new ArrayList<>();
        for (int i = 0; i < limit && i < allStocks.size(); i++) result.add(allStocks.get(i));
        return result;
    }

    public double getTotalExchangeTurnover() {
        List<Trade> allTrades = tradeRepo.listAll();
        double total = 0.0;
        for (Trade t : allTrades) total = total + t.getGrossAmount();
        return total;
    }

    public double getAverageMarketReturn() {
        List<Stock> stocks = stockRepo.listAll();
        if (stocks.isEmpty()) return 0.0;
        double totalChange = 0.0;
        for (Stock s : stocks) totalChange = totalChange + s.getChangePercentage();
        return totalChange / stocks.size();
    }

    public java.util.Map<String, Double> getSectorTurnover() {
        List<Stock> stocks = stockRepo.listAll();
        java.util.Map<String, Double> map = new java.util.HashMap<>();
        for (Stock s : stocks) {
            String sector = s.getSector();
            double t = s.getVolume() * s.getCurrentPrice();
            if (!map.containsKey(sector)) map.put(sector, 0.0);
            map.put(sector, map.get(sector) + t);
        }
        return map;
    }
}
