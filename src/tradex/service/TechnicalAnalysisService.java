package tradex.service;

import tradex.model.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes authentic quantitative technical indicators:
 * - Simple Moving Average (SMA)
 * - Exponential Moving Average (EMA)
 * - Relative Strength Index (RSI - 14)
 * - Historical Volatility
 * - Price Momentum & Signal Generation
 */
public class TechnicalAnalysisService {

    public static double calculateSMA(List<Double> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return 0.0;
        }
        int start = prices.size() - period;
        double sum = 0.0;
        for (int i = start; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    public static double calculateEMA(List<Double> prices, int period) {
        if (prices == null || prices.isEmpty() || period <= 0) {
            return 0.0;
        }
        if (prices.size() < period) {
            return calculateSMA(prices, prices.size());
        }

        double multiplier = 2.0 / (period + 1.0);
        // Seed EMA with the initial SMA
        double ema = calculateSMA(prices.subList(0, period), period);

        for (int i = period; i < prices.size(); i++) {
            ema = ((prices.get(i) - ema) * multiplier) + ema;
        }
        return ema;
    }

    /**
     * Calculates the Relative Strength Index (RSI) across a specified period (typically 14).
     */
    public static double calculateRSI(List<Double> prices, int period) {
        if (prices == null || prices.size() <= period || period <= 0) {
            return 50.0; // Default neutral level if insufficient history
        }

        double gains = 0.0;
        double losses = 0.0;

        for (int i = prices.size() - period; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains += change;
            } else {
                losses += Math.abs(change);
            }
        }

        double avgGain = gains / period;
        double avgLoss = losses / period;

        if (avgLoss == 0.0) {
            return 100.0;
        }

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    public static double calculateVolatility(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            return 0.0;
        }
        // Calculate daily percentage returns
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            if (prev > 0) {
                returns.add((prices.get(i) - prev) / prev);
            }
        }
        if (returns.isEmpty()) return 0.0;

        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = 0.0;
        for (double r : returns) {
            variance += Math.pow(r - mean, 2);
        }
        return Math.sqrt(variance / returns.size()) * 100.0;
    }

    public static class TechnicalReport {
        public final String symbol;
        public final double ltp;
        public final double sma20;
        public final double sma50;
        public final double ema20;
        public final double rsi14;
        public final double volatility;
        public final double momentum;
        public final String signal; // "STRONG BULLISH", "BULLISH", "NEUTRAL", "BEARISH", "STRONG BEARISH"

        public TechnicalReport(String symbol, double ltp, double sma20, double sma50,
                               double ema20, double rsi14, double volatility,
                               double momentum, String signal) {
            this.symbol = symbol;
            this.ltp = ltp;
            this.sma20 = sma20;
            this.sma50 = sma50;
            this.ema20 = ema20;
            this.rsi14 = rsi14;
            this.volatility = volatility;
            this.momentum = momentum;
            this.signal = signal;
        }
    }

    public TechnicalReport generateReport(Stock stock, List<Double> priceSeries) {
        List<Double> series = new ArrayList<>(priceSeries != null ? priceSeries : Collections.emptyList());
        if (series.isEmpty() || series.get(series.size() - 1) != stock.getCurrentPrice()) {
            series.add(stock.getCurrentPrice());
        }

        double ltp = stock.getCurrentPrice();
        double sma20 = calculateSMA(series, Math.min(series.size(), 20));
        double sma50 = calculateSMA(series, Math.min(series.size(), 50));
        double ema20 = calculateEMA(series, Math.min(series.size(), 20));
        double rsi14 = calculateRSI(series, Math.min(series.size() - 1, 14));
        double vol = calculateVolatility(series);

        double momentum = series.size() >= 5 ?
                ((ltp - series.get(series.size() - 5)) / series.get(series.size() - 5)) * 100.0 : 0.0;

        // Signal scoring model
        int score = 0;
        if (ltp > sma20 && sma20 > 0) score++;
        if (sma20 > sma50 && sma50 > 0) score++;
        if (rsi14 < 30) score += 2; // Oversold -> Buy opportunity
        else if (rsi14 > 70) score -= 2; // Overbought -> Sell pressure
        else if (rsi14 >= 50) score++;
        else score--;

        if (momentum > 1.0) score++;
        else if (momentum < -1.0) score--;

        String signal;
        if (score >= 3) signal = "STRONG BULLISH";
        else if (score >= 1) signal = "BULLISH";
        else if (score == 0) signal = "NEUTRAL";
        else if (score >= -2) signal = "BEARISH";
        else signal = "STRONG BEARISH";

        return new TechnicalReport(stock.getSymbol(), ltp, sma20, sma50, ema20, rsi14, vol, momentum, signal);
    }
}
