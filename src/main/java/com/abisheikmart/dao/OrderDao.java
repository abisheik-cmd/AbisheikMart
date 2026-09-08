package com.abisheikmart.dao;

import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.OrderItem;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    public Order checkout(Long userId, Long cartId, List<CartItem> cartItems, double totalAmount) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into orders
            String orderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'PLACED');";
            long orderId;
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setLong(1, userId);
                psOrder.setDouble(2, totalAmount);
                psOrder.executeUpdate();
                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getLong(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated order ID.");
                    }
                }
            }

            // 2. Insert order items & deduct stock
            String itemSql = "INSERT INTO order_items (order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?);";
            String stockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?;";

            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : cartItems) {
                try (PreparedStatement psStock = conn.prepareStatement(stockSql)) {
                    psStock.setInt(1, ci.getQuantity());
                    psStock.setLong(2, ci.getProductId());
                    psStock.setInt(3, ci.getQuantity());
                    int updated = psStock.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Insufficient stock for product: " + ci.getProductName());
                    }
                }

                try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                    psItem.setLong(1, orderId);
                    psItem.setLong(2, ci.getProductId());
                    psItem.setLong(3, ci.getSellerId());
                    psItem.setString(4, ci.getProductName());
                    psItem.setInt(5, ci.getQuantity());
                    psItem.setDouble(6, ci.getUnitPrice());
                    psItem.setDouble(7, ci.getSubtotal());
                    psItem.executeUpdate();
                }

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

            // 3. Clear cart
            try (PreparedStatement psClear = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?;")) {
                psClear.setLong(1, cartId);
                psClear.executeUpdate();
            }

            conn.commit();

            Order order = new Order();
            order.setId(orderId);
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setStatus("PLACED");
            order.setItems(orderItems);
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Transaction rollback failed", ex);
                }
            }
            logger.error("Error during order checkout for user: {}", userId, e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection after checkout", e);
                }
            }
        }
    }

    public List<Order> findOrdersByUserId(Long userId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.id, o.user_id, u.name AS user_name, u.email AS user_email, o.total_amount, o.status, o.created_at
            FROM orders o
            JOIN users u ON o.user_id = u.id
            WHERE o.user_id = ?
            ORDER BY o.id DESC;
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapOrder(rs);
                    o.setItems(findOrderItemsByOrderId(o.getId()));
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding orders for user: {}", userId, e);
        }
        return orders;
    }

    public Optional<Order> findOrderById(Long orderId) {
        String sql = """
            SELECT o.id, o.user_id, u.name AS user_name, u.email AS user_email, o.total_amount, o.status, o.created_at
            FROM orders o
            JOIN users u ON o.user_id = u.id
            WHERE o.id = ?;
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = mapOrder(rs);
                    o.setItems(findOrderItemsByOrderId(o.getId()));
                    return Optional.of(o);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by id: {}", orderId, e);
        }
        return Optional.empty();
    }

    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT id, order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal FROM order_items WHERE order_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getLong("id"));
                    oi.setOrderId(rs.getLong("order_id"));
                    oi.setProductId(rs.getLong("product_id"));
                    oi.setSellerId(rs.getLong("seller_id"));
                    oi.setProductName(rs.getString("product_name"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getDouble("unit_price"));
                    oi.setSubtotal(rs.getDouble("subtotal"));
                    list.add(oi);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order items for orderId: {}", orderId, e);
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setUserEmail(rs.getString("user_email"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
