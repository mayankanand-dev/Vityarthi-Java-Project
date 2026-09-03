package tradex.model;

import java.time.LocalDateTime;

/**
 * Market-moving headline event that impacts sector or stock sentiment.
 */
public class MarketEvent {
    private final int eventId;
    private final String headline;
    private final String symbol;       // null or empty if broad market event
    private final String sentiment;    // "BULLISH", "BEARISH", "NEUTRAL"
    private final double impactPct;    // simulated expected price shift percentage
    private final LocalDateTime timestamp;

    public MarketEvent(int eventId, String headline, String symbol, String sentiment, double impactPct, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.headline = headline;
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
        this.sentiment = sentiment;
        this.impactPct = impactPct;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public MarketEvent(String headline, String symbol, String sentiment, double impactPct) {
        this(0, headline, symbol, sentiment, impactPct, LocalDateTime.now());
    }

    public int getEventId() {
        return eventId;
    }

    public String getHeadline() {
        return headline;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSentiment() {
        return sentiment;
    }

    public double getImpactPct() {
        return impactPct;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Sentiment: %s (Impact: %+.1f%%)",
                symbol != null ? symbol : "BROAD MARKET", headline, sentiment, impactPct);
    }
}
