package tradex.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class DiagramGenerator {

    public static void main(String[] args) {
        System.out.println("Rendering architectural and design diagrams to docs/diagrams/...");
        File dir = new File("docs/diagrams");
        if (!dir.exists()) dir.mkdirs();

        generateUseCaseDiagram();
        generateClassDiagram();
        generateSequenceDiagram();
        generateWorkflowDiagram();
        generateERDiagram();

        System.out.println("All 5 design diagrams rendered successfully!");
    }

    static void initGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // 1. Use Case Diagram
    static void generateUseCaseDiagram() {
        int w = 1100, h = 750;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        initGraphics(g);

        // Background
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, w, h);

        // Header
        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("TradeX CLI Stock Exchange - System Use Case Diagram", 40, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Actors, System Boundaries, and Core Functional Operations", 40, 68);

        // System Boundary Box
        g.setColor(new Color(226, 232, 240));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(240, 95, 620, 610, 16, 16);
        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("TradeX Exchange Simulator System Boundary", 260, 125);

        // Draw Actors
        drawActor(g, 90, 240, "Trader / User");
        drawActor(g, 90, 520, "Simulation Bot");
        drawActor(g, 980, 360, "Exchange Operator / Admin");

        // Use cases
        String[] cases = {
                "UC-01: Authenticate & Manage Account",
                "UC-02: Deposit / Withdraw Virtual Funds",
                "UC-03: Browse Stock Catalog & LTP Quotes",
                "UC-04: View Double-Auction Order Book Depth",
                "UC-05: Place Order (Market / Limit / Stop)",
                "UC-06: Cancel Resting Open Order",
                "UC-07: Continuous Double-Auction Matching",
                "UC-08: Execute Atomic Clearing & Settlement",
                "UC-09: Portfolio & Realized/Unrealized P&L",
                "UC-10: Quantitative Technical Analysis (SMA/RSI)",
                "UC-11: Stochastic Simulation & News Injection",
                "UC-12: Export Reports to CSV (Java NIO.2)"
        };

        int startY = 150;
        int stepY = 44;
        for (int i = 0; i < cases.length; i++) {
            int cy = startY + (i * stepY);
            drawUseCaseBubble(g, 340, cy, 420, 36, cases[i]);

            // Connect Trader
            if (i != 6 && i != 7 && i != 10) {
                g.setColor(new Color(148, 163, 184));
                g.setStroke(new BasicStroke(1.2f));
                g.drawLine(145, 240, 340, cy + 18);
            }

            // Connect Bot
            if (i == 4 || i == 6 || i == 10) {
                g.setColor(new Color(59, 130, 246));
                g.drawLine(145, 520, 340, cy + 18);
            }

            // Connect Admin
            if (i == 0 || i == 2 || i == 10 || i == 11) {
                g.setColor(new Color(239, 68, 68));
                g.drawLine(940, 360, 760, cy + 18);
            }
        }

        g.dispose();
        saveImage(img, "docs/diagrams/use-case.png");
    }

    static void drawActor(Graphics2D g, int x, int y, String label) {
        g.setColor(new Color(30, 41, 59));
        g.setStroke(new BasicStroke(2.0f));
        // Head
        g.drawOval(x - 12, y - 45, 24, 24);
        // Body
        g.drawLine(x, y - 21, x, y + 10);
        // Arms
        g.drawLine(x - 22, y - 10, x + 22, y - 10);
        // Legs
        g.drawLine(x, y + 10, x - 18, y + 36);
        g.drawLine(x, y + 10, x + 18, y + 36);

        // Label
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x - (fm.stringWidth(label) / 2), y + 54);
    }

    static void drawUseCaseBubble(Graphics2D g, int x, int y, int w, int h, String text) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 20, 20);
        g.setColor(new Color(203, 213, 225));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 20, 20);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + 23);
    }

    // 2. Class Diagram
    static void generateClassDiagram() {
        int w = 1250, h = 850;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        initGraphics(g);

        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("TradeX CLI Stock Exchange - Core UML Class Diagram", 40, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Domain Entities, Double-Auction Exchange Engines, Repositories, and Strategy Hierarchies", 40, 68);

        // Boxes
        drawClassBox(g, 40, 110, 260, 200, "OrderBook",
                "- symbol: String\n- bids: PriorityQueue<Order>\n- asks: PriorityQueue<Order>\n- rwLock: ReentrantReadWriteLock",
                "+ addOrder(o: Order): void\n+ cancelOrder(id: String): boolean\n+ peekBestBid(): Optional<Order>\n+ peekBestAsk(): Optional<Order>\n+ getBidsDepth(n: int): List<Level>\n+ getSpread(): double");

        drawClassBox(g, 350, 110, 280, 210, "MatchingEngine",
                "- settlementEngine: SettlementEngine\n- orderRepo: OrderRepository\n- stockRepo: StockRepository",
                "+ match(o: Order, b: OrderBook): List<Trade>\n- matchBuyOrder(...): void\n- matchSellOrder(...): void\n- executeTrade(...): Trade");

        drawClassBox(g, 680, 110, 280, 210, "SettlementEngine",
                "- accountRepo: AccountRepository\n- holdingRepo: HoldingRepository\n- tradeRepo: TradeRepository\n- txRepo: TransactionRepository",
                "+ settle(trade: Trade): void\n+ calculateBrokerage(amt: double): double\n- debitBuyer(...): void\n- creditSeller(...): void");

        drawClassBox(g, 990, 110, 220, 180, "Exchange",
                "- orderBooks: Map<String, OrderBook>\n- marketStatus: MarketStatus",
                "+ submitOrder(o: Order): List<Trade>\n+ cancelOrder(id: String): boolean\n+ getOrderBook(sym: String): OrderBook\n+ openMarket(): void\n+ closeMarket(): void");

        drawClassBox(g, 40, 360, 250, 200, "Order",
                "- orderId: String\n- accountId: int\n- symbol: String\n- side: OrderSide\n- type: OrderType\n- quantity: int\n- price: double\n- status: OrderStatus",
                "+ fill(qty: int): void\n+ isTerminal(): boolean\n+ getRemainingQuantity(): int\n+ compareTo(other: Order): int");

        drawClassBox(g, 330, 360, 250, 190, "Trade",
                "- tradeId: String\n- buyOrderId: String\n- sellOrderId: String\n- buyerAccountId: int\n- sellerAccountId: int\n- quantity: int\n- price: double",
                "+ getGrossAmount(): double\n+ getBrokerageBuyer(): double\n+ getBrokerageSeller(): double\n+ getTimestamp(): LocalDateTime");

        drawClassBox(g, 620, 360, 260, 190, "Stock",
                "- symbol: String\n- currentPrice: double\n- dayOpen / High / Low: double\n- volume: long\n- upperCircuit / lowerCircuit: double",
                "+ updatePrice(p: double, vol: long)\n+ getChangePercentage(): double\n+ isCircuitBreached(p: double): boolean");

        drawClassBox(g, 920, 360, 280, 190, "Holding",
                "- holdingId: int\n- accountId: int\n- symbol: String\n- quantity: int\n- averageBuyPrice: double\n- realizedPnL: double",
                "+ addShares(q: int, p: double): void\n+ removeShares(q: int, p: double): void\n+ getUnrealizedPnL(ltp: double): double\n+ getCurrentValue(ltp: double): double");

        // Strategy pattern boxes
        drawClassBox(g, 220, 610, 240, 100, "<<interface>> TradingStrategy",
                "", "+ getName(): String\n+ evaluate(s: Stock, acc: int, cash: double, shares: int): Optional<Order>");

        drawClassBox(g, 40, 740, 190, 75, "MomentumStrategy", "", "+ evaluate(...): Optional<Order>");
        drawClassBox(g, 260, 740, 190, 75, "MeanReversionStrategy", "", "+ evaluate(...): Optional<Order>");
        drawClassBox(g, 480, 740, 200, 75, "RandomLiquidityStrategy", "", "+ evaluate(...): Optional<Order>");

        // DatabaseManager
        drawClassBox(g, 750, 610, 260, 140, "<<Singleton>> DatabaseManager",
                "- instance: DatabaseManager\n- dbUrl: String",
                "+ getInstance(): DatabaseManager\n+ getConnection(): Connection\n+ initializeSchema(): void\n+ resetDatabase(): void");

        // Connecting lines
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(300, 210, 350, 210); // OrderBook -> MatchingEngine
        g.drawLine(630, 210, 680, 210); // MatchingEngine -> SettlementEngine
        g.drawLine(960, 210, 990, 210); // SettlementEngine -> Exchange
        g.drawLine(160, 310, 160, 360); // OrderBook contains Orders
        g.drawLine(450, 320, 450, 360); // MatchingEngine creates Trades

        // Strategy hierarchy lines
        g.drawLine(135, 740, 340, 710);
        g.drawLine(355, 740, 340, 710);
        g.drawLine(580, 740, 340, 710);

        g.dispose();
        saveImage(img, "docs/diagrams/class-diagram.png");
    }

    static void drawClassBox(Graphics2D g, int x, int y, int w, int h, String name, String fields, String methods) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(new Color(203, 213, 225));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 8, 8);

        // Header
        g.setColor(new Color(241, 245, 249));
        g.fillRoundRect(x, y, w, 28, 8, 8);
        g.fillRect(x, y + 20, w, 8);
        g.setColor(new Color(203, 213, 225));
        g.drawLine(x, y + 28, x + w, y + 28);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, x + (w - fm.stringWidth(name)) / 2, y + 19);

        // Content
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        int curY = y + 42;
        if (!fields.isEmpty()) {
            for (String f : fields.split("\n")) {
                g.drawString(f, x + 8, curY);
                curY += 13;
            }
            g.setColor(new Color(226, 232, 240));
            g.drawLine(x, curY + 2, x + w, curY + 2);
            curY += 10;
        }

        g.setColor(new Color(51, 65, 85));
        if (!methods.isEmpty()) {
            for (String m : methods.split("\n")) {
                g.drawString(m, x + 8, curY);
                curY += 13;
            }
        }
    }

    // 3. Sequence Diagram
    static void generateSequenceDiagram() {
        int w = 1150, h = 750;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        initGraphics(g);

        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("TradeX CLI Stock Exchange - Order Matching & Settlement Sequence", 40, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Detailed message flow for order verification, matching, atomic clearing, and ledger updates", 40, 68);

        String[] lifelines = {"Trader / CLI", "Exchange", "CircuitBreaker", "AccountRepo", "OrderBook", "MatchingEngine", "SettlementEngine", "Database"};
        int[] lifeX = {80, 210, 350, 480, 620, 770, 930, 1070};

        // Draw lifelines
        for (int i = 0; i < lifelines.length; i++) {
            int x = lifeX[i];
            drawLifelineHeader(g, x, 110, lifelines[i]);
            g.setColor(new Color(203, 213, 225));
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            g.drawLine(x, 145, x, 710);
        }

        // Sequence Messages
        int y = 175;
        drawMessageArrow(g, lifeX[0], lifeX[1], y, "1: submitOrder(order)");
        y += 45;
        drawMessageArrow(g, lifeX[1], lifeX[2], y, "2: validatePriceBand(stock, price)");
        y += 45;
        drawReturnArrow(g, lifeX[2], lifeX[1], y, "3: Validation OK (within circuit)");
        y += 45;
        drawMessageArrow(g, lifeX[1], lifeX[3], y, "4: checkAvailableCash() / freezeCash()");
        y += 45;
        drawReturnArrow(g, lifeX[3], lifeX[1], y, "5: Margin reserved successfully");
        y += 45;
        drawMessageArrow(g, lifeX[1], lifeX[5], y, "6: match(order, orderBook)");
        y += 45;
        drawMessageArrow(g, lifeX[5], lifeX[4], y, "7: peekBestAsk() / pollBestAsk()");
        y += 45;
        drawReturnArrow(g, lifeX[4], lifeX[5], y, "8: Return cross match order");
        y += 45;
        drawMessageArrow(g, lifeX[5], lifeX[6], y, "9: settle(trade)");
        y += 45;
        drawMessageArrow(g, lifeX[6], lifeX[7], y, "10: Atomic SQL: Debit Buyer, Credit Seller, Transfer Shares");
        y += 45;
        drawReturnArrow(g, lifeX[7], lifeX[6], y, "11: Committed (ACID)");
        y += 45;
        drawReturnArrow(g, lifeX[6], lifeX[5], y, "12: Settlement confirmed");
        y += 45;
        drawReturnArrow(g, lifeX[5], lifeX[0], y, "13: Return List<Trade> (Fills displayed to CLI)");

        g.dispose();
        saveImage(img, "docs/diagrams/sequence-diagram.png");
    }

    static void drawLifelineHeader(Graphics2D g, int x, int y, String label) {
        int bw = 110, bh = 34;
        g.setColor(new Color(241, 245, 249));
        g.fillRoundRect(x - (bw / 2), y, bw, bh, 6, 6);
        g.setColor(new Color(71, 85, 105));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x - (bw / 2), y, bw, bh, 6, 6);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x - (fm.stringWidth(label) / 2), y + 21);
    }

    static void drawMessageArrow(Graphics2D g, int x1, int x2, int y, String label) {
        g.setColor(new Color(37, 99, 235));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(x1, y, x2, y);

        // Arrowhead
        int dir = x2 > x1 ? 1 : -1;
        g.fillPolygon(new int[]{x2, x2 - (dir * 8), x2 - (dir * 8)}, new int[]{y, y - 4, y + 4}, 3);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(30, 41, 59));
        g.drawString(label, Math.min(x1, x2) + 10, y - 5);
    }

    static void drawReturnArrow(Graphics2D g, int x1, int x2, int y, String label) {
        g.setColor(new Color(16, 185, 129));
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
        g.drawLine(x1, y, x2, y);

        int dir = x2 > x1 ? 1 : -1;
        g.fillPolygon(new int[]{x2, x2 - (dir * 8), x2 - (dir * 8)}, new int[]{y, y - 4, y + 4}, 3);

        g.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g.setColor(new Color(71, 85, 105));
        g.drawString(label, Math.min(x1, x2) + 10, y - 5);
    }

    // 4. Workflow Diagram
    static void generateWorkflowDiagram() {
        int w = 1100, h = 750;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        initGraphics(g);

        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("TradeX CLI Stock Exchange - End-to-End Trading Workflow", 40, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("Visual process execution sequence from user input to post-trade settlement and analytics", 40, 68);

        // Workflow nodes
        int x = 450;
        drawProcessNode(g, x, 110, 220, 45, "1. User Input / Order Entry\n(CLI Terminal)", new Color(224, 242, 254), new Color(2, 132, 199));
        drawProcessNode(g, x, 195, 220, 50, "2. Pre-Trade Risk Controls\n(Circuit Bands & Cash/Holdings)", new Color(254, 243, 199), new Color(217, 119, 6));
        drawProcessNode(g, x, 285, 220, 50, "3. Double-Auction Order Book\n(Price-Time Priority Queue)", new Color(243, 232, 255), new Color(147, 51, 234));
        drawProcessNode(g, x, 375, 220, 50, "4. Continuous Matching Engine\n(Double Auction Cross Detection)", new Color(236, 253, 245), new Color(16, 185, 129));
        drawProcessNode(g, x, 465, 220, 50, "5. Trade Execution Generation\n(Maker Price Rule & Brokerage)", new Color(254, 226, 226), new Color(220, 38, 38));
        drawProcessNode(g, x, 555, 220, 50, "6. Atomic Clearing & Settlement\n(Cash & Share Balance Updates)", new Color(224, 231, 255), new Color(79, 70, 229));
        drawProcessNode(g, x, 645, 220, 45, "7. Portfolio & Analytics Update\n(P&L Recalculation & Reports)", new Color(241, 245, 249), new Color(71, 85, 105));

        // Connect nodes
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(2.0f));
        for (int y = 155; y <= 600; y += 90) {
            g.drawLine(x + 110, y, x + 110, y + 40);
            g.fillPolygon(new int[]{x + 110, x + 105, x + 115}, new int[]{y + 40, y + 32, y + 32}, 3);
        }

        // Side boxes: Stochastic Simulation and Persistence
        drawProcessNode(g, 100, 285, 230, 80, "Market Simulation Engine\n- Geometric Brownian Motion\n- Automated Bot Strategies\n- News Shocks Injection", new Color(255, 237, 213), new Color(234, 88, 12));
        g.drawLine(330, 325, 450, 325);
        g.fillPolygon(new int[]{450, 442, 442}, new int[]{325, 320, 330}, 3);

        drawProcessNode(g, 780, 545, 220, 70, "SQLite Relational Storage\n- ACID Transaction Logging\n- Position Ledgers\n- NIO.2 CSV Report Export", new Color(240, 253, 244), new Color(22, 163, 74));
        g.drawLine(670, 580, 780, 580);
        g.fillPolygon(new int[]{780, 772, 772}, new int[]{580, 575, 585}, 3);

        g.dispose();
        saveImage(img, "docs/diagrams/workflow.png");
    }

    static void drawProcessNode(Graphics2D g, int x, int y, int w, int h, String text, Color bg, Color border) {
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(border);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String[] lines = text.split("\n");
        int ly = y + 18;
        for (String l : lines) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(l, x + (w - fm.stringWidth(l)) / 2, ly);
            ly += 15;
        }
    }

    // 5. ER Diagram
    static void generateERDiagram() {
        int w = 1200, h = 800;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        initGraphics(g);

        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("TradeX CLI Stock Exchange - Relational Database Schema & ER Diagram", 40, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(100, 116, 139));
        g.drawString("SQLite 3 Relational Tables, Primary Keys (PK), Foreign Keys (FK), and Cardinalities", 40, 68);

        // Tables
        drawERTable(g, 40, 110, 220, 150, "users", new String[]{
                "PK  id: INTEGER",
                "    username: TEXT (UQ)",
                "    password_hash: TEXT",
                "    role: TEXT",
                "    created_at: TEXT"
        });

        drawERTable(g, 330, 110, 230, 160, "accounts", new String[]{
                "PK  account_id: INTEGER",
                "FK  user_id: INTEGER",
                "    cash_balance: REAL",
                "    frozen_cash: REAL",
                "    created_at: TEXT"
        });

        drawERTable(g, 630, 110, 250, 220, "stocks", new String[]{
                "PK  symbol: TEXT",
                "    name: TEXT",
                "    sector: TEXT",
                "    current_price: REAL",
                "    previous_close: REAL",
                "    day_open / high / low: REAL",
                "    volume: INTEGER",
                "    upper / lower_circuit: REAL",
                "    status: TEXT"
        });

        drawERTable(g, 940, 110, 220, 220, "orders", new String[]{
                "PK  order_id: TEXT",
                "FK  account_id: INTEGER",
                "FK  symbol: TEXT",
                "    side: TEXT (BUY/SELL)",
                "    type: TEXT (LIMIT/MKT)",
                "    original_quantity: INT",
                "    filled_quantity: INT",
                "    price / stop_price: REAL",
                "    status: TEXT",
                "    timestamp: TEXT"
        });

        drawERTable(g, 40, 360, 240, 180, "trades", new String[]{
                "PK  trade_id: TEXT",
                "FK  buy_order_id: TEXT",
                "FK  sell_order_id: TEXT",
                "FK  buyer_account_id: INT",
                "FK  seller_account_id: INT",
                "FK  symbol: TEXT",
                "    quantity: INTEGER",
                "    price: REAL",
                "    brokerage_buyer / seller: REAL"
        });

        drawERTable(g, 340, 360, 240, 160, "holdings", new String[]{
                "PK  holding_id: INTEGER",
                "FK  account_id: INTEGER",
                "FK  symbol: TEXT",
                "    quantity: INTEGER",
                "    average_buy_price: REAL",
                "    realized_pnl: REAL",
                "UQ (account_id, symbol)"
        });

        drawERTable(g, 640, 360, 240, 160, "transactions", new String[]{
                "PK  transaction_id: INTEGER",
                "FK  account_id: INTEGER",
                "    type: TEXT",
                "    amount: REAL",
                "    balance_after: REAL",
                "    description: TEXT",
                "    timestamp: TEXT"
        });

        drawERTable(g, 940, 360, 220, 160, "alerts", new String[]{
                "PK  alert_id: INTEGER",
                "FK  account_id: INTEGER",
                "FK  symbol: TEXT",
                "    type: TEXT",
                "    target_value: REAL",
                "    triggered: INTEGER",
                "    created_at: TEXT"
        });

        drawERTable(g, 490, 600, 250, 140, "market_events", new String[]{
                "PK  event_id: INTEGER",
                "    headline: TEXT",
                "    symbol: TEXT (Nullable)",
                "    sentiment: TEXT",
                "    impact_pct: REAL",
                "    timestamp: TEXT"
        });

        // Relational arrows
        g.setColor(new Color(59, 130, 246));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(260, 170, 330, 170); // users -> accounts (1 to 1..N)
        g.drawLine(560, 170, 630, 170); // accounts -> stocks interaction
        g.drawLine(880, 170, 940, 170); // stocks -> orders
        g.drawLine(450, 270, 450, 360); // accounts -> holdings
        g.drawLine(760, 330, 760, 360); // stocks -> transactions/holdings

        g.dispose();
        saveImage(img, "docs/diagrams/er-diagram.png");
    }

    static void drawERTable(Graphics2D g, int x, int y, int w, int h, String name, String[] columns) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(new Color(203, 213, 225));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // Header
        g.setColor(new Color(30, 41, 59));
        g.fillRoundRect(x, y, w, 28, 6, 6);
        g.fillRect(x, y + 20, w, 8);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name.toUpperCase(), x + (w - fm.stringWidth(name.toUpperCase())) / 2, y + 19);

        // Columns
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        int cy = y + 44;
        for (String c : columns) {
            if (c.startsWith("PK")) {
                g.setColor(new Color(185, 28, 28)); // Red for PK
            } else if (c.startsWith("FK")) {
                g.setColor(new Color(29, 78, 216)); // Blue for FK
            } else {
                g.setColor(new Color(51, 65, 85));
            }
            g.drawString(c, x + 8, cy);
            cy += 14;
        }
    }

    static void saveImage(BufferedImage img, String path) {
        try {
            ImageIO.write(img, "png", new File(path));
        } catch (IOException e) {
            System.err.println("Could not write diagram " + path + ": " + e.getMessage());
        }
    }
}

