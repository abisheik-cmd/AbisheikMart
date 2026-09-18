package com.abisheikmart.dao;

import com.abisheikmart.model.Cart;
import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class CartDaoTest {
    private static final CartDao DAO = new CartDao();
    @BeforeAll static void init() throws Exception { Properties p = new Properties(); p.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); p.setProperty("db.driver", "org.h2.Driver"); DBUtil.initDataSource(p); try (InputStream in = CartDaoTest.class.getClassLoader().getResourceAsStream("schema.sql"); Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) { assertNotNull(in); for (String sql : new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if (!sql.isBlank()) try { s.execute(sql); } catch (SQLException ignored) {} } }
    @BeforeEach void seed() throws Exception { try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) { s.executeUpdate("DELETE FROM cart_items"); s.executeUpdate("DELETE FROM cart"); s.executeUpdate("DELETE FROM products"); s.executeUpdate("DELETE FROM categories"); s.executeUpdate("DELETE FROM users"); s.executeUpdate("INSERT INTO users (id,name,email,password_hash,role) VALUES (1,'Buyer 1','b1@t','h','CUSTOMER'),(2,'Buyer 2','b2@t','h','CUSTOMER'),(3,'Seller','s@t','h','SELLER')"); s.executeUpdate("INSERT INTO categories (id,name) VALUES (1,'Test')"); s.executeUpdate("INSERT INTO products (id,seller_id,category_id,name,description,original_price,price,stock,image_url) VALUES (1,3,1,'Item','Desc',20,10,5,'')"); } }
    @AfterAll static void close() { DBUtil.closeDataSource(); }
    @Test void cartsAreIsolatedAndMutationsCalculateSubtotal() throws Exception { Cart one = DAO.createCartForUser(1L); Cart two = DAO.createCartForUser(2L); DAO.addOrUpdateCartItem(one.getId(),1L,2); DAO.addOrUpdateCartItem(one.getId(),1L,1); DAO.addOrUpdateCartItem(two.getId(),1L,1); assertEquals(1, DAO.findCartByUserId(1L).orElseThrow().getItems().size()); assertEquals(3, DAO.findCartByUserId(1L).orElseThrow().getItems().get(0).getQuantity()); assertEquals(30.0, DAO.findCartByUserId(1L).orElseThrow().getGrandTotal()); assertEquals(1, DAO.findCartByUserId(2L).orElseThrow().getItems().get(0).getQuantity()); DAO.updateCartItemQuantity(one.getId(),1L,2); assertEquals(2, DAO.findCartByUserId(1L).orElseThrow().getItems().get(0).getQuantity()); DAO.removeCartItem(one.getId(),1L); assertTrue(DAO.findCartByUserId(1L).orElseThrow().getItems().isEmpty()); DAO.clearCart(two.getId()); assertTrue(DAO.findCartByUserId(2L).orElseThrow().getItems().isEmpty()); }
}
