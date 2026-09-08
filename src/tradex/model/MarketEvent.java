package tradex.model;

import java.time.LocalDateTime;

public class MarketEvent {
    int eventId;
    String headline;
    String symbol;
    String sentiment; // BULLISH, BEARISH, NEUTRAL
    double impactPct;
    LocalDateTime timestamp;

    public MarketEvent(int eventId, String headline, String symbol, String sentiment, double impactPct, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.headline = headline;
        if (symbol != null) {
            this.symbol = symbol.toUpperCase().trim();
        } else {
            this.symbol = null;
        }
        this.sentiment = sentiment;
        this.impactPct = impactPct;
        if (timestamp != null) {
            this.timestamp = timestamp;
        } else {
            this.timestamp = LocalDateTime.now();
        }
    }

    public MarketEvent(String headline, String symbol, String sentiment, double impactPct) {
        this(0, headline, symbol, sentiment, impactPct, LocalDateTime.now());
    }

    public int getEventId() { return eventId; }
    public String getHeadline() { return headline; }
    public String getSymbol() { return symbol; }
    public String getSentiment() { return sentiment; }
    public double getImpactPct() { return impactPct; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        String s;
        if (symbol != null) {
            s = symbol;
        } else {
            s = "BROAD MARKET";
        }
        return "[" + s + "] " + headline + " | Sentiment: " + sentiment + " (Impact: " + impactPct + "%)";
    }
}
