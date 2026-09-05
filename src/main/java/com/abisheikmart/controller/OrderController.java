package com.abisheikmart.controller;

import com.abisheikmart.model.Order;
import com.abisheikmart.model.User;
import com.abisheikmart.service.OrderService;
import com.abisheikmart.util.CryptoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final CryptoUtils cryptoUtils;

    public OrderController(OrderService orderService, CryptoUtils cryptoUtils) {
        this.orderService = orderService;
        this.cryptoUtils = cryptoUtils;
    }

    @PostMapping("/api/checkout")
    public ResponseEntity<?> checkout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            Order order = orderService.checkout(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Order placed successfully",
                    "order", order
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/orders")
    public ResponseEntity<?> getOrderHistory(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        List<Order> orders = orderService.getOrderHistory(user);
        return ResponseEntity.ok(Map.of("orders", orders));
    }
}

