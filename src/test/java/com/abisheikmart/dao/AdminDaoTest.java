package com.abisheikmart.dao;

import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AdminDaoTest {
    private static final UserDao USER_DAO = new UserDao();
    private static final ProductDao PRODUCT_DAO = new ProductDao();
    private static final OrderDao ORDER_DAO = new OrderDao();

    @BeforeAll
    static void initializeDatabase() throws Exception {
        Properties properties = new Properties(); properties.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); properties.setProperty("db.driver", "org.h2.Driver"); properties.setProperty("db.username", "sa"); properties.setProperty("db.password", "");
        DBUtil.initDataSource(properties);
        try (InputStream input = AdminDaoTest.class.getClassLoader().getResourceAsStream("schema.sql"); Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            assertNotNull(input);
            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : schema.split(";")) if (!sql.trim().isEmpty()) try { statement.execute(sql); } catch (Exception ignored) { }
        }
    }

    @BeforeEach
    void seedFixture() throws Exception {
        try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM order_items"); statement.executeUpdate("DELETE FROM orders"); statement.executeUpdate("DELETE FROM products"); statement.executeUpdate("DELETE FROM categories"); statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("INSERT INTO users (id, name, email, password_hash, role, active) VALUES (1, 'Admin', 'admin@test', 'hash', 'ADMIN', TRUE), (2, 'Buyer', 'buyer@test', 'hash', 'CUSTOMER', TRUE), (3, 'Seller', 'seller@test', 'hash', 'SELLER', FALSE)");
            statement.executeUpdate("INSERT INTO categories (id, name, description) VALUES (1, 'Electronics', 'Test category'), (2, 'Books', 'Book category')");
            statement.executeUpdate("INSERT INTO products (id, seller_id, category_id, name, description, original_price, price, stock, image_url, active) VALUES (1, 3, 1, 'Active Product', 'A', 100, 80, 7, 'images/keyboard.jpg', TRUE), (2, 3, 2, 'Inactive Product', 'B', 200, 150, 0, 'images/books.jpg', FALSE)");
            statement.executeUpdate("INSERT INTO orders (id, user_id, total_amount, status, delivery_address, phone_number) VALUES (1, 2, 230, 'PLACED', 'Address', '9999999999'), (2, 2, 150, 'DELIVERED', 'Address', '9999999999')");
            statement.executeUpdate("INSERT INTO order_items (id, order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (1, 1, 1, 3, 'Active Product', 1, 80, 80), (2, 2, 2, 3, 'Inactive Product', 1, 150, 150)");
        }
    }

    @AfterAll static void closeDatabase() { DBUtil.closeDataSource(); }

    @Test
    void adminUserQueriesFilterAndUpdateStatus() throws Exception {
        assertEquals(3, USER_DAO.findAllUsers(null, null, null).size());
        assertEquals(1, USER_DAO.findAllUsers("sell", "SELLER", false).size());
        assertTrue(USER_DAO.updateUserStatus(3L, true));
        assertEquals(1, USER_DAO.findAllUsers(null, "SELLER", true).size());
    }

    @Test
    void adminProductQueriesFilterAndUpdateStatus() throws Exception {
        assertEquals(1, PRODUCT_DAO.findAdminProducts(null, null, 3L, true).size());
        assertEquals(1, PRODUCT_DAO.findAdminProducts("inactive", null, null, false).size());
        assertEquals(2, PRODUCT_DAO.countProducts()); assertEquals(1, PRODUCT_DAO.countActiveProducts()); assertEquals(1, PRODUCT_DAO.countInactiveProducts());
        assertTrue(PRODUCT_DAO.updateProductStatus(2L, true));
        assertEquals(2, PRODUCT_DAO.countActiveProducts());
    }

    @Test
    void adminOrderQueriesFilterUpdateAndCalculateStats() throws Exception {
        assertEquals(2, ORDER_DAO.findAllOrders(null, null, null).size());
        assertEquals(1, ORDER_DAO.findAllOrders(null, "PLACED", null).size());
        assertEquals(2, ORDER_DAO.countOrders()); assertEquals(1, ORDER_DAO.countOrdersByStatus("DELIVERED")); assertEquals(380.0, ORDER_DAO.calculateMarketplaceOrderValue());
        assertTrue(ORDER_DAO.updateOrderStatus(1L, "SHIPPED"));
        assertEquals("SHIPPED", ORDER_DAO.findOrderById(1L).orElseThrow().getStatus());
    }
}
