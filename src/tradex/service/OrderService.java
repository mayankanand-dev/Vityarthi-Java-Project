package tradex.service;

import tradex.exception.TradeXException;
import tradex.exchange.Exchange;
import tradex.model.Order;
import tradex.model.Trade;
import tradex.model.enums.OrderSide;
import tradex.model.enums.OrderStatus;
import tradex.model.enums.OrderType;
import tradex.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    Exchange exchange;
    OrderRepository orderRepo;

    public OrderService(Exchange exchange, OrderRepository orderRepo) {
        this.exchange = exchange;
        this.orderRepo = orderRepo;
    }

    public List<Trade> placeOrder(int accountId, String symbol, OrderSide side,
                                  OrderType type, int quantity, double price, double stopPrice) throws TradeXException {
        Order order = new Order.Builder()
                .accountId(accountId)
                .symbol(symbol)
                .side(side)
                .type(type)
                .quantity(quantity)
                .price(price)
                .stopPrice(stopPrice)
                .status(OrderStatus.OPEN)
                .timestamp(LocalDateTime.now())
                .build();
        return exchange.submitOrder(order);
    }

    public boolean cancelOrder(String orderId, int accountId) throws TradeXException {
        return exchange.cancelOrder(orderId, accountId);
    }

    public List<Order> getOpenOrders(int accountId) { return orderRepo.findOpenOrdersByAccountId(accountId); }
    public List<Order> getAllOrders(int accountId) { return orderRepo.findByAccountId(accountId); }
}
