package tradex.model;

public class Holding {
    int holdingId;
    int accountId;
    String symbol;
    int quantity;
    double averageBuyPrice;
    double realizedPnL;

    public Holding(int holdingId, int accountId, String symbol, int quantity, double averageBuyPrice, double realizedPnL) {
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.symbol = symbol.toUpperCase().trim();
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.realizedPnL = realizedPnL;
    }

    public Holding(int accountId, String symbol, int quantity, double averageBuyPrice) {
        this(0, accountId, symbol, quantity, averageBuyPrice, 0.0);
    }

    public int getHoldingId() { return holdingId; }
    public int getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public synchronized int getQuantity() { return quantity; }
    public synchronized double getAverageBuyPrice() { return averageBuyPrice; }
    public synchronized double getRealizedPnL() { return realizedPnL; }

    // when buying more shares, update average price
    public synchronized void addShares(int additionalQty, double purchasePrice) {
        if (additionalQty <= 0) return;
        double currentTotalCost = this.quantity * this.averageBuyPrice;
        double newCost = additionalQty * purchasePrice;
        this.quantity = this.quantity + additionalQty;
        // weighted average formula
        this.averageBuyPrice = (currentTotalCost + newCost) / this.quantity;
    }

    public synchronized void removeShares(int soldQty, double sellPrice) {
        if (soldQty <= 0) return;
        if (soldQty > this.quantity) {
            throw new IllegalStateException("Cannot sell more shares than held in account");
        }
        double profitPerShare = sellPrice - this.averageBuyPrice;
        this.realizedPnL = this.realizedPnL + (profitPerShare * soldQty);
        this.quantity = this.quantity - soldQty;
        if (this.quantity == 0) {
            this.averageBuyPrice = 0.0;
        }
    }

    public synchronized double getInvestedValue() { return quantity * averageBuyPrice; }
    public synchronized double getCurrentValue(double ltp) { return quantity * ltp; }
    public synchronized double getUnrealizedPnL(double ltp) { return getCurrentValue(ltp) - getInvestedValue(); }

    public synchronized double getUnrealizedPnLPct(double ltp) {
        double invested = getInvestedValue();
        if (invested == 0.0) return 0.0;
        return (getUnrealizedPnL(ltp) / invested) * 100.0;
    }

    @Override
    public String toString() {
        return "Holding[" + symbol + " Qty:" + quantity + " Avg:Rs." + averageBuyPrice + " RealizedPnL:Rs." + realizedPnL + "]";
    }
}
