package com.abisheikmart.service;

import com.abisheikmart.dao.CartDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.model.Cart;
import com.abisheikmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService() {
        this.cartDao = new CartDao();
        this.productDao = new ProductDao();
    }

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public Cart getOrCreateCart(Long userId) throws SQLException {
        Optional<Cart> cartOpt = cartDao.findCartByUserId(userId);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        return cartDao.createCartForUser(userId);
    }

    public Cart addToCart(Long userId, Long productId, int quantity) throws IllegalArgumentException, SQLException {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (quantity <= 0) {
            quantity = 1;
        }

        Optional<Product> productOpt = productDao.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock available for " + product.getName() + ". Only " + product.getStock() + " left.");
        }

        Cart cart = getOrCreateCart(userId);
        cartDao.addOrUpdateCartItem(cart.getId(), productId, quantity);
        logger.info("Added product id: {} (qty: {}) to cart for user id: {}", productId, quantity, userId);

        return getOrCreateCart(userId);
    }

    public Cart updateCartItemQuantity(Long userId, Long productId, int quantity) throws IllegalArgumentException, SQLException {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }

        Cart cart = getOrCreateCart(userId);
        if (quantity > 0) {
            Optional<Product> productOpt = productDao.findById(productId);
            if (productOpt.isPresent() && productOpt.get().getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock available. Only " + productOpt.get().getStock() + " left.");
            }
        }

        cartDao.updateCartItemQuantity(cart.getId(), productId, quantity);
        logger.info("Updated cart item productId: {} to qty: {} for user id: {}", productId, quantity, userId);
        return getOrCreateCart(userId);
    }

    public Cart removeFromCart(Long userId, Long productId) throws SQLException {
        Cart cart = getOrCreateCart(userId);
        cartDao.removeCartItem(cart.getId(), productId);
        logger.info("Removed productId: {} from cart for user id: {}", productId, userId);
        return getOrCreateCart(userId);
    }

    public void clearCart(Long userId) throws SQLException {
        Cart cart = getOrCreateCart(userId);
        cartDao.clearCart(cart.getId());
        logger.info("Cleared cart for user id: {}", userId);
    }
}

