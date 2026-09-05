package com.abisheikmart.repository;

import com.abisheikmart.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CartItem> cartItemRowMapper = (rs, rowNum) -> {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setBuyerId(rs.getLong("buyer_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setDescription(rs.getString("description"));
        item.setUnitPrice(rs.getDouble("price"));
        item.setAvailableStock(rs.getInt("stock"));
        item.setCategory(rs.getString("category"));
        item.setSellerId(rs.getLong("seller_id"));
        item.setSellerName(rs.getString("seller_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setSubtotal(item.getUnitPrice() * item.getQuantity());
        return item;
    };

    public List<CartItem> findByBuyerId(Long buyerId) {
        String sql = """
            SELECT c.id, c.buyer_id, c.product_id, p.name AS product_name, p.description, p.price, p.stock, p.category, p.seller_id, u.name AS seller_name, c.quantity
            FROM cart_items c
            JOIN products p ON c.product_id = p.id
            JOIN users u ON p.seller_id = u.id
            WHERE c.buyer_id = ?
            ORDER BY c.id ASC;
        """;
        return jdbcTemplate.query(sql, cartItemRowMapper, buyerId);
    }

    public Optional<CartItem> findByBuyerAndProduct(Long buyerId, Long productId) {
        String sql = "SELECT * FROM cart_items WHERE buyer_id = ? AND product_id = ?;";
        List<CartItem> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            CartItem c = new CartItem();
            c.setId(rs.getLong("id"));
            c.setBuyerId(rs.getLong("buyer_id"));
            c.setProductId(rs.getLong("product_id"));
            c.setQuantity(rs.getInt("quantity"));
            return c;
        }, buyerId, productId);
        return list.stream().findFirst();
    }

    public void addOrUpdateItem(Long buyerId, Long productId, int quantity) {
        String checkSql = "SELECT id, quantity FROM cart_items WHERE buyer_id = ? AND product_id = ?;";
        List<CartItem> existing = jdbcTemplate.query(checkSql, (rs, rowNum) -> {
            CartItem c = new CartItem();
            c.setId(rs.getLong("id"));
            c.setQuantity(rs.getInt("quantity"));
            return c;
        }, buyerId, productId);

        if (!existing.isEmpty()) {
            int newQty = existing.get(0).getQuantity() + quantity;
            jdbcTemplate.update("UPDATE cart_items SET quantity = ? WHERE id = ?;", newQty, existing.get(0).getId());
        } else {
            jdbcTemplate.update("INSERT INTO cart_items (buyer_id, product_id, quantity) VALUES (?, ?, ?);", buyerId, productId, quantity);
        }
    }

    public void updateQuantity(Long buyerId, Long productId, int quantity) {
        if (quantity <= 0) {
            jdbcTemplate.update("DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;", buyerId, productId);
        } else {
            jdbcTemplate.update("UPDATE cart_items SET quantity = ? WHERE buyer_id = ? AND product_id = ?;", quantity, buyerId, productId);
        }
    }

    public void deleteItem(Long buyerId, Long productId) {
        jdbcTemplate.update("DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;", buyerId, productId);
    }

    public void clearCart(Long buyerId) {
        jdbcTemplate.update("DELETE FROM cart_items WHERE buyer_id = ?;", buyerId);
    }
}

