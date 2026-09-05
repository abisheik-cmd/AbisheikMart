package com.abisheikmart.service;

import com.abisheikmart.dto.CartItemRequest;
import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.User;
import com.abisheikmart.repository.CartRepository;
import com.abisheikmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Map<String, Object> getCart(User user) {
        List<CartItem> items = cartRepository.findByBuyerId(user.getId());
        double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
        return Map.of("items", items, "total", total);
    }

    public void addToCart(User user, CartItemRequest req) {
        if (req.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        int qty = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;

        Product p = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (p.getStock() < qty) {
            throw new IllegalArgumentException("Insufficient stock available");
        }

        cartRepository.addOrUpdateItem(user.getId(), req.getProductId(), qty);
    }

    public void updateCartItem(User user, CartItemRequest req) {
        if (req.getProductId() == null || req.getQuantity() == null) {
            throw new IllegalArgumentException("Product ID and quantity are required");
        }
        if (req.getQuantity() > 0) {
            Product p = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            if (p.getStock() < req.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock available");
            }
        }

        cartRepository.updateQuantity(user.getId(), req.getProductId(), req.getQuantity());
    }

    public void removeFromCart(User user, Long productId) {
        cartRepository.deleteItem(user.getId(), productId);
    }
}

