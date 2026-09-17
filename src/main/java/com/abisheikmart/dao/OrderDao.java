package com.abisheikmart.dao;

import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.OrderItem;
import com.abisheikmart.model.SellerOrderSummary;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    public Order checkout(Long userId, Long cartId, List<CartItem> cartItems, double totalAmount,
                          String deliveryAddress, String phoneNumber, String paymentMethod,
                          String couponCode, double discountAmount) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into orders
            String orderSql = "INSERT INTO orders (user_id, total_amount, status, delivery_address, phone_number, payment_method, coupon_code, discount_amount) VALUES (?, ?, 'PLACED', ?, ?, ?, ?, ?);";
            long orderId;
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setLong(1, userId);
                psOrder.setDouble(2, totalAmount);
                psOrder.setString(3, deliveryAddress);
                psOrder.setString(4, phoneNumber);
                psOrder.setString(5, paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY");
                psOrder.setString(6, couponCode);
                psOrder.setDouble(7, discountAmount);
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
            order.setDeliveryAddress(deliveryAddress);
            order.setPhoneNumber(phoneNumber);
            order.setPaymentMethod(paymentMethod);
            order.setCouponCode(couponCode);
            order.setDiscountAmount(discountAmount);
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
            SELECT o.id, o.user_id, u.name AS user_name, u.email AS user_email, o.total_amount, o.status,
                   o.delivery_address, o.phone_number, o.payment_method, o.coupon_code, o.discount_amount, o.created_at
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
            SELECT o.id, o.user_id, u.name AS user_name, u.email AS user_email, o.total_amount, o.status,
                   o.delivery_address, o.phone_number, o.payment_method, o.coupon_code, o.discount_amount, o.created_at
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

    public List<SellerOrderSummary> findSellerOrderSummaries(Long sellerId) {
        String sql = "SELECT o.id AS order_id, o.created_at, o.status, o.delivery_address, o.phone_number, "
                + "u.name AS buyer_name, u.email AS buyer_email, oi.product_id, oi.product_name, oi.quantity, "
                + "oi.unit_price, oi.subtotal FROM orders o JOIN users u ON u.id = o.user_id "
                + "JOIN order_items oi ON oi.order_id = o.id WHERE oi.seller_id = ? ORDER BY o.created_at DESC, o.id DESC";
        return sellerSummaryQuery(sql, sellerId, null);
    }

    public List<SellerOrderSummary> findSellerOrderSummariesByOrderId(Long orderId, Long sellerId) {
        String sql = "SELECT o.id AS order_id, o.created_at, o.status, o.delivery_address, o.phone_number, "
                + "u.name AS buyer_name, u.email AS buyer_email, oi.product_id, oi.product_name, oi.quantity, "
                + "oi.unit_price, oi.subtotal FROM orders o JOIN users u ON u.id = o.user_id "
                + "JOIN order_items oi ON oi.order_id = o.id WHERE oi.order_id = ? AND oi.seller_id = ? ORDER BY oi.id";
        return sellerSummaryQuery(sql, sellerId, orderId);
    }

    public long countSellerOrders(Long sellerId) {
        return countSeller("SELECT COUNT(DISTINCT order_id) FROM order_items WHERE seller_id = ?", sellerId);
    }

    public long countPendingSellerOrders(Long sellerId) {
        return countSeller("SELECT COUNT(DISTINCT oi.order_id) FROM order_items oi JOIN orders o ON o.id = oi.order_id WHERE oi.seller_id = ? AND o.status = 'PLACED'", sellerId);
    }

    public boolean updateSellerOrderStatus(Long orderId, Long sellerId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ? AND EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = orders.id AND oi.seller_id = ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, orderId);
            ps.setLong(3, sellerId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error updating seller order status for orderId: {}", orderId, e);
            return false;
        }
    }

    private List<SellerOrderSummary> sellerSummaryQuery(String sql, Long sellerId, Long orderId) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (orderId == null) {
                ps.setLong(1, sellerId);
            } else {
                ps.setLong(1, orderId);
                ps.setLong(2, sellerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<SellerOrderSummary> list = new ArrayList<>();
                while (rs.next()) list.add(mapSellerSummary(rs));
                return list;
            }
        } catch (SQLException e) {
            logger.error("Error finding seller order summaries for sellerId: {}", sellerId, e);
            return new ArrayList<>();
        }
    }

    private long countSeller(String sql, Long sellerId) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            logger.error("Error counting seller orders for sellerId: {}", sellerId, e);
            return 0L;
        }
    }

    private SellerOrderSummary mapSellerSummary(ResultSet rs) throws SQLException {
        SellerOrderSummary summary = new SellerOrderSummary();
        summary.setOrderId(rs.getLong("order_id"));
        summary.setOrderDate(rs.getTimestamp("created_at"));
        summary.setStatus(rs.getString("status"));
        summary.setDeliveryAddress(rs.getString("delivery_address"));
        summary.setPhoneNumber(rs.getString("phone_number"));
        summary.setBuyerName(rs.getString("buyer_name"));
        summary.setBuyerEmail(rs.getString("buyer_email"));
        summary.setProductId(rs.getLong("product_id"));
        summary.setProductName(rs.getString("product_name"));
        summary.setQuantity(rs.getInt("quantity"));
        summary.setUnitPrice(rs.getDouble("unit_price"));
        summary.setSubtotal(rs.getDouble("subtotal"));
        return summary;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setUserEmail(rs.getString("user_email"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setPhoneNumber(rs.getString("phone_number"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setCouponCode(rs.getString("coupon_code"));
        o.setDiscountAmount(rs.getDouble("discount_amount"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}
