package tradex.util;

import tradex.model.Stock;
import tradex.model.Trade;
import tradex.service.PortfolioService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * File I/O manager implemented using Java NIO.2 (java.nio.file)
 * for CSV export generation, audit logs, and database backups.
 */
public class FileManager {
    private static final Path EXPORTS_DIR = Paths.get("exports");
    private static final Path LOGS_DIR = Paths.get("logs");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        try {
            if (!Files.exists(EXPORTS_DIR)) {
                Files.createDirectories(EXPORTS_DIR);
            }
            if (!Files.exists(LOGS_DIR)) {
                Files.createDirectories(LOGS_DIR);
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Could not initialize export directories: " + e.getMessage());
        }
    }

    public static Path exportPortfolioReport(PortfolioService.PortfolioSummary summary) throws IOException {
        String filename = "portfolio_account_" + summary.accountId + "_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".csv";
        Path targetPath = EXPORTS_DIR.resolve(filename);

        List<String> lines = new ArrayList<>();
        lines.add("Symbol,Quantity,AveragePrice,CurrentPrice,InvestedValue,CurrentValue,UnrealizedPnL,UnrealizedPnLPct,RealizedPnL");

        for (PortfolioService.PositionView pos : summary.positions) {
            lines.add(String.format("%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                    pos.symbol, pos.quantity, pos.averagePrice, pos.currentPrice,
                    pos.investedValue, pos.currentValue, pos.unrealizedPnL, pos.unrealizedPnLPct, pos.realizedPnL));
        }

        lines.add("");
        lines.add(String.format("Cash Balance,%.2f", summary.cashBalance));
        lines.add(String.format("Total Invested,%.2f", summary.totalInvested));
        lines.add(String.format("Total Current Value,%.2f", summary.totalCurrentValue));
        lines.add(String.format("Total Unrealized P&L,%.2f", summary.totalUnrealizedPnL));
        lines.add(String.format("Total Realized P&L,%.2f", summary.totalRealizedPnL));
        lines.add(String.format("Total Net Worth,%.2f", summary.netWorth));

        Files.write(targetPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return targetPath;
    }

    public static Path exportDailyMarketReport(List<Stock> stocks) throws IOException {
        String filename = "daily_market_report_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".csv";
        Path targetPath = EXPORTS_DIR.resolve(filename);

        List<String> lines = new ArrayList<>();
        lines.add("Symbol,Name,Sector,Price,PreviousClose,DayOpen,DayHigh,DayLow,ChangeAmount,ChangePct,Volume,UpperCircuit,LowerCircuit,Status");

        for (Stock s : stocks) {
            lines.add(String.format("%s,\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d,%.2f,%.2f,%s",
                    s.getSymbol(), s.getName(), s.getSector(), s.getCurrentPrice(), s.getPreviousClose(),
                    s.getDayOpen(), s.getDayHigh(), s.getDayLow(), s.getChangeAmount(), s.getChangePercentage(),
                    s.getVolume(), s.getUpperCircuit(), s.getLowerCircuit(), s.getStatus()));
        }

        Files.write(targetPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return targetPath;
    }

    public static Path exportTradeHistory(List<Trade> trades) throws IOException {
        String filename = "trade_history_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".csv";
        Path targetPath = EXPORTS_DIR.resolve(filename);

        List<String> lines = new ArrayList<>();
        lines.add("TradeId,BuyOrderId,SellOrderId,BuyerAccount,SellerAccount,Symbol,Quantity,Price,GrossAmount,BuyerBrokerage,SellerBrokerage,Timestamp");

        for (Trade t : trades) {
            lines.add(String.format("%s,%s,%s,%d,%d,%s,%d,%.2f,%.2f,%.2f,%.2f,%s",
                    t.getTradeId(), t.getBuyOrderId(), t.getSellOrderId(), t.getBuyerAccountId(),
                    t.getSellerAccountId(), t.getSymbol(), t.getQuantity(), t.getPrice(),
                    t.getGrossAmount(), t.getBrokerageBuyer(), t.getBrokerageSeller(), t.getTimestamp()));
        }

        Files.write(targetPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return targetPath;
    }

    public static void logMessage(String level, String message) {
        Path logPath = LOGS_DIR.resolve("tradex.log");
        String formatted = String.format("[%s] [%s] %s%n", LocalDateTime.now(), level, message);
        try {
            Files.writeString(logPath, formatted, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}
