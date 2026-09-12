package com.abisheikmart.service;

import com.abisheikmart.dao.CartDao;
import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.model.Cart;
import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderDao orderDao;
    private final CartDao cartDao;

    public OrderService() {
        this.orderDao = new OrderDao();
        this.cartDao = new CartDao();
    }

    public OrderService(OrderDao orderDao, CartDao cartDao) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
    }

    public Order checkout(Long userId) throws IllegalArgumentException, SQLException {
        Optional<Cart> cartOpt = cartDao.findCartByUserId(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout: Your shopping cart is empty.");
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cart.getItems();
        double grandTotal = cart.getGrandTotal();

        Order order = orderDao.checkout(userId, cart.getId(), items, grandTotal);
        logger.info("Successfully created order id: {}, user id: {}, total: ₹{}", order.getId(), userId, grandTotal);
        return order;
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderDao.findOrdersByUserId(userId);
    }

    public Optional<Order> getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Optional<Order> orderOpt = orderDao.findOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (isAdmin || order.getUserId().equals(userId)) {
                return Optional.of(order);
            } else {
                throw new SecurityException("Unauthorized: You do not have permission to view this order.");
            }
        }
        return Optional.empty();
    }
}

