package tradex.model;

import tradex.model.enums.MarketStatus;

public class Stock {
    String symbol;
    String name;
    String sector;
    double currentPrice;
    double previousClose;
    double dayOpen;
    double dayHigh;
    double dayLow;
    long volume;
    double fiftyTwoWeekHigh;
    double fiftyTwoWeekLow;
    double upperCircuit;
    double lowerCircuit;
    MarketStatus status;

    public Stock(String symbol, String name, String sector, double currentPrice, double previousClose,
                 double dayOpen, double dayHigh, double dayLow, long volume,
                 double fiftyTwoWeekHigh, double fiftyTwoWeekLow, double upperCircuit, double lowerCircuit,
                 MarketStatus status) {
        this.symbol = symbol.toUpperCase().trim();
        this.name = name;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.dayOpen = dayOpen;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.volume = volume;
        this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
        this.fiftyTwoWeekLow = fiftyTwoWeekLow;
        this.upperCircuit = upperCircuit;
        this.lowerCircuit = lowerCircuit;
        if (status == null) {
            this.status = MarketStatus.OPEN;
        } else {
            this.status = status;
        }
    }

    // simple constructor with just basic info
    public Stock(String symbol, String name, String sector, double basePrice) {
        this(symbol, name, sector, basePrice, basePrice, basePrice, basePrice, basePrice, 0L,
                basePrice * 1.35, basePrice * 0.70, basePrice * 1.10, basePrice * 0.90, MarketStatus.OPEN);
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getSector() { return sector; }

    public synchronized double getCurrentPrice() { return currentPrice; }
    public synchronized double getPreviousClose() { return previousClose; }
    public synchronized double getDayOpen() { return dayOpen; }
    public synchronized double getDayHigh() { return dayHigh; }
    public synchronized double getDayLow() { return dayLow; }
    public synchronized long getVolume() { return volume; }
    public synchronized double getFiftyTwoWeekHigh() { return fiftyTwoWeekHigh; }
    public synchronized double getFiftyTwoWeekLow() { return fiftyTwoWeekLow; }
    public synchronized double getUpperCircuit() { return upperCircuit; }
    public synchronized double getLowerCircuit() { return lowerCircuit; }
    public synchronized MarketStatus getStatus() { return status; }

    public synchronized void setStatus(MarketStatus status) {
        this.status = status;
    }

    public synchronized double getChangeAmount() {
        double change = currentPrice - previousClose;
        return change;
    }

    public synchronized double getChangePercentage() {
        if (previousClose == 0.0) {
            return 0.0;
        }
        double pct = ((currentPrice - previousClose) / previousClose) * 100.0;
        return pct;
    }

    public synchronized void updatePrice(double newPrice, long tradedQty) {
        if (newPrice <= 0) return;
        this.currentPrice = newPrice;
        if (newPrice > this.dayHigh) this.dayHigh = newPrice;
        if (newPrice < this.dayLow) this.dayLow = newPrice;
        if (newPrice > this.fiftyTwoWeekHigh) this.fiftyTwoWeekHigh = newPrice;
        if (newPrice < this.fiftyTwoWeekLow) this.fiftyTwoWeekLow = newPrice;
        this.volume = this.volume + tradedQty;
    }

    // returns true if price is outside allowed circuit limits
    public synchronized boolean isCircuitBreached(double targetPrice) {
        if (targetPrice > upperCircuit) return true;
        if (targetPrice < lowerCircuit) return true;
        return false;
    }

    @Override
    public String toString() {
        return symbol + " " + name + " Rs." + currentPrice + " (" + getChangePercentage() + "%) Vol:" + volume;
    }
}
