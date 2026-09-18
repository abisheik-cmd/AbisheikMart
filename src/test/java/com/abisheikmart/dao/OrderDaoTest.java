package com.abisheikmart.dao;

import com.abisheikmart.model.CartItem;
import com.abisheikmart.model.Order;
import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class OrderDaoTest {
    private static final OrderDao DAO = new OrderDao();
    @BeforeAll static void init() throws Exception { Properties p = new Properties(); p.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); p.setProperty("db.driver", "org.h2.Driver"); DBUtil.initDataSource(p); try (InputStream in = OrderDaoTest.class.getClassLoader().getResourceAsStream("schema.sql"); Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) { assertNotNull(in); for (String sql : new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if (!sql.isBlank()) try { s.execute(sql); } catch (SQLException ignored) {} } }
    @BeforeEach void seed() throws Exception { try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) { s.executeUpdate("DELETE FROM order_items"); s.executeUpdate("DELETE FROM orders"); s.executeUpdate("DELETE FROM cart_items"); s.executeUpdate("DELETE FROM cart"); s.executeUpdate("DELETE FROM products"); s.executeUpdate("DELETE FROM categories"); s.executeUpdate("DELETE FROM users"); s.executeUpdate("INSERT INTO users (id,name,email,password_hash,role) VALUES (1,'Buyer','buyer@t','h','CUSTOMER'),(2,'Seller','seller@t','h','SELLER')"); s.executeUpdate("INSERT INTO categories (id,name) VALUES (1,'Test')"); s.executeUpdate("INSERT INTO products (id,seller_id,category_id,name,description,original_price,price,stock,image_url) VALUES (1,2,1,'Available','Desc',20,10,5,''),(2,2,1,'Limited','Desc',30,15,1,'')"); s.executeUpdate("INSERT INTO cart (id,user_id) VALUES (1,1)"); s.executeUpdate("INSERT INTO cart_items (cart_id,product_id,quantity) VALUES (1,1,2)"); } }
    @AfterAll static void close() { DBUtil.closeDataSource(); }
    private CartItem item(long productId, int quantity, double price, String name) { CartItem item = new CartItem(); item.setProductId(productId); item.setSellerId(2L); item.setProductName(name); item.setQuantity(quantity); item.setUnitPrice(price); item.setSubtotal(price * quantity); return item; }
    @Test void successfulCheckoutDeductsStockCreatesSnapshotsAndClearsCart() throws Exception { Order order = DAO.checkout(1L, 1L, List.of(item(1L,2,10,"Available")), 20,"Address","999","CASH_ON_DELIVERY",null,0); assertNotNull(order.getId()); assertEquals(1, order.getItems().size()); try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("SELECT stock FROM products WHERE id=1"); ResultSet r = p.executeQuery()) { assertTrue(r.next()); assertEquals(3, r.getInt(1)); } try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM cart_items")) { assertTrue(r.next()); assertEquals(0, r.getInt(1)); } }
    @Test void failedCheckoutRollsBackOrderItemsStockAndCart() throws Exception { assertThrows(SQLException.class, () -> DAO.checkout(1L, 1L, List.of(item(1L,2,10,"Available"), item(2L,2,15,"Limited")), 50,"Address","999","CASH_ON_DELIVERY",null,0)); try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT (SELECT COUNT(*) FROM orders), (SELECT stock FROM products WHERE id=1), (SELECT COUNT(*) FROM cart_items)")) { assertTrue(r.next()); assertEquals(0, r.getInt(1)); assertEquals(5, r.getInt(2)); assertEquals(1, r.getInt(3)); } }
}
