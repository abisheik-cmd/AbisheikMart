package com.abisheikmart.dao;

import com.abisheikmart.model.Cart;
import com.abisheikmart.model.CartItem;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartDao {

    private static final Logger logger = LoggerFactory.getLogger(CartDao.class);

    public Optional<Cart> findCartByUserId(Long userId) {
        String sql = "SELECT id, user_id, updated_at FROM cart WHERE user_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setId(rs.getLong("id"));
                    cart.setUserId(rs.getLong("user_id"));
                    cart.setUpdatedAt(rs.getTimestamp("updated_at"));
                    cart.setItems(getCartItemsByCartId(cart.getId()));
                    return Optional.of(cart);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart for user: {}", userId, e);
        }
        return Optional.empty();
    }

    public Cart createCartForUser(Long userId) throws SQLException {
        String sql = "INSERT INTO cart (user_id) VALUES (?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Cart cart = new Cart();
                    cart.setId(keys.getLong(1));
                    cart.setUserId(userId);
                    return cart;
                }
            }
        }
        throw new SQLException("Failed to create cart for user: " + userId);
    }

    public List<CartItem> getCartItemsByCartId(Long cartId) {
        List<CartItem> list = new ArrayList<>();
        String sql = """
            SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, ci.created_at,
                   p.name AS product_name, p.description, p.price AS unit_price, p.stock AS available_stock,
                   COALESCE(p.image_url, '') AS image_url, c.name AS category_name, p.seller_id, u.name AS seller_name
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            JOIN users u ON p.seller_id = u.id
            WHERE ci.cart_id = ?
            ORDER BY ci.id ASC;
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCartId(rs.getLong("cart_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setProductName(rs.getString("product_name"));
                    item.setDescription(rs.getString("description"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setAvailableStock(rs.getInt("available_stock"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setCategoryName(rs.getString("category_name"));
                    item.setSellerId(rs.getLong("seller_id"));
                    item.setSellerName(rs.getString("seller_name"));
                    item.setSubtotal(item.getUnitPrice() * item.getQuantity());
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching cart items for cartId: {}", cartId, e);
        }
        return list;
    }

    public void addOrUpdateCartItem(Long cartId, Long productId, int quantity) throws SQLException {
        String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setLong(1, cartId);
            checkPs.setLong(2, productId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    long itemId = rs.getLong("id");
                    int newQty = rs.getInt("quantity") + quantity;
                    try (PreparedStatement updatePs = conn.prepareStatement("UPDATE cart_items SET quantity = ? WHERE id = ?;")) {
                        updatePs.setInt(1, newQty);
                        updatePs.setLong(2, itemId);
                        updatePs.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertPs = conn.prepareStatement("INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?);")) {
                        insertPs.setLong(1, cartId);
                        insertPs.setLong(2, productId);
                        insertPs.setInt(3, quantity);
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }

    public void updateCartItemQuantity(Long cartId, Long productId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            if (quantity <= 0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?;")) {
                    ps.setLong(1, cartId);
                    ps.setLong(2, productId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?;")) {
                    ps.setInt(1, quantity);
                    ps.setLong(2, cartId);
                    ps.setLong(3, productId);
                    ps.executeUpdate();
                }
            }
        }
    }

    public void removeCartItem(Long cartId, Long productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    public void clearCart(Long cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        }
    }
}
