package com.abisheikmart.repository;

import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getString("created_at"));
        return o;
    };

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem oi = new OrderItem();
        oi.setId(rs.getLong("id"));
        oi.setOrderId(rs.getLong("order_id"));
        oi.setProductId(rs.getLong("product_id"));
        oi.setSellerId(rs.getLong("seller_id"));
        oi.setProductName(rs.getString("product_name"));
        oi.setQuantity(rs.getInt("quantity"));
        oi.setUnitPrice(rs.getDouble("unit_price"));
        oi.setSubtotal(rs.getDouble("subtotal"));
        return oi;
    };

    @Transactional
    public Order checkout(Long buyerId, List<CartItem> cartItems, double totalAmount) {
        // 1. Create order
        String orderSql = "INSERT INTO orders (buyer_id, total_amount, status) VALUES (?, ?, 'PLACED');";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, buyerId);
            ps.setDouble(2, totalAmount);
            return ps;
        }, keyHolder);

        Long orderId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : 0L;

        List<OrderItem> orderItems = new ArrayList<>();
        String itemSql = "INSERT INTO order_items (order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?);";
        String stockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?;";

        for (CartItem ci : cartItems) {
            jdbcTemplate.update(itemSql,
                    orderId,
                    ci.getProductId(),
                    ci.getSellerId(),
                    ci.getProductName(),
                    ci.getQuantity(),
                    ci.getUnitPrice(),
                    ci.getSubtotal()
            );

            jdbcTemplate.update(stockSql, ci.getQuantity(), ci.getProductId(), ci.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setOrderId(orderId);
            oi.setProductId(ci.getProductId());
            oi.setSellerId(ci.getSellerId());
            oi.setProductName(ci.getProductName());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setSubtotal(ci.getSubtotal());
            orderItems.add(oi);
        }

        // Clear cart
        jdbcTemplate.update("DELETE FROM cart_items WHERE buyer_id = ?;", buyerId);

        Order order = new Order();
        order.setId(orderId);
        order.setBuyerId(buyerId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");
        order.setItems(orderItems);
        return order;
    }

    public List<Order> findOrdersByBuyerId(Long buyerId) {
        String sql = "SELECT id, buyer_id, total_amount, status, created_at FROM orders WHERE buyer_id = ? ORDER BY id DESC;";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, buyerId);

        String itemSql = "SELECT id, order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal FROM order_items WHERE order_id = ?;";
        for (Order o : orders) {
            List<OrderItem> items = jdbcTemplate.query(itemSql, orderItemRowMapper, o.getId());
            o.setItems(items);
        }
        return orders;
    }
}

