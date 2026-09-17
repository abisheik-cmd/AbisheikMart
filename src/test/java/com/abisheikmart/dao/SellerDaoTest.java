package com.abisheikmart.dao;

import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SellerDaoTest {
    private static final ProductDao PRODUCT_DAO = new ProductDao();
    private static final OrderDao ORDER_DAO = new OrderDao();

    @BeforeAll
    static void initializeDatabase() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        properties.setProperty("db.driver", "org.h2.Driver");
        properties.setProperty("db.username", "sa");
        properties.setProperty("db.password", "");
        DBUtil.initDataSource(properties);
        try (InputStream input = SellerDaoTest.class.getClassLoader().getResourceAsStream("schema.sql");
             Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            assertNotNull(input);
            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : schema.split(";")) {
                if (!sql.trim().isEmpty()) {
                    try { statement.execute(sql); } catch (Exception ignored) { }
                }
            }
        }
    }

    @BeforeEach
    void seedFixture() throws Exception {
        try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM order_items");
            statement.executeUpdate("DELETE FROM orders");
            statement.executeUpdate("DELETE FROM products");
            statement.executeUpdate("DELETE FROM categories");
            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("INSERT INTO users (id, name, email, password_hash, role) VALUES (1, 'Seller One', 'seller1@test', 'hash', 'SELLER'), (2, 'Buyer One', 'buyer@test', 'hash', 'CUSTOMER'), (3, 'Seller Two', 'seller2@test', 'hash', 'SELLER')");
            statement.executeUpdate("INSERT INTO categories (id, name, description) VALUES (1, 'Electronics', 'Test category')");
            statement.executeUpdate("INSERT INTO products (id, seller_id, category_id, name, description, original_price, price, stock, image_url) VALUES (1, 1, 1, 'Seller Product', 'Desc', 100, 80, 7, 'images/keyboard.jpg'), (2, 3, 1, 'Other Product', 'Desc', 100, 90, 4, 'images/camera.jpg')");
            statement.executeUpdate("INSERT INTO orders (id, user_id, total_amount, status, delivery_address, phone_number) VALUES (1, 2, 170, 'PLACED', 'Test address', '9999999999')");
            statement.executeUpdate("INSERT INTO order_items (id, order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (1, 1, 1, 1, 'Seller Product', 1, 80, 80), (2, 1, 2, 3, 'Other Product', 1, 90, 90)");
        }
    }

    @AfterAll
    static void closeDatabase() { DBUtil.closeDataSource(); }

    @Test
    void sellerProductAndOrderQueriesAreScopedToSeller() {
        assertEquals(1, PRODUCT_DAO.countBySellerId(1L));
        assertEquals(1, PRODUCT_DAO.countActiveBySellerId(1L));
        assertEquals(1, ORDER_DAO.countSellerOrders(1L));
        assertEquals(1, ORDER_DAO.countPendingSellerOrders(1L));
        assertEquals(1, ORDER_DAO.findSellerOrderSummaries(1L).size());
        assertTrue(ORDER_DAO.findSellerOrderSummariesByOrderId(1L, 1L).stream().allMatch(line -> line.getProductId() == 1L));
        assertTrue(ORDER_DAO.findSellerOrderSummariesByOrderId(1L, 3L).stream().allMatch(line -> line.getProductId() == 2L));
    }

    @Test
    void sellerCanAdvanceOnlyAssociatedOrderStatus() {
        assertTrue(ORDER_DAO.updateSellerOrderStatus(1L, 1L, "SHIPPED"));
        assertEquals("SHIPPED", ORDER_DAO.findSellerOrderSummariesByOrderId(1L, 1L).get(0).getStatus());
        assertFalse(ORDER_DAO.updateSellerOrderStatus(1L, 99L, "DELIVERED"));
    }
}
