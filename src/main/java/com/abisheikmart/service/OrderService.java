package com.abisheikmart.service;

import com.abisheikmart.dao.CartDao;
import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.model.Cart;
import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Coupon;
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
    private final CouponService couponService;

    public OrderService() {
        this.orderDao = new OrderDao();
        this.cartDao = new CartDao();
        this.couponService = new CouponService();
    }

    public OrderService(OrderDao orderDao, CartDao cartDao, CouponService couponService) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
        this.couponService = couponService;
    }

    public Order checkout(Long userId, String deliveryAddress, String phoneNumber, String paymentMethod, String couponCode) throws IllegalArgumentException, SQLException {
        Optional<Cart> cartOpt = cartDao.findCartByUserId(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout: Your shopping cart is empty.");
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cart.getItems();
        double cartSubtotal = cart.getGrandTotal();

        double discountAmount = 0.0;
        String validCouponCode = null;
        if (couponCode != null && !couponCode.isBlank()) {
            Optional<Coupon> couponOpt = couponService.validateCoupon(couponCode.trim(), cartSubtotal);
            if (couponOpt.isPresent()) {
                Coupon c = couponOpt.get();
                discountAmount = c.calculateDiscount(cartSubtotal);
                validCouponCode = c.getCode();
            }
        }

        double finalTotal = Math.max(0.0, cartSubtotal - discountAmount);

        Order order = orderDao.checkout(userId, cart.getId(), items, finalTotal,
                deliveryAddress != null ? deliveryAddress.trim() : "",
                phoneNumber != null ? phoneNumber.trim() : "",
                paymentMethod != null ? paymentMethod.trim() : "CASH_ON_DELIVERY",
                validCouponCode, discountAmount);

        logger.info("Successfully created order id: {}, user id: {}, final total: ₹{}", order.getId(), userId, finalTotal);
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
