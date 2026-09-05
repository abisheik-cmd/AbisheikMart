package com.abisheikmart.service;

import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.User;
import com.abisheikmart.repository.CartRepository;
import com.abisheikmart.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    public Order checkout(User user) {
        List<CartItem> cartItems = cartRepository.findByBuyerId(user.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout: Cart is empty");
        }

        double totalAmount = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        return orderRepository.checkout(user.getId(), cartItems, totalAmount);
    }

    public List<Order> getOrderHistory(User user) {
        return orderRepository.findOrdersByBuyerId(user.getId());
    }
}

