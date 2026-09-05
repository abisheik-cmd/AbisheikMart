package com.abisheikmart.controller;

import com.abisheikmart.dto.CartItemRequest;
import com.abisheikmart.model.User;
import com.abisheikmart.service.CartService;
import com.abisheikmart.util.CryptoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CryptoUtils cryptoUtils;

    public CartController(CartService cartService, CryptoUtils cryptoUtils) {
        this.cartService = cartService;
        this.cryptoUtils = cryptoUtils;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                       @RequestBody CartItemRequest req) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            cartService.addToCart(user, req);
            return ResponseEntity.ok(Map.of("message", "Product added to cart"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestBody CartItemRequest req) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            cartService.updateCartItem(user, req);
            return ResponseEntity.ok(Map.of("message", "Cart updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @PathVariable Long productId) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        cartService.removeFromCart(user, productId);
        return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
    }
}

